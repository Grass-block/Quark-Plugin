package org.tbstcraft.quark.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tbstcraft.quark.Quark;

import java.io.*;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public interface FilePath {
    File PLUGIN_FOLDER = new File(System.getProperty("user.dir") + "/plugins");
    Logger LOGGER = Quark.PLUGIN.getLogger();

    static File tryReleaseAndGetFile(String src, String dest) {
        File f = new File(dest);
        if (f.exists() && f.length() != 0) {
            return f;
        }
        coverFile(src, dest);
        return f;
    }

    static String pluginFolder() {
        return System.getProperty("user.dir") + "/plugins/Quark";
    }

    static JsonObject packageDescriptor(String packageId) {
        try {
            JsonObject obj;
            InputStream stream = getResourceInPlugins("/%s.package.json".formatted(packageId));
            if (stream == null) {
                LOGGER.warning("failed to load package descriptor.");
                return null;
            }
            obj = ((JsonObject) new JsonParser().parse(new InputStreamReader(stream)));
            stream.close();
            return obj;
        } catch (IOException e) {
            LOGGER.warning("failed to load package descriptor:");
            LOGGER.severe(e.getMessage());
        }
        return null;
    }

    static File languageFile(String packageId, String locale) {
        String fileDir = "%s/lang/%s/%s.yml".formatted(pluginFolder(), BukkitUtil.fixLocaleId(locale), packageId);
        String srcDir = "/lang/%s.%s.yml".formatted(packageId, BukkitUtil.fixLocaleId(locale));
        return tryReleaseAndGetFile(srcDir, fileDir);
    }

    static void coverLanguageFile(String packageId, String locale) {
        String fileDir = "%s/lang/%s/%s.yml".formatted(pluginFolder(), BukkitUtil.fixLocaleId(locale), packageId);
        String srcDir = "/lang/%s.%s.yml".formatted(packageId, BukkitUtil.fixLocaleId(locale));
        coverFile(srcDir, fileDir);
    }

    static void coverFile(String srcDir, String fileDir) {
        File f = new File(fileDir);
        if (!f.getParentFile().mkdirs()) {
            LOGGER.info("failed to create folder.");
        }
        try {
            if (!f.createNewFile()) {
                LOGGER.info("failed to create file.");
            }
            InputStream is = getResourceInPlugins(srcDir);
            OutputStream s = new FileOutputStream(f);
            if (is == null) {
                if (!f.delete()) {
                    LOGGER.warning("failed to delete release-failed file.");
                }
                return;
            }
            s.write(is.readAllBytes());
            s.close();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage() + "(src: %s,dest: %s)".formatted(srcDir, fileDir));
        }
    }

    static void coverConfigFile(String moduleId) {
        String fileDir = "%s/config/%s.yml".formatted(pluginFolder(), moduleId);
        String srcDir = "/%s.yml".formatted(moduleId);
        coverFile(srcDir, fileDir);
    }

    static File configFile(String moduleId) {
        String fileDir = "%s/config/%s.yml".formatted(pluginFolder(), moduleId);
        String srcDir = "/%s.yml".formatted(moduleId);
        return tryReleaseAndGetFile(srcDir, fileDir);
    }

    static File data(String file) {
        File f = new File(file);
        if (!f.exists()) {
            if (f.getParentFile().mkdirs()) {
                LOGGER.severe("failed to create data folder.");
            }
            try {
                if (f.createNewFile()) {
                    LOGGER.severe("failed to create data file.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }

    static InputStream getResourceInPlugins(String path) {
        String fixedPath = path.replaceFirst("/", "");
        for (File f : Objects.requireNonNull(PLUGIN_FOLDER.listFiles())) {
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

    static File moduleData() {
        return new File(pluginFolder() + "/module_data");
    }

    static File playerData() {
        return new File(pluginFolder() + "/player_data");
    }
}
