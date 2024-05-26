package org.tbstcraft.quark.util;

import org.tbstcraft.quark.Quark;

import java.io.*;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public interface FilePath {

    static File tryReleaseAndGetFile(String src, String dest) {
        File f = new File(dest);
        if (f.exists() && f.length() != 0) {
            return f;
        }
        coverFile(src, dest);
        return f;
    }

    static void coverFile(String srcDir, String fileDir) {
        File f = new File(fileDir);
        if (f.getParentFile().mkdirs()) {
            Quark.LOGGER.info("created folder of file: " + fileDir);
        }
        try {
            InputStream is = getPluginResource(srcDir);
            OutputStream s = new FileOutputStream(f);
            if (is == null) {
                if (f.createNewFile()) {
                    Quark.LOGGER.info("created file:" + fileDir);
                }
                return;
            }
            s.write(is.readAllBytes());
            s.close();
        } catch (Exception e) {
            Quark.LOGGER.severe("failed to save resource(src: %s,dest: %s): %s".formatted(srcDir, fileDir, e.getMessage()));
        }
    }

    static String server() {
        return System.getProperty("user.dir");
    }

    static String pluginsFolder() {
        return server() + "/plugins";
    }

    static String pluginFolder(String plugin) {
        plugin = Objects.equals(plugin, "quark") ? "Quark" : plugin;
        return server() + "/plugins/" + plugin;
    }

    static InputStream getPluginResource(String path) {
        String fixedPath = path.replaceFirst("/", "");

        File folder = new File(pluginsFolder());

        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (!f.getName().endsWith(".jar")) {
                continue;
            }
            try {
                JarFile jf = new JarFile(f);
                ZipEntry entry = jf.getEntry(fixedPath);
                if (entry == null) {
                    jf.close();
                    continue;
                }
                return jf.getInputStream(entry);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
