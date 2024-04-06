package org.tbstcraft.quark.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    static JsonObject packageDescriptor(String id) {
        try {
            JsonObject obj;
            InputStream stream = getPluginResource("/packages/%s.json".formatted(id));
            if (stream == null) {
                Quark.LOGGER.warning("failed to load package descriptor.");
                return null;
            }
            obj = ((JsonObject) new JsonParser().parse(new InputStreamReader(stream)));
            stream.close();
            return obj;
        } catch (IOException e) {
            Quark.LOGGER.warning("failed to load package descriptor of %s: %s".formatted(id, e.getMessage()));
            Quark.LOGGER.severe(e.getMessage());
        }
        return null;
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

    static String recordFolder(String plugin) {
        plugin = Objects.equals(plugin, "quark") ? "Quark" : plugin;
        return pluginFolder(plugin) + "/record";
    }

    static String moduleData(String plugin) {
        plugin = Objects.equals(plugin, "quark") ? "Quark" : plugin;
        return pluginFolder(plugin) + "/module_data";
    }

    static String playerData(String plugin) {
        plugin = Objects.equals(plugin, "quark") ? "Quark" : plugin;
        return pluginFolder(plugin) + "/player_data";
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

    static void cover(File target, InputStream stream) {
        if (stream == null) {
            Quark.LOGGER.severe("null source!");
            return;
        }
        try {
            if (target.getParentFile().mkdirs()) {
                Quark.LOGGER.info("created folder of file: " + target.getName());
            }
            if (target.createNewFile()) {
                Quark.LOGGER.info("created file:" + target.getName());
            }
            FileOutputStream out = new FileOutputStream(target);
            out.write(stream.readAllBytes());
            stream.close();
            out.close();
        } catch (IOException e) {
            Quark.LOGGER.severe("failed to save resource(dest: %s): %s".formatted(target.getName(), e.getMessage()));
        }
    }
}
