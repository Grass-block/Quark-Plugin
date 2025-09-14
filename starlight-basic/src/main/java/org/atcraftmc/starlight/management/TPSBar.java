package org.atcraftmc.starlight.management;

import me.gb2022.commons.math.MathHelper;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.util.TemplateExpansion;

@SLModule
@QuarkCommand(name = "tpsbar", permission = "-starlight.monitor.tpsbar")
public final class TPSBar extends CommandModule {
    public static final TemplateExpansion TEMPLATE_EXPANSION = TemplateExpansion.build((b) -> b.replacement("tps").replacement("mspt"));

    private final BossBar bar = Bukkit.createBossBar("_tpsbar", BarColor.PURPLE, BarStyle.SEGMENTED_20);

    @Override
    public void enable() throws Exception {
        super.enable();

        TaskService.async().timer("tpsbar:update", 5, 5, () -> {
            var tps = BukkitUtil.formatTPS(BukkitUtil.getTPS());
            var msptValue = BukkitUtil.getMSPT();
            var mspt = BukkitUtil.formatMSPT(msptValue);

            var line = TEMPLATE_EXPANSION.expand(getConfig().value("line").string(), tps, mspt);

            if (!this.bar.getPlayers().isEmpty()) {
                this.bar.setTitle(PluginPlatform.global().globalFormatMessage(line));
                if (msptValue < 15) {
                    this.bar.setColor(BarColor.GREEN);
                } else if (msptValue < 35) {
                    this.bar.setColor(BarColor.YELLOW);
                } else {
                    this.bar.setColor(BarColor.RED);
                }
                this.bar.setProgress(MathHelper.clamp(msptValue / 50f, 0, 1));
            }
        });
    }

    @Override
    public void disable() throws Exception {
        super.disable();
        TaskService.async().cancel("tpsbar:update");

        this.bar.removeAll();
    }

    @Override
    public void execute(CommandExecution context) {
        var player = context.requireSenderAsPlayer();

        if (this.bar.getPlayers().contains(player)) {
            this.bar.removePlayer(player);
            MessageAccessor.send(this.getLanguage(), player, "false");
            return;
        }

        this.bar.addPlayer(player);
        MessageAccessor.send(this.getLanguage(), player, "true");
    }
}
