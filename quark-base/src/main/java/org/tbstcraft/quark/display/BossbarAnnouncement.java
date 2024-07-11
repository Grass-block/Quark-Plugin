package org.tbstcraft.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
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
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(BossbarAnnouncement.BossbarAnnounceCommand.class)
@QuarkModule(version = "1.1.0")
public final class BossbarAnnouncement extends PackageModule {
    public static final String TASK_UPDATE_TID = "quark-display:custom_bossbar:update";
    private final HashMap<Locale, BossBar> bars = new HashMap<>();
    private String content = null;

    @Inject
    private LanguageEntry language;

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
        final Locale locale = Language.locale(p);
        BossBar b = this.bars.computeIfAbsent(locale, s -> generateBossbar(locale));
        b.addPlayer(p);
    }

    public void removeBar(Player p) {
        Locale locale = Language.locale(p);
        for (BossBar bar : new ArrayList<>(this.bars.values())) {
            bar.removePlayer(p);
        }
        BossBar bar = this.bars.get(Language.locale(p));
        if (bar == null) {
            return;
        }
        if (bar.getPlayers().isEmpty()) {
            bar.setVisible(false);
            bar.removeAll();
            this.bars.remove(locale);
        }
    }

    public BossBar generateBossbar(Locale locale) {
        String msg = this.language.buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui"));

        BarColor color = BarColor.valueOf(Objects.requireNonNull(this.getConfig().getString("bar-color")).toUpperCase());

        BossBar bar = Bukkit.createBossBar(msg, color, BarStyle.SOLID);
        bar.setTitle(msg);
        return bar;
    }


    public void updateBossbar() {
        Set<Locale> _keySet = this.bars.keySet();
        for (Locale locale : _keySet) {
            String msg = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui"));
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
                this.getLanguage().sendMessage(sender, "custom-clear");
                this.getModule().setContent(null);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : args) {
                    sb.append(s).append(" ");
                }
                String content = PlaceHolderService.format(sb.toString());
                tag.setString("custom", content);
                this.getLanguage().sendMessage(sender, "custom-set", content);
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
