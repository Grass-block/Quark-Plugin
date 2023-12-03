package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.TaskManager;
import org.tbstcraft.quark.config.LanguageFile;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitUtil;

import java.util.*;

@QuarkModule
public final class CustomBossbar extends PluginModule {
    public static final String TASK_UPDATE_TID = "quark_display:custom_bossbar:update";

    private final HashMap<String, BossBar> bars = new HashMap<>();
    private int prevId = 0;

    @Override
    public void onEnable() {
        this.registerListener();

        ConfigurationSection config = this.getConfig();
        TaskManager.runTimer(TASK_UPDATE_TID, 0, config.getInt("period"), this::updateBossbar);

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.addBar(player);
        }
    }

    @Override
    public void onDisable() {
        TaskManager.cancelTask(TASK_UPDATE_TID);
        this.unregisterListener();
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.removeBar(player);
        }
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
        String locale = p.getLocale();
        final String _locale = locale;
        BossBar b = this.bars.computeIfAbsent(locale, s -> generateBossbar(_locale));
        b.addPlayer(p);
    }

    public void removeBar(Player p) {
        String locale = p.getLocale();
        for (BossBar bar : new ArrayList<>(this.bars.values())) {
            bar.removePlayer(p);
        }
        BossBar bar = this.bars.get(p.getLocale());
        if(bar==null){
            return;
        }
        if (bar.getPlayers().isEmpty()) {
            this.bars.remove(locale);
        }
    }

    public BossBar generateBossbar(String locale) {
        List<String> msgList = this.getLanguage().getMessageList(locale, "bar_texts");
        String msg = LanguageFile.formatGlobal(BukkitUtil.formatChatComponent(msgList.get(this.prevId % msgList.size())));
        BarColor color = BarColor.valueOf(Objects.requireNonNull(this.getConfig().getString("bar_color")).toUpperCase());

        BossBar bar = Bukkit.createBossBar(msg, color, BarStyle.SOLID);
        bar.setTitle(msg);
        return bar;
    }


    public void updateBossbar() {
        Set<String> _keySet = this.bars.keySet();
        for (String locale : _keySet) {
            List<String> msgList = this.getLanguage().getMessageList(locale, "bar_texts");
            String msg = LanguageFile.formatGlobal(BukkitUtil.formatChatComponent(msgList.get(this.prevId % msgList.size())));
            BossBar bar = this.bars.get(locale);
            bar.setTitle(msg);
        }
        this.prevId++;
    }
}
