package org.atcraftmc.quark.display;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO0;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.ModuleDataService;
import org.tbstcraft.quark.data.PlayerDataService;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.data.storage.DataEntry;
import org.tbstcraft.quark.foundation.ComponentSerializer;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.component.Components;
import org.tbstcraft.quark.framework.module.component.ModuleComponent;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.CachedInfo;
import org.atcraftmc.qlib.texts.placeholder.StringObjectPlaceHolder;

import java.util.*;

@SuppressWarnings("deprecation")
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule
@QuarkCommand(name = "header", permission = "-quark.header")
@Components({PlayerNameHeader.ProtocolLibNameTags.class, PlayerNameHeader.NameTags.class, PlayerNameHeader.BelowNameColumns.class})
public final class PlayerNameHeader extends CommandModule {
    @Inject
    private LanguageEntry language;

    @Inject
    private Logger logger;

    @Override
    public void enable() {
        super.enable();

        DataEntry legacy = ModuleDataService.get("player-name-header");

        var keys = new ArrayList<>(legacy.getTagMap().keySet());

        for (String player : keys) {
            try {
                DataEntry fixed = PlayerDataService.get(player);
                fixed.set("player-name-header", legacy.getString(player));
                legacy.getTagMap().remove(player);
                fixed.save();
                this.logger.info("moved playerData handle of %s to modern format.".formatted(player));
            } catch (Exception e) {
                this.logger.error("failed to move playerData handle of %s".formatted(player), e);
            }
        }

        ModuleDataService.save("player-name-header");

        for (Player p : Bukkit.getOnlinePlayers()) {
            this.attach(p);
        }

        PlaceHolderService.PLAYER.register("rank", (StringObjectPlaceHolder<Player>) this::getHeader);
    }

    @Override
    public void disable() {
        super.disable();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.detach(p);
        }

        PlaceHolderService.PLAYER.unregister("rank");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p = Bukkit.getPlayerExact(args[1]);
        var entry = PlayerDataService.get(args[1]);

        if (Objects.equals(args[0], "set")) {
            entry.set("player-name-header", args[2]);
            this.language.sendMessage(sender, "set-header", args[1], args[2]);
        }
        if (Objects.equals(args[0], "clear")) {
            entry.remove("player-name-header");
            this.language.sendMessage(sender, "clear-header", args[1]);
        }
        if (p != null && p.isOnline()) {
            this.attach(p);
        }
        entry.save();
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("set");
            tabList.add("clear");
        }
        if (buffer.length == 2) {
            tabList.addAll(CachedInfo.getAllPlayerNames());
        }
        if (buffer.length == 3 && Objects.equals(buffer[0], "set")) {
            tabList.add("<header>");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.attach(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.detach(event.getPlayer());
    }


    public void attach(Player p) {
        var name = getPlayerName(p);

        var string = LegacyComponentSerializer.legacySection().serialize(name);
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (APIProfileTest.isPaperCompat()) {
            try {
                p.playerListName(name);
            } catch (IllegalArgumentException ignored) {
            }
            p.customName(name);
            p.displayName(name);
            return;
        }

        p.setDisplayName(string);

        try {
            p.setPlayerListName(string);
        } catch (IllegalArgumentException ignored) {
        }


        p.setCustomName(string);
        p.setCustomNameVisible(false);
    }

    public void detach(Player p) {
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
    }

    public String getHeader(Player player) {
        var data = PlayerDataService.get(player);
        String header;

        if (data.hasKey("player-name-header")) {
            header = data.getString("player-name-header");
        } else {
            if (player.isOp()) {
                header = getConfig().getString("op-header");
            } else {
                header = getConfig().getString("player-header");
            }
        }
        return "{;}" + header + "{;}";
    }

    public Component getPlayerName(Player player) {
        String header = getHeader(player);
        String template = this.getConfig().getString("template");
        if (template == null) {
            return Component.text(player.getName());
        }
        return TextBuilder.buildComponent(PlaceHolderService.format(template.replace("{player}", player.getName())
                                                                            .replace("{header}", header + TextBuilder.EMPTY_COMPONENT)));
    }

    public Component getPlayerSuffix(Player player) {
        var header = getHeader(player);
        var template = Objects.requireNonNull(this.getConfig().getString("template")).split("\\{player}");
        if (template.length == 1) {
            return Component.text("");
        }
        return TextBuilder.buildComponent(PlaceHolderService.format(template[template.length - 1].replace("{header}", header)));
    }

    public Component getPlayerPrefix(Player player) {
        var header = getHeader(player);
        var template = Objects.requireNonNull(this.getConfig().getString("template")).split("\\{player}");
        return TextBuilder.buildComponent(PlaceHolderService.format(template[0].replace("{header}", header)));
    }


    public static final class NameTags extends ModuleComponent<PlayerNameHeader> {
        private static final MethodHandleO2<Team, Component, Component> TEAM_PREFIX = MethodHandle.select((ctx) -> {
            ctx.attempt(() -> Team.class.getMethod("prefix", Component.class), (t, c1, c2) -> {
                t.prefix(c1);
                t.suffix(c2);
            });
            ctx.dummy((t, c1, c2) -> {
                t.setPrefix(ComponentSerializer.legacy(c1));
                t.setSuffix(ComponentSerializer.legacy(c2));
            });
        });
        private static final MethodHandleO0<Team> SET_NAME_TAG_VISIBILITY = MethodHandle.select((ctx) -> {
            ctx.attempt(() -> {
                Class.forName("org.bukkit.scoreboard.Team.Option");
                return null;
            }, (t) -> t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS));
            ctx.attempt(
                    () -> Team.class.getMethod("setNameTagVisibility", NameTagVisibility.class),
                    (t) -> t.setNameTagVisibility(NameTagVisibility.ALWAYS)
                       );
            ctx.dummy((t) -> {});
        });


        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("org.bukkit.scoreboard.Team"));
            Compatibility.requireClass(() -> Class.forName("org.bukkit.scoreboard.ScoreboardTeam"));
        }

        @Override
        public void enable() {
            TaskService.global().timer("render-name-tags", 1, 20, this::render);
        }

        @Override
        public void disable() {
            TaskService.global().cancel("render-name-tags");
        }

        public void render() {
            for (Player view : Bukkit.getOnlinePlayers()) {
                Scoreboard scoreboard = view.getScoreboard();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    var tid = "quark@" + player.getName().hashCode();

                    Team t = scoreboard.getTeam(tid);

                    if (t == null) {
                        t = scoreboard.registerNewTeam(tid);
                    }


                    SET_NAME_TAG_VISIBILITY.invoke(t);
                    TEAM_PREFIX.invoke(t, parent.getPlayerPrefix(player), parent.getPlayerSuffix(player));
                    t.addPlayer(player);
                }
            }
        }
    }

    public static final class BelowNameColumns extends ModuleComponent<PlayerNameHeader> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat"));
            Compatibility.assertion(false);
        }

        public Component build(Player player, Locale locale) {
            String template = Language.generateTemplate(this.getConfig(), "below-name");

            String ui = this.getLanguage().buildTemplate(locale, template);
            ui = PlaceHolderService.formatPlayer(player, ui);

            return TextBuilder.buildComponent(ui);
        }

        @Override
        public void enable() {
            TaskService.global().timer("render-below-name", 0, 20, this::render);
        }

        @Override
        public void disable() {
            TaskService.global().cancel("render-below-name");
        }

        public void render() {
            for (Player view : Bukkit.getOnlinePlayers()) {
                Scoreboard scoreboard = view.getScoreboard();

                Objective obj = scoreboard.getObjective("below-name");

                if (obj == null) {
                    obj = scoreboard.registerNewObjective("below-name", "@quark");
                }

                obj.setDisplaySlot(DisplaySlot.BELOW_NAME);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    obj.displayName(build(player, LocaleService.locale(view)));
                    obj.getScore(player).numberFormat(NumberFormat.fixed(build(player, LocaleService.locale(view))));
                }
            }
        }
    }

    public static final class ProtocolLibNameTags extends ModuleComponent<PlayerNameHeader> {



        private final ProtocolManager service = ProtocolLibrary.getProtocolManager();

        private final PacketListener entityMeta = new PacketAdapter(Quark.getInstance(), PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityId = event.getPacket().getIntegers().read(0);
                var packet = event.getPacket();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (entityId == player.getEntityId()) {

                        // 创建 DataWatcher 并设置新的显示名称
                        var watcher = new WrappedDataWatcher();
                        var serializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
                        var displayNameObject = new WrappedDataWatcher.WrappedDataWatcherObject(2, serializer);
                        var displayName = WrappedChatComponent.fromJson(ComponentSerializer.json(parent.getPlayerName(player)));
                        watcher.setObject(displayNameObject, Optional.of(displayName.getHandle()));

                        // 将 DataWatcher 对象写入包
                        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                        return;
                    }
                }
            }
        };

        private final PacketListener playerData = new PacketAdapter(Quark.getInstance(), PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                var packet = event.getPacket();
                var infos = packet.getPlayerInfoDataLists().read(0);

                for (PlayerInfoData data : infos) {
                    var player = Bukkit.getPlayer(data.getProfile().getName());
                    if (player == null) {
                        continue;
                    }
                    var name = ProtocolLibNameTags.this.parent.getPlayerName(player);
                    var displayName = WrappedChatComponent.fromJson(ComponentSerializer.json(name));

                    PlayerInfoData newData = new PlayerInfoData(data.getProfile(), data.getLatency(), data.getGameMode(), displayName);
                    infos.set(infos.indexOf(data), newData);
                }
                packet.getPlayerInfoDataLists().write(0, infos);
            }
        };

        @Override
        public void enable() {
            service.addPacketListener(this.playerData);
            service.addPacketListener(this.entityMeta);
        }

        @Override
        public void disable() {
            service.removePacketListener(this.playerData);
            service.removePacketListener(this.entityMeta);
        }

        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.assertion(false);
            Compatibility.requireClass(() -> Class.forName("com.comphenix.protocol.ProtocolLibrary"));
        }
    }
}
