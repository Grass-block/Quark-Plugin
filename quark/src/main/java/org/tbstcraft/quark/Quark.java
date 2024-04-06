package org.tbstcraft.quark;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.command.internal.InternalCommands;
import org.tbstcraft.quark.command.internal.core.QuarkPluginCommand;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.config.Queries;
import org.tbstcraft.quark.packages.InternalPackages;
import org.tbstcraft.quark.service.framework.PackageManager;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.text.TextBuilder;
import org.tbstcraft.quark.text.TextSender;
import org.tbstcraft.quark.util.DeferredLogger;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.Timer;
import org.tbstcraft.quark.util.api.APIProfileTest;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public final class Quark extends JavaPlugin {
    public static final String PLUGIN_ID = "quark";
    public static final ExecutorService SHARED_THREAD_POOL = Executors.newFixedThreadPool(4);
    public static final Properties METADATA = new Properties();

    //identifier
    public static final String CORE_ID = "tm5-34";
    public static final String TEXT_ENGINE_ID = "te3-02";

    //instances
    public static Language LANGUAGE;
    public static Configuration CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;

    private static boolean isCoreAvailable = false;

    private String instanceId;

    //brands
    public static String getTextLogo() {
        return "{color(purple)}QuarkPlugin {color(gray)} - {color(white)}v%s".formatted(getVersion());
    }

    public static String getLogo() {
        return """
                {color(purple)} ______   __  __   ______   ______   __  __
                {color(purple)}/\\  __ \\ /\\ \\/\\ \\ /\\  __ \\ /\\  == \\ /\\ \\/ /
                {color(purple)}\\ \\ \\/\\_\\\\ \\ \\_\\ \\\\ \\  __ \\\\ \\  __< \\ \\  _"-.
                {color(purple)} \\ \\___\\_\\\\ \\_____\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\\\ \\_\\ \\_\\
                {color(purple)}  \\/___/_/ \\/_____/ \\/_/\\/_/ \\/_/ /_/ \\/_/\\/_/       {color(white)}v%s
                """.formatted(getVersion());
    }

    public static String getVersion() {
        return Quark.PLUGIN.getDescription().getVersion();
    }


    //display
    public static void sendInfoDisplay(CommandSender sender) {
        String s = """
                {color(yellow)} ─────────────────────────────────
                {logo}
                 {color(gray)}A comprehensive Minecraft plugins based on Paper API.
                 {color(gray)}Artifact by GrassBlock2022, Published by @TBSTMC.
                  
                 {color(white)}Copyright @TBSTMC 2024.
                 
                   {color(gray)}Website: {color(aqua);underline;click(link,https://quark.tbstmc.xyz)}https://quark.tbstmc.xyz{none}
                   {color(gray)}Contact: {color(aqua)}tbstmc@163.com {color(gold);click(copy,tbstmc@163.com)}[Copy]{none}
                {color(yellow)} ─────────────────────────────────
                """;
        if (!(sender instanceof ConsoleCommandSender)) {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", getTextLogo())));
        } else {
            TextSender.sendBlock(sender, TextBuilder.build(s.replace("{logo}", getTextLogo())));
        }
    }

    public static void sendStatsDisplay(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        for (String s : PackageManager.getSubPacksFromServer()) {
            sb.append("{}   ").append("- ").append(s).append("\n");
        }
        String f = sb.toString();
        f = f.substring(0, f.length() - 1);

        TextSender.sendBlock(sender, TextBuilder.build("""
                {color(yellow)} ─────────────────────────────────
                统计信息:
                  {#white}已安装的模块: {#aqua}{#module_installed}
                  {#white}已启用的模块: {#aqua}{#module_enabled}
                  {#white}生成的玩家档案: {#aqua}{#player_data_count}
                  {#white}生成的模块档案: {#aqua}{#module_data_count}
                  {#white}已安装的子包:
                %s
                                
                  {#white}核心版本: {#aqua}{#quark_version}
                  {#white}框架版本: {#aqua}{#quark_framework_version}
                  {#white}文本引擎版本: {#aqua}{#quark_text_engine_version}
                  {#white}构建时间: {#aqua}{#build_time}
                {color(yellow)} ─────────────────────────────────
                """.formatted(f)));
    }


    //instance
    public static boolean isCoreUnavailable() {
        return !isCoreAvailable;
    }

    private void run(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, Comparator.comparingInt(m -> m.getAnnotation(ContextComponent.class).order()));

        for (Method m : methods) {
            try {
                m.invoke(null, this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            String hint = m.getAnnotation(ContextComponent.class).text();
            if (Objects.equals(hint, "")) {
                continue;
            }
            LOGGER.info(hint + ". (%d ms)".formatted(Timer.passedTime()));
        }
    }


    //plugins
    @Override
    public void onEnable() {
        this.instanceId = UUID.randomUUID().toString();
        try {
            Class.forName("org.tbstcraft.quark.util.Timer");
            Class.forName("org.tbstcraft.quark.Quark$BootOperations");
            Class.forName("org.tbstcraft.quark.Quark$ContextComponent");
            Class.forName("org.tbstcraft.quark.Quark$StopOperations");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Timer.restartTiming();
        run(BootOperations.class);
        isCoreAvailable = true;

        LOGGER.info("Initialization completed.(%d ms)".formatted(Timer.passedTime()));
    }

    @Override
    public void onDisable() {
        Timer.restartTiming();
        isCoreAvailable = false;
        InternalPackages.unregisterAll();
        run(StopOperations.class);

        LOGGER.info("Stop completed.(%d ms)".formatted(Timer.passedTime()));
    }

    public void updateDataFolder() {
        File f = new File(FilePath.pluginFolder("Quark"));
        File f2 = new File(FilePath.pluginFolder("quark"));
        if (!f.exists() || !f.isDirectory()) {
            return;
        }
        LOGGER.info("find legacy folder. rename it to newer plugin id.");
        if (!f.renameTo(f2)) {
            LOGGER.warning("failed to update data folder. consider manually rename your folder (Quark->quark)");
        }
    }

    public String getInstanceId() {
        return this.instanceId;
    }


    interface BootOperations {
        @ContextComponent(order = 0)
        static void guiding(Quark instance) throws Exception {
            Class.forName("org.tbstcraft.quark.util.api.APIProfile");
            Class.forName("org.tbstcraft.quark.text.TextSender");
            Class.forName("org.tbstcraft.quark.text.TextBuilder");
        }

        @ContextComponent(order = 1, text = "Bootstrap initialization completed")
        static void bootstrap(Quark instance) throws Exception {
            PLUGIN = instance;
            Quark.LOGGER = new DeferredLogger(instance.getLogger());
            METADATA.load(instance.getClass().getResourceAsStream("/metadata.properties"));

            Service.initBase();
            APIProfileTest.test();
            TextSender.initContext();
        }

        @ContextComponent(order = 2)
        static void startupDisplay(Quark instance) {
            LOGGER.info("detected server platform: " + APIProfileTest.getAPIProfile().toString());

            TextSender.sendToConsole(TextBuilder.build("""
                    %s {color(yellow)}------------------------------------------------------------
                      {color(gray)}Artifact by {color(white)}GrassBlock2022, {color(gray)}Copyright {color(white)}[C]TBSTMC 2024.
                    """.formatted(getLogo())));
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
            if (!APIProfileTest.isArclightBasedServer()) {
               // return;
            }
            LOGGER.info("detected mohist/arclight platform, loading full jar...");
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
            LANGUAGE = Language.create("quark-core");
            CONFIG = new Configuration("quark-core");
            Queries.reloadExternal();
        }

        @ContextComponent(order = 5, text = "Service started")
        static void startService(Quark instance) {
            Service.init();
            Queries.initialize();
            QuarkPluginCommand.ReloadCommand.ReloadTask.initLoaders();
        }

        @ContextComponent(order = 6, text = "Internal content registered")
        static void internalContent(Quark instance) {
            InternalCommands.register();
            InternalPackages.registerAll();
        }
    }

    interface StopOperations {
        @ContextComponent(order = 0, text = "Internal contents unregistered.")
        static void unregisterAllPackages(Quark instance) {
            InternalPackages.unregisterAll();
            InternalCommands.unregister();
        }

        @ContextComponent(order = 1, text = "Service stopped.")
        static void stopService(Quark instance) {
            Service.stop();
            Service.stopBase();
        }

        @ContextComponent(order = 2, text = "Context destroyed")
        static void destroyContext(Quark instance) {
            PLUGIN = null;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ContextComponent {
        int order();

        String text() default "";
    }
}
