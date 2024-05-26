package org.tbstcraft.quark.display;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.assets.Asset;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.data.config.Queries;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.text.ComponentBlock;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.tbstcraft.quark.util.platform.APIProfile;
import org.tbstcraft.quark.util.platform.APIProfileTest;

import java.io.File;
import java.util.List;

@QuarkModule(version = "1.0.2")
@CommandProvider({CustomMotd.MotdCommand.class})
@Compat(CustomMotd.PaperCompat.class)
@ModuleService(ServiceType.EVENT_LISTEN)
public final class CustomMotd extends PackageModule {
    private CachedServerIcon cachedServerIcon;

    private Asset motdIcon;

    @Override
    public void enable() {
        this.motdIcon = new Asset(this.getOwnerPlugin(), "motd.png", false);
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
        if (APIProfileTest.isPaperCompat()) {
            return;
        }
        e.setMotd(generateMotdMessage().toString());

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

    @QuarkCommand(name = "motd",permission = "-quark.motd.command")
    public static final class MotdCommand extends ModuleCommand<CustomMotd> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "refresh-icon" -> {
                    this.getModule().refreshIcon();
                    this.getLanguage().sendMessageTo(sender, "icon-refresh");
                }
                case "text" -> {
                    this.getLanguage().sendMessageTo(sender, "motd-command");
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

    @CompatDelegate(APIProfile.PAPER)
    public static final class PaperCompat extends CompatContainer<CustomMotd> {
        public PaperCompat(CustomMotd parent) {
            super(parent);
        }

        @EventHandler
        public void onPing(PaperServerListPingEvent e) {
            e.motd(this.getParent().generateMotdMessage().toSingleLine());
            if (this.getParent().cachedServerIcon == null) {
                return;
            }
            e.setServerIcon(this.getParent().cachedServerIcon);
        }
    }
}