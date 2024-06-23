package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.api.QueryPingEvent;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.assets.Asset;
import org.tbstcraft.quark.data.config.Language;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.foundation.text.ComponentBlock;
import org.tbstcraft.quark.foundation.text.TextBuilder;

import java.io.File;
import java.util.List;

@QuarkModule(version = "1.0.2")
@CommandProvider({CustomMotd.MotdCommand.class})
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CustomMotd extends PackageModule {
    private CachedServerIcon cachedServerIcon;

    @Inject("motd.png;false")
    private Asset motdIcon;

    @Override
    public void enable() {
        this.motdIcon.getFile();

        File iconFile = new File(Quark.PLUGIN.getDataFolder().getAbsolutePath() + "/motd.png");
        if (!iconFile.exists()) {
            return;
        }

        this.refreshIcon();
    }

    public void refreshIcon() {
        try {
            this.cachedServerIcon = Bukkit.loadServerIcon(this.motdIcon.getFile());
        } catch (Exception e) {
            this.getLogger().severe("failed to load server icon. please consider refresh icon when fixed.");
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        e.setMotd(generateMotdMessage().toString());

        if (this.cachedServerIcon == null) {
            return;
        }
        e.setServerIcon(this.cachedServerIcon);
    }

    @EventHandler
    public void onPing(QueryPingEvent e) {
        e.setMotd(generateMotdMessage().toPlainTextString());

        if (this.cachedServerIcon == null) {
            return;
        }
        e.setServerIcon(this.cachedServerIcon);
    }

    private ComponentBlock generateMotdMessage() {
        ConfigurationSection cfg = this.getConfig();
        String title = cfg.getString("motd-title");
        String subtitle = cfg.getString("motd-subtitle");
        String raw = Queries.GLOBAL_TEMPLATE_ENGINE.handle(Language.handleReplacement(title + "\n" + subtitle, cfg, "cfg"));
        return TextBuilder.build(raw);
    }

    @QuarkCommand(name = "motd", permission = "-quark.motd.command")
    public static final class MotdCommand extends ModuleCommand<CustomMotd> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "refresh-icon" -> {
                    this.getModule().refreshIcon();
                    this.getLanguage().sendMessage(sender, "icon-refresh");
                }
                case "text" -> {
                    this.getLanguage().sendMessage(sender, "motd-command");
                    getModule().generateMotdMessage().send(sender);
                }
                default -> this.sendExceptionMessage(sender);
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("refresh-icon");
                tabList.add("text");
            }
        }
    }
}