package emu.nebula.server.routes;

import org.jetbrains.annotations.NotNull;

import emu.nebula.server.HttpServer;
import emu.nebula.util.AeadHelper;
import emu.nebula.util.Utils;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AccessLevel;
import lombok.Getter;
import emu.nebula.Nebula;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Getter(AccessLevel.PRIVATE)
public class MetaWinHandler implements Handler {
    private HttpServer server;
    private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();
    private static volatile long cacheTime;
    private static final long ttlMillis = 300_000;
    
    public MetaWinHandler(HttpServer server) {
        this.server = server;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String ua = ctx.header("User-Agent");
        String unity = ctx.header("X-Unity-Version");
        if (ua != null && ua.contains("UnityPlayer") && unity != null) {
            byte[] data = getRealWinHtml(ua, unity);
            if (data != null) {
                ctx.contentType(ContentType.APPLICATION_OCTET_STREAM);
                ctx.result(data);
                return;
            }
        }
        var diffBytes = this.getServer().getDiff();
        if (diffBytes == null) {
            ctx.contentType(ContentType.APPLICATION_OCTET_STREAM);
            ctx.result(Utils.EMPTY_BYTE_ARRAY);
            return;
        }
        ctx.contentType(ContentType.APPLICATION_OCTET_STREAM);
        ctx.result(AeadHelper.encryptCBC(diffBytes));
    }

    private byte[] getRealWinHtml(String ua, String unity) {
        long now = System.currentTimeMillis();
        byte[] cached = cache.get("win.html");
        if (cached != null && (now - cacheTime) < ttlMillis) {
            return cached;
        }
        String url = "https://nova-static.stellasora.global/meta/win.html";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", ua)
                    .header("X-Unity-Version", unity)
                    .header("Accept", "*/*")
                    .GET().build();
            HttpResponse<byte[]> rsp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (rsp.statusCode() == 200) {
                byte[] body = rsp.body();
                cache.put("win.html", body);
                cacheTime = now;
                Nebula.getLogger().info("Fetched win.html from official server");
                return body;
            } else {
                Nebula.getLogger().warn("Failed to fetch win.html: " + rsp.statusCode());
            }
        } catch (Exception e) {
            Nebula.getLogger().error("win.html proxy error", e);
        }
        return null;
    }

}
