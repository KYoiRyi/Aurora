package emu.nebula.server.routes;

import emu.nebula.Nebula;
import emu.nebula.server.HttpServer;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceProxyHandler implements Handler {
    private final HttpServer server;
    private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long ttlMillis = 600_000;

    public ResourceProxyHandler(HttpServer server) {
        this.server = server;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String ua = ctx.header("User-Agent");
        String unity = ctx.header("X-Unity-Version");
        if (ua == null || !ua.contains("UnityPlayer") || unity == null) {
            ctx.status(403);
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{}");
            return;
        }
        String version = ctx.pathParam("version");
        String base = "/res/win/" + version + "/";
        String full = ctx.path();
        String rest = full.length() > base.length() ? full.substring(base.length()) : "";
        String url = "https://nova-static.stellasora.global/res/win/" + version + "/" + rest;
        CacheEntry entry = get(url);
        if (entry != null) {
            ctx.contentType(entry.contentType);
            ctx.result(entry.data);
            return;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", ua)
                    .header("X-Unity-Version", unity)
                    .header("Accept", "*/*")
                    .GET().build();
            HttpResponse<byte[]> rsp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (rsp.statusCode() == 200) {
                String ct = rsp.headers().firstValue("Content-Type").orElse("application/octet-stream");
                byte[] body = rsp.body();
                put(url, body, ct);
                Nebula.getLogger().info("Fetched resource: " + url);
                ctx.contentType(ct);
                ctx.result(body);
                return;
            }
            Nebula.getLogger().warn("Resource fetch failed: " + url + " status=" + rsp.statusCode());
            ctx.status(404);
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{}");
        } catch (Exception e) {
            Nebula.getLogger().error("Resource proxy error: " + url, e);
            ctx.status(500);
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result("{}");
        }
    }

    private CacheEntry get(String key) {
        CacheEntry e = cache.get(key);
        if (e == null) return null;
        long now = System.currentTimeMillis();
        if ((now - e.ts) > ttlMillis) {
            cache.remove(key);
            return null;
        }
        return e;
    }

    private void put(String key, byte[] data, String ct) {
        cache.put(key, new CacheEntry(data, ct, System.currentTimeMillis()));
        evict();
    }

    private void evict() {
        if (cache.size() <= 1024) return;
        long now = System.currentTimeMillis();
        for (Map.Entry<String, CacheEntry> e : cache.entrySet()) {
            if ((now - e.getValue().ts) > ttlMillis) cache.remove(e.getKey());
            if (cache.size() <= 1024) break;
        }
    }

    private static class CacheEntry {
        final byte[] data;
        final String contentType;
        final long ts;
        CacheEntry(byte[] d, String ct, long t) { this.data = d; this.contentType = ct; this.ts = t; }
    }
}
