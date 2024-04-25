package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.command.CommandRegistry;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.config.Queries;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.service.task.TaskService;
import org.tbstcraft.quark.util.api.PlayerUtil;
import me.gb2022.commons.nbt.NBTTagCompound;

import java.util.*;

@EventListener
@CommandRegistry(BossbarAnnouncement.BossbarAnnounceCommand.class)
@QuarkModule(version = "1.1.0")
public final class BossbarAnnouncement extends PackageModule {
    public static final String TASK_UPDATE_TID = "quark-display:custom_bossbar:update";
    private final HashMap<String, BossBar> bars = new HashMap<>();
    private String content = null;

    @Override
    public void enable() {
        ConfigurationSection config = this.getConfig();

        NBTTagCompound tag = ModuleDataService.getEntry(this.getId());
        if (tag.hasKey("custom")) {
            this.content = tag.getString("custom");
        }
        TaskService.asyncTimerTask(TASK_UPDATE_TID, 0, config.getInt("period"), this::updateBossbar);

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.addBar(player);
        }
    }

    @Override
    public void disable() {
        TaskService.cancelTask(TASK_UPDATE_TID);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.removeBar(player);
        }
    }


    public void setContent(String content) {
        this.content = content;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.addBar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.removeBar(event.getPlayer());
    }

    public void addBar(Player p) {
        String locale = PlayerUtil.getLocale(p);
        final String _locale = locale;
        BossBar b = this.bars.computeIfAbsent(locale, s -> generateBossbar(_locale));
        b.addPlayer(p);
    }

    public void removeBar(Player p) {
        String locale = PlayerUtil.getLocale(p);
        for (BossBar bar : new ArrayList<>(this.bars.values())) {
            bar.removePlayer(p);
        }
        BossBar bar = this.bars.get(PlayerUtil.getLocale(p));
        if (bar == null) {
            return;
        }
        if (bar.getPlayers().isEmpty()) {
            bar.setVisible(false);
            bar.removeAll();
            this.bars.remove(locale);
        }
    }

    public BossBar generateBossbar(String locale) {
        String msg = this.getLanguage().buildUI(this.getConfig(), "ui", locale);

        BarColor color = BarColor.valueOf(Objects.requireNonNull(this.getConfig().getString("bar-color")).toUpperCase());

        BossBar bar = Bukkit.createBossBar(msg, color, BarStyle.SOLID);
        bar.setTitle(msg);
        return bar;
    }


    public void updateBossbar() {
        Set<String> _keySet = this.bars.keySet();
        for (String locale : _keySet) {
            String msg = this.getLanguage().buildUI(this.getConfig(), "ui", locale);
            BossBar bar = this.bars.get(locale);
            if (this.content != null) {
                bar.setTitle(this.content);
            } else {
                bar.setTitle(msg);
            }
        }

    }

    @QuarkCommand(name = "bossbar-announce", op = true)
    public static final class BossbarAnnounceCommand extends ModuleCommand<BossbarAnnouncement> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound tag = ModuleDataService.getEntry(this.getModuleId());
            if (Objects.equals(args[0], "none")) {
                tag.remove("custom");
                this.getLanguage().sendMessageTo(sender, "custom-clear");
                this.getModule().setContent(null);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : args) {
                    sb.append(s).append(" ");
                }
                String content = Queries.GLOBAL_TEMPLATE_ENGINE.handle(sb.toString());
                tag.setString("custom", content);
                this.getLanguage().sendMessageTo(sender, "custom-set", content);
                this.getModule().setContent(content);
            }
            this.getModule().updateBossbar();
            ModuleDataService.save(this.getModuleId());
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("[content]");
                tabList.add("none");
            }
        }
    }
}
