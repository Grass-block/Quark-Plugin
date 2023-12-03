package org.tbstcraft.quark.pkg;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.BukkitUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface PackageManager {
    HashMap<String, PluginPackage> LOCAL_PACKAGE_STORAGE = new HashMap<>();

    static void scan(CommandSender sender) {
        String key;
        try {
            key = new String(Quark.class.getResourceAsStream("/package_key.dat").readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (File file : Objects.requireNonNull(new File(System.getProperty("user.dir") + "/plugins").listFiles())) {
            if (file.isDirectory()) {
                continue;
            }
            try {
                JarFile f = new JarFile(file);
                JarEntry entry = new JarEntry("package_key");
                InputStream stream = f.getInputStream(entry);
                if (stream == null) {
                    f.close();
                    continue;
                }
                String s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                if (!s.equals(key)) {
                    stream.close();
                    f.close();
                    continue;
                }
                stream.close();
                f.close();
                String name = BukkitUtil.getPluginDescription(file).getName();
                if (Bukkit.getPluginManager().isPluginEnabled(name)) {
                    continue;
                }
                Plugin p = Bukkit.getPluginManager().loadPlugin(file);
                if (p == null) {
                    continue;
                }
                Bukkit.getPluginManager().enablePlugin(p);
                Quark.LANGUAGE.sendMessageTo(sender, "package_import", name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void loadPackage(String className) {
        if (LOCAL_PACKAGE_STORAGE.containsKey(className)) {
            unloadPackage(className);
        }
        try {
            Class<?> clazz = Class.forName(className);
            PluginPackage p = ((PluginPackage) clazz.getDeclaredConstructor().newInstance());
            p.onEnable();
            LOCAL_PACKAGE_STORAGE.put(className, p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void unloadPackage(String className) {
        if (!LOCAL_PACKAGE_STORAGE.containsKey(className)) {
            return;
        }
        try {
            PluginPackage p = LOCAL_PACKAGE_STORAGE.get(className);
            p.onDisable();
            LOCAL_PACKAGE_STORAGE.remove(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
