package org.atcraftmc.starlight.internal;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.ProductInfo;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.platform.PluginUtil;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.framework.packages.PackageManager;

import java.util.List;

@Components({InstallationCheck.Incomplete.class, InstallationCheck.Unregistered.class, InstallationCheck.CounterConflicts.class})
@SLModule(internal = true)
public final class InstallationCheck extends PackageModule {

    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class Incomplete extends ModuleComponent<InstallationCheck> {
        public static final String WARN_EN = """
                There is no plugin installed which requires starlight-core.
                This should never happen because starlight-core.jar won't provide any feature.
                Please go back to the download page and pick other packs:
                    https://modrinth.com/plugin/starlight-plugin/version/%s
                or to install something that requires starlight-core.
                """;

        public static final String WARN_ZH = """
                您的服务端环境中没有依赖 starlight-core 的插件。
                这是错误的，因为 starlight-core.jar 实际上不会提供任何功能。
                请回到下载版本页面并按照说明选择需要的扩展包并安装:
                    https://modrinth.com/plugin/starlight-plugin/version/%s
                或者装一些您需要的依赖 starlight-core 的其他插件.
                """;


        private boolean hasWorkload = false;

        @Override
        public void enable() {
            for (var plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getDescription().getDepend().contains(ProductInfo.CORE_ID)) {
                    this.hasWorkload = true;
                    break;
                }
            }

            if (!this.hasWorkload) {
                for (var file : PluginUtil.getAllPluginFiles()) {
                    try {
                        var desc = PluginUtil.getPluginDescription(file);

                        if (desc.getDepend().contains(ProductInfo.CORE_ID)) {
                            this.hasWorkload = true;
                            break;
                        }
                    } catch (Throwable e) {
                        continue;
                    }
                }
            }

            if (this.hasWorkload) {
                return;
            }

            var sender = Bukkit.getConsoleSender();

            for (var line : warn(sender).split("\n")) {
                Starlight.LOGGER.error(line);
            }

            for (var player : Bukkit.getOnlinePlayers()) {
                warnPlayer(player);
            }
        }


        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            warnPlayer(event.getPlayer());
        }

        private void warnPlayer(Player p) {
            if (!p.isOp()) {
                return;
            }
            if (this.hasWorkload) {
                return;
            }

            p.sendMessage(ChatColor.YELLOW + "一一一一一一一一一一一一一一一一一一一一一一一一一一一");

            for (var line : warn(p).split("\n")) {
                p.sendMessage(ChatColor.RED + line);
            }

            p.sendMessage(ChatColor.YELLOW + "一一一一一一一一一一一一一一一一一一一一一一一一一一一");
        }

        private String warn(CommandSender sender) {
            String template;

            if (LocaleService.locale(sender).minecraft().contains("zh")) {
                template = WARN_ZH;
            } else {
                template = WARN_EN;
            }

            return String.format(template, ProductInfo.version());
        }
    }

    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class Unregistered extends ModuleComponent<InstallationCheck> {
        private static final String WARN_OP = """
                {#red} 您管理的服务器正在使用未经验证的 {#purple}Quark-Plugin{#red} 插件实例。
                {#red} 请注意盗版软件可能导致的后门和数据泄露等风险。
                {#red} 我们不对您当前所使用的的该版本负责。
                """;

        private static final String WARN_PLAYER = """
                {#red} 您正在游玩的服务器使用了未授权的 {#purple}Quark-Plugin{#red} 插件实例。
                {#red} 请注意可能的隐私泄露和数据安全风险。
                {#red} 我们不对该服务器当前所使用的的该版本负责。
                """;

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            if (ProductService.isActivated()) {
                return;
            }
            if (event.getPlayer().isOp()) {
                ComponentBlock block = TextBuilder.build(WARN_OP);
                TextSender.sendBlock(event.getPlayer(), block);
            } else {
                ComponentBlock block = TextBuilder.build(WARN_PLAYER);
                TextSender.sendBlock(event.getPlayer(), block);
            }
        }
    }

    public static final class CounterConflicts extends ModuleComponent<InstallationCheck> {
        public static final String MAIN_CLASS = "org.kyoikumi.plugin.counter.Counter";
        public static final String PLUGIN_ID = "Counter";
        public static final List<String> CONFLICT_LIST = List.of("quark-display", "quark-chat");

        @Inject
        public Logger logger;

        @Override
        public void enable() {
            Plugin counter = Bukkit.getPluginManager().getPlugin(PLUGIN_ID);

            if (counter == null) {
                return;
            }
            if (!counter.getClass().getName().equals(MAIN_CLASS)) {
                return;
            }

            this.logger.warn("detected 'counter' plugin by 'org.kyoikumi', this may cause conflict.");
            this.logger.warn("we WON'T fix any problem of duplicated function.");

            for (String s : CONFLICT_LIST) {
                this.logger.warn("rejected local package %s.".formatted(s));
                PackageManager.addRejection(s);
            }
        }
    }
}
