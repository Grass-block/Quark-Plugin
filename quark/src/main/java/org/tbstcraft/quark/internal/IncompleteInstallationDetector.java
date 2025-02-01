package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.language.LocaleMapping;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.ProductInfo;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.Registers;

@QuarkModule(internal = true)
@AutoRegister(Registers.BUKKIT_EVENT)
public final class IncompleteInstallationDetector extends PackageModule {
    public static final String WARN_EN = """
            There is no plugin installed which requires quark-plugin.
            This should never happen because quark.jar won't provide any feature.
            Please go back to the download page and pick other packs:
                https://modrinth.com/plugin/quark-plugin/version/%s
            or to install something that requires quark-plugin.
            """;

    public static final String WARN_ZH = """
            您的服务端环境中没有依赖 quark-plugin 的插件。
            这是错误的，因为 quark.jar 实际上不会提供任何功能。
            请回到下载版本页面并按照说明选择需要的扩展包并安装:
                https://modrinth.com/plugin/quark-plugin/version/%s
            或者装一些您需要的依赖 quark-plugin 的其他插件.
            """;


    private boolean hasWorkload = false;

    @Override
    public void enable() {
        for (var plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getDepend().contains(Quark.PLUGIN_ID)) {
                this.hasWorkload = true;
                break;
            }
        }

        if (!this.hasWorkload) {
            for (var file : PluginUtil.getAllPluginFiles()) {
                try {
                    var desc = PluginUtil.getPluginDescription(file);

                    if (desc.getDepend().contains(Quark.PLUGIN_ID)) {
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
            Quark.LOGGER.error(line);
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

        if (LocaleMapping.minecraft(LocaleService.locale(sender)).contains("zh")) {
            template = WARN_ZH;
        } else {
            template = WARN_EN;
        }

        return String.format(template, ProductInfo.version());
    }
}
