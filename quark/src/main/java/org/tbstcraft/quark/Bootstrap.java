package org.tbstcraft.quark;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.backend.MessageBackend;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.util.DeferredLogger;
import org.tbstcraft.quark.util.Timer;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface Bootstrap {
    static void run(Class<?> clazz, Quark context) {
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, Comparator.comparingInt(m -> m.getAnnotation(ContextComponent.class).order()));

        for (Method m : methods) {
            try {
                m.invoke(null, context);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            String hint = m.getAnnotation(ContextComponent.class).text();
            if (Objects.equals(hint, "")) {
                continue;
            }
            Quark.LOGGER.info(hint + ". (%d ms)".formatted(Timer.passedTime()));
        }
    }

    interface BootOperations {
        @ContextComponent(order = 0)
        static void guiding(Quark instance) throws Exception {
            Class.forName("org.tbstcraft.quark.foundation.platform.APIProfile");
            Class.forName("org.tbstcraft.quark.foundation.text.TextSender");
            Class.forName("org.tbstcraft.quark.foundation.text.TextBuilder");
        }

        @ContextComponent(order = 1, text = "Bootstrap initialization completed")
        static void bootstrap(Quark instance) throws Exception {
            Quark.LOGGER = new DeferredLogger(instance.getLogger());
            ProductInfo.METADATA.load(instance.getClass().getResourceAsStream("/product-info.properties"));

            APIProfileTest.test();
            TextSender.initContext();
        }

        @ContextComponent(order = 2)
        static void startupDisplay(Quark instance) {
            Quark.LOGGER.info("detected server platform: " + APIProfileTest.getAPIProfile().toString());

            TextSender.sendToConsole(TextBuilder.build(ProductInfo.logo()));
            if (APIProfileTest.isFoliaServer()) {
                TextSender.sendToConsole(TextBuilder.build("""
                        {color(red)}Quark核心检测到您正在使用Folia服务端!{color(white)}
                          Folia兼容已自动启用。我们不保证任何功能的可用!
                        {color(red)}Quark core detected you are using Folia Server!{color(white)}
                          Folia compat are automatically enabled.We do NOT ensure any feature's availability!
                        """));
            }
            if (APIProfileTest.isArclightBasedServer()) {
                TextSender.sendToConsole(TextBuilder.build("""
                        {color(red)}Quark核心检测到Arclight/Mohist服务端, 自动更新系统将不可用。若您需要更新，请重启。
                        {color(red)}Quark core detected Arclight/Mohist server, auto-update system will be unavailable. Please RESTART if you want to update!
                        """));
            }

            ((DeferredLogger) Quark.LOGGER).batch();
            Quark.LOGGER = instance.getLogger();
        }

        @ContextComponent(order = 3, text = "Full JAR loaded")
        static void loadFullJar(Quark instance) {
            if (instance.isFastBoot()) {
                if (APIProfileTest.isArclightBasedServer()) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "检测到Mohist/ArcLight平台，无法启用快速启动(FastBoot)");
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "快速启动(FastBoot)已开启。热重载功能将失效");
                    return;
                }
            }

            Quark.LOGGER.info("disabled fastboot, loading full jar...");
            try (JarFile jarFile = new JarFile(new File(System.getProperty("user.dir") + "/plugins/quark.jar"))) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) {
                        continue;
                    }
                    String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                    if (className.contains("internal")) {
                        continue;
                    }
                    if (className.contains("META-INF")) {
                        continue;
                    }
                    try {
                        Quark.class.getClassLoader().loadClass(className);
                    } catch (Throwable e) {
                        instance.getLogger().info("failed to load class: " + className);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @ContextComponent(order = 4, text = "Configuration loaded")
        static void configuration(Quark instance) {
            InputStream templateResource = Objects.requireNonNull(instance.getClass().getResourceAsStream("/config.yml"));
            YamlConfiguration template = YamlConfiguration.loadConfiguration(new InputStreamReader(templateResource));

            YamlUtil.update(instance.getConfig(), template, false, 3);

            instance.saveConfig();
            Queries.setEnvironmentVars(Objects.requireNonNull(instance.getConfig().getConfigurationSection("config.environment")));

            Quark.CONFIG = new Configuration("quark-core");
        }

        @ContextComponent(order = 5, text = "Service started")
        static void startService(Quark instance) {
            Service.initBase();
            QuarkInternalPackage.register(PackageManager.INSTANCE.get());

            ClientMessenger.setBackend(MessageBackend.bukkit(Quark.PLUGIN));
            ClientMessenger.getBackend().start();
        }
    }

    interface StopOperations {
        @ContextComponent(order = 0, text = "Service stopped.")
        static void stopService(Quark instance) {
            ServiceManager.unregisterAll();
            Service.stopBase();
            ClientMessenger.getBackend().stop();
        }

        @ContextComponent(order = 1, text = "Context destroyed")
        static void destroyContext(Quark instance) {
            Quark.PLUGIN = null;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ContextComponent {
        int order();

        String text() default "";
    }
}
