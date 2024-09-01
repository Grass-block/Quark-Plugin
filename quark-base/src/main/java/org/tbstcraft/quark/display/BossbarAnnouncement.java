package org.tbstcraft.quark.display;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.tbstcraft.quark.api.ClientLocaleChangeEvent;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.text.TextBuilder;
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
    private final HashMap<Locale, BossbarWrapper> bars = new HashMap<>();
    private String content = null;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        var config = this.getConfig();

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

    @EventHandler
    public void onPlayerLocaleChange(ClientLocaleChangeEvent event) {
        removeBar(event.getPlayer());
        addBar(event.getPlayer());
    }

    public void addBar(Player p) {
        final Locale locale = Language.locale(p);
        BossbarWrapper b = this.bars.computeIfAbsent(locale, s -> generateBossbar(locale));
        b.add(p);
    }

    public void removeBar(Player p) {
        Locale locale = Language.locale(p);
        for (BossbarWrapper bar : new ArrayList<>(this.bars.values())) {
            bar.remove(p);
        }
        BossbarWrapper bar = this.bars.get(Language.locale(p));
        if (bar == null) {
            return;
        }
        if (bar.getAudiences().isEmpty()) {
            this.bars.remove(locale);
        }
    }

    private BossbarWrapper generateBossbar(Locale locale) {
        String msg = this.language.buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui"));

        BossbarWrapper wrapper = BossbarWrapper.create();

        wrapper.color(this.getConfig().getString("bar-color"));
        wrapper.title(TextBuilder.buildComponent(msg));
        return wrapper;
    }

    public void updateBossbar() {
        Set<Locale> _keySet = this.bars.keySet();
        for (Locale locale : _keySet) {
            String msg = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui"));
            BossbarWrapper bar = this.bars.get(locale);
            if (this.content != null) {
                bar.title(TextBuilder.buildComponent(this.content));
            } else {
                bar.title(TextBuilder.buildComponent(msg));
            }
        }
    }

    interface BossbarWrapper {
        static BossbarWrapper create() {
            if (APIProfileTest.isPaperCompat()) {
                return new AdventureBossbar();
            }
            return new BukkitBossbar();
        }

        void add(Player audience);

        void remove(Player audience);

        void title(Component title);

        void color(String content);

        Set<Player> getAudiences();

        final class AdventureBossbar implements BossbarWrapper {
            private final net.kyori.adventure.bossbar.BossBar bar;
            private final Set<Player> players = new HashSet<>();

            public AdventureBossbar() {
                this.bar = net.kyori.adventure.bossbar.BossBar.bossBar(Component.text(), 1.0f, net.kyori.adventure.bossbar.BossBar.Color.WHITE, net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS);
            }

            @Override
            public void add(Player audience) {
                audience.showBossBar(this.bar);
                this.players.add(audience);
            }

            @Override
            public void remove(Player audience) {
                audience.hideBossBar(this.bar);
                this.players.remove(audience);
            }

            @Override
            public void title(Component title) {
                this.bar.name(title);
            }

            @Override
            public void color(String content) {
                this.bar.color(net.kyori.adventure.bossbar.BossBar.Color.valueOf(content.toUpperCase()));
            }

            @Override
            public Set<Player> getAudiences() {
                return this.players;
            }
        }

        final class BukkitBossbar implements BossbarWrapper {
            private final BossBar bar;

            public BukkitBossbar() {
                this.bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
            }

            @Override
            public void add(Player audience) {
                this.bar.addPlayer(audience);
            }

            @Override
            public void remove(Player audience) {
                this.bar.removePlayer(audience);
            }

            @Override
            public void title(Component title) {
                this.bar.setTitle(LegacyComponentSerializer.legacySection().serialize(title));
            }

            @Override
            public void color(String content) {
                this.bar.setColor(BarColor.valueOf(content.toUpperCase()));
            }

            @Override
            public Set<Player> getAudiences() {
                return new HashSet<>(this.bar.getPlayers());
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
