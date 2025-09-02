package org.atcraftmc.starlight.display;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.qlib.texts.placeholder.StringObjectPlaceHolder;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.CachedInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;

@SuppressWarnings("deprecation")
@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule
@QuarkCommand(name = "header", permission = "-starlight.name.header")
@Components({PlayerNameHeader.BelowNameColumns.class})
public final class PlayerNameHeader extends CommandModule {
    public static final TableColumn<String> PLAYER_HEADER = TableColumn.string("name_header", 16, "unset");
    private final Map<UUID, String> cache = new HashMap<>();
    MethodHandleO1<Player, Component> SET_NAME_HEADER = MethodHandle.select(ctx -> {
        ctx.attempt(() -> Player.class.getMethod("playerListName", Component.class), (p, c) -> {
            p.playerListName(c);
            p.customName(c);
            p.displayName(c);
        });
        ctx.dummy((p, c) -> {
            var cc = ComponentSerializer.legacy(c);
            p.setPlayerListName(cc);
            p.setCustomName(cc);
            p.setDisplayName(cc);
        });
    });

    @Inject
    private LanguageEntry language;

    @Inject
    private Logger logger;

    @Override
    public void enable() {
        super.enable();

        for (var p : Bukkit.getOnlinePlayers()) {
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
        var p = Bukkit.getPlayerExact(args[1]);
        var o = Bukkit.getOfflinePlayer(args[1]);

        if (Objects.equals(args[0], "set")) {
            PLAYER_HEADER.set(PlayerDataService.PLAYER_SHARED, Bukkit.getOfflinePlayer(args[1]).getUniqueId(), args[2]);
            MessageAccessor.send(this.language, sender, "set-header", args[1], args[2]);
        }
        if (Objects.equals(args[0], "clear")) {
            PLAYER_HEADER.set(PlayerDataService.PLAYER_SHARED, o.getUniqueId(), "unset");
            MessageAccessor.send(this.language, sender, "clear-header", args[1]);
        }

        this.cache.remove(o.getUniqueId());

        if (p != null && p.isOnline()) {
            this.attach(p);
        }
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
        SET_NAME_HEADER.invoke(p, name);

        for (var viewer : Bukkit.getOnlinePlayers()) {
            var prefix = getPlayerPrefix(p);//todo: render with locale
            var postfix = getPlayerSuffix(p);

            VisualScoreboardService.instance().visualScoreboard(viewer).setNameTag(p, prefix, postfix);
        }
    }

    public void detach(Player p) {
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());
        for (var target : Bukkit.getOnlinePlayers()) {
            VisualScoreboardService.instance().visualScoreboard(target).setNameTag(p, Component.text(""), Component.text(""));
        }
    }

    public String getHeader(Player player) {
        if (this.cache.containsKey(player.getUniqueId())) {
            return this.cache.get(player.getUniqueId());
        }

        var data = PLAYER_HEADER.get(PlayerDataService.PLAYER_SHARED, player.getUniqueId());

        if (!Objects.equals(data, "unset")) {
            this.cache.put(player.getUniqueId(), data);
            return data;
        } else {
            if (player.isOp()) {
                var h = getConfig().value("op-header").string();
                this.cache.put(player.getUniqueId(), h);
                return h;
            } else {
                var h = getConfig().value("player-header").string();
                this.cache.put(player.getUniqueId(), h);
                return h;
            }
        }
    }

    public Component getPlayerName(Player player) {
        String header = getHeader(player);
        String template = this.getConfig().value("template").string();
        if (template == null) {
            return Component.text(player.getName());
        }
        return TextBuilder.buildComponent(PlaceHolderService.format(template.replace("{player}", player.getName())
                                                                            .replace("{header}", header + TextBuilder.EMPTY_COMPONENT)));
    }

    public Component getPlayerSuffix(Player player) {
        var header = getHeader(player);
        var template = Objects.requireNonNull(this.getConfig().value("template").string()).split("\\{player}");
        if (template.length == 1) {
            return Component.text("");
        }
        return TextBuilder.buildComponent(PlaceHolderService.format(template[template.length - 1].replace("{header}", header)));
    }

    public Component getPlayerPrefix(Player player) {
        var header = getHeader(player);
        var template = Objects.requireNonNull(this.getConfig().value("template").string()).split("\\{player}");
        return TextBuilder.buildComponent(PlaceHolderService.format(template[0].replace("{header}", header)));
    }


    public static final class BelowNameColumns extends ModuleComponent<PlayerNameHeader> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat"));
            Compatibility.assertion(ConfigContainer.getInstance().value("quark-display:player-name-header:below-name-enable").bool());
        }

        public Component build(Player player, MinecraftLocale locale) {
            String template = Language.generateTemplate(this.getConfig(), "below-name");

            String ui = MessageAccessor.buildTemplate(getLanguage(), locale, template);
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
                var scoreboard = ((VisualScoreboardService.BukkitVisualScoreboard) VisualScoreboardService.instance()
                        .visualScoreboard(view)).getScoreboard();

                var obj = scoreboard.getObjective("below-name");

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
}
