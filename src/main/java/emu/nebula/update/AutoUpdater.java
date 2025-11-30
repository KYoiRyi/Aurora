package emu.nebula.update;

import emu.nebula.Nebula;
import emu.nebula.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class AutoUpdater {
    private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static void runIfEnabled() {
        Config cfg = Nebula.getConfig();
        if (!cfg.isAutoUpdate()) return;
        try {
            perform(cfg);
        } catch (Exception e) {
            Nebula.getLogger().error("Auto update failed", e);
        }
    }

    private static void perform(Config cfg) throws Exception {
        Path resDir = Paths.get(cfg.getResourceDir());
        Files.createDirectories(resDir);
        Path lockFile = resDir.resolve(".update.lock");
        try (FileChannel ch = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            FileLock lock = ch.tryLock();
            if (lock == null) {
                Nebula.getLogger().warn("Auto update locked");
                return;
            }
            try {
                String regionFolder = regionFolder(cfg.getRegion());
                Path tempBin = resDir.resolve("bin.new-" + System.currentTimeMillis());
                Files.createDirectories(tempBin);
                boolean staged = stageBinFromClone(cfg, tempBin);
                if (!staged) {
                    String zipUrl = toZipballUrl(cfg.getUpdateUrl());
                    if (zipUrl == null || zipUrl.isBlank()) {
                        Nebula.getLogger().warn("Auto update: invalid updateUrl");
                        return;
                    }
                    Nebula.getLogger().info("Auto update: downloading " + zipUrl);
                    byte[] zipBytes = download(zipUrl, 20);
                    if (zipBytes == null || zipBytes.length == 0) {
                        Nebula.getLogger().warn("Auto update: empty download");
                        return;
                    }
                    extractBin(zipBytes, regionFolder, tempBin);
                }
                if (!validateBinDir(tempBin)) {
                    deleteRecursive(tempBin);
                    Nebula.getLogger().warn("Auto update: validation failed");
                    return;
                }
                Path binDir = resDir.resolve("bin");
                Path backup = resDir.resolve("bin.backup-" + System.currentTimeMillis());
                if (Files.exists(binDir)) {
                    safeMove(binDir, backup);
                }
                safeMove(tempBin, binDir);
                deleteRecursive(backup);
                Nebula.getLogger().info("Auto update: bin updated");
                if (cfg.isOfficialDataFetch()) {
                    updateVersionsFromWinHtml(cfg);
                }
                Nebula.saveConfig();
            } finally {
                try { lock.release(); } catch (Exception ignored) {}
            }
        }
    }

    private static String toZipballUrl(String repoUrl) {
        if (repoUrl == null) return null;
        String url = repoUrl.trim();
        if (url.endsWith(".git")) url = url.substring(0, url.length() - 4);
        if (url.startsWith("https://github.com/")) {
            String rest = url.substring("https://github.com/".length());
            int slash = rest.indexOf('/');
            if (slash <= 0) return null;
            String owner = rest.substring(0, slash);
            String repo = rest.substring(slash + 1);
            if (repo.isEmpty()) return null;
            if (repo.contains("/")) repo = repo.substring(0, repo.indexOf('/'));
            return "https://codeload.github.com/" + owner + "/" + repo + "/zip/refs/heads/main";
        }
        return null;
    }

    private static boolean stageBinFromClone(Config cfg, Path tempBin) {
        try {
            String url = cfg.getUpdateUrl();
            if (url == null || url.isBlank()) return false;
            Path work = tempBin.getParent().resolve(".clone-" + System.currentTimeMillis());
            Files.createDirectories(work);
            ProcessBuilder pb = new ProcessBuilder("git", "clone", "--depth=1", "--branch", "main", url, work.toString());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean finished = p.waitFor(180, TimeUnit.SECONDS);
            if (!finished || p.exitValue() != 0) {
                deleteRecursive(work);
                return false;
            }
            Path src = work.resolve(regionFolder(cfg.getRegion())).resolve("bin");
            if (!Files.isDirectory(src)) {
                deleteRecursive(work);
                return false;
            }
            copyDir(src, tempBin);
            deleteRecursive(work);
            Nebula.getLogger().info("Auto update: staged bin via git clone");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void copyDir(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(sp -> {
            try {
                Path rel = src.relativize(sp);
                Path target = dest.resolve(rel);
                if (Files.isDirectory(sp)) {
                    Files.createDirectories(target);
                } else {
                    Path parent = target.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    Files.copy(sp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ignored) {}
        });
    }

    private static byte[] download(String url, int timeoutSeconds) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(timeoutSeconds)).header("Accept", "*/*").GET().build();
        HttpResponse<byte[]> rsp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (rsp.statusCode() != 200) return null;
        return rsp.body();
    }

    private static String regionFolder(String region) {
        if (region == null) return "EN";
        String r = region.trim().toLowerCase();
        if (r.equals("global") || r.equals("en") || r.equals("english")) return "EN";
        if (r.equals("jp") || r.equals("japan") || r.equals("ja")) return "JP";
        return "EN";
    }

    private static void extractBin(byte[] zipBytes, String regionFolder, Path destBin) throws IOException {
        String marker = "/" + regionFolder.toLowerCase() + "/bin/";
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                String lower = name.toLowerCase();
                int idx = lower.indexOf(marker);
                if (idx < 0) continue;
                String rel = name.substring(idx + marker.length());
                if (rel.isEmpty()) continue;
                Path out = destBin.resolve(rel).normalize();
                if (!out.startsWith(destBin)) continue;
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Path parent = out.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    byte[] buffer = new byte[8192];
                    try (var os = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                        int read;
                        while ((read = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, read);
                        }
                    }
                }
            }
        }
    }

    private static boolean validateBinDir(Path binDir) throws IOException {
        if (!Files.isDirectory(binDir)) return false;
        Path character = binDir.resolve("Character.json");
        if (!Files.isRegularFile(character)) return false;
        long size = Files.size(character);
        return size > 0;
    }

    private static void safeMove(Path src, Path dst) throws IOException {
        try {
            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(src, dst);
        }
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (path == null) return;
        if (!Files.exists(path)) return;
        Files.walk(path).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
            try { Files.deleteIfExists(p); } catch (IOException ignored) {}
        });
    }

    private static void updateVersionsFromWinHtml(Config cfg) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://nova-static.stellasora.global/meta/win.html"))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "UnityPlayer")
                    .header("X-Unity-Version", "2021.3")
                    .header("Accept", "*/*")
                    .GET().build();
            HttpResponse<byte[]> rsp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (rsp.statusCode() != 200) return;
            String html = new String(rsp.body());
            String gamever = extractGamever(html);
            Integer ver = extractVer(html);
            if (gamever != null && !gamever.isEmpty()) cfg.gameVersion = gamever;
            if (ver != null && ver > 0) cfg.customDataVersion = ver;
            Nebula.getLogger().info("Auto update: version set gamever=" + cfg.getGameVersion() + " ver=" + cfg.getCustomDataVersion());
        } catch (Exception e) {
            Nebula.getLogger().warn("Auto update: win.html fetch failed");
        }
    }

    private static String extractGamever(String html) {
        Pattern p = Pattern.compile("gamever\\W*[:=]\\W*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        return null;
    }

    private static Integer extractVer(String html) {
        Pattern p = Pattern.compile("\\bver\\W*[:=]\\W*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }
}
