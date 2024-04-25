package org.tbstcraft.quark.display;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CommandRegistry;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.framework.config.Queries;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.compat.Compat;
import org.tbstcraft.quark.framework.module.compat.CompatContainer;
import org.tbstcraft.quark.framework.module.compat.CompatDelegate;
import org.tbstcraft.quark.framework.text.ComponentBlock;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.util.api.APIProfile;
import org.tbstcraft.quark.util.api.APIProfileTest;

import java.io.File;

@SuppressWarnings("deprecation")
@EventListener
@QuarkModule(version = "1.0.2")
@CommandRegistry({CustomMotd.MotdCommand.class})
@Compat(CustomMotd.PaperCompat.class)
public final class CustomMotd extends PackageModule {
    private CachedServerIcon cachedServerIcon;

    @Override
    public void enable() {
        File iconFile = new File(Quark.PLUGIN.getDataFolder().getAbsolutePath() + "/motd.png");
        if (!iconFile.exists()) {
            return;
        }
        try {
            this.cachedServerIcon = Bukkit.loadServerIcon(iconFile);
        } catch (Exception e) {
            this.getLogger().severe("failed to load server icon. please consider reload this module when fixed.");
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

    @QuarkCommand(name = "motd")
    public static final class MotdCommand extends ModuleCommand<CustomMotd> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getLanguage().sendMessageTo(sender, "motd-command");
            getModule().generateMotdMessage().send(sender);
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