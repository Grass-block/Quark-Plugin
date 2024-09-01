package org.tbstcraft.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.*;

@SuppressWarnings("deprecation")
@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "0.1")
public final class CustomScoreboard extends PackageModule {
    private final Map<Player, Scoreboard> scoreboards = new HashMap<>();

    private Set<Player> sessions = new HashSet<>();

    static Objective saveGetObjective(String id, Scoreboard scoreboard) {
        Objective obj = scoreboard.getObjective(id);
        if (obj == null) {
            obj = scoreboard.registerNewObjective(id, "-quark-display");
        }
        return obj;
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.scoreboard.Scoreboard"));
    }

    @Override
    public void enable() {
        this.sessions.addAll(Bukkit.getOnlinePlayers());

        TaskService.timerTask("quark://scoreboard/update", 1, 20, () -> {
            if (this.sessions == null) {
                return;
            }
            for (Player p : new HashSet<>(this.sessions)) {
                setScoreboard(p);
            }
        });
    }

    @Override
    public void disable() {
        this.sessions = null;
        TaskService.cancelTask("quark://scoreboard/update");
        for (Player p : Bukkit.getOnlinePlayers()) {
            unsetScoreboard(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.sessions == null) {
            return;
        }
        this.sessions.remove(event.getPlayer());
        unsetScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.sessions == null) {
            return;
        }
        this.sessions.add(event.getPlayer());
        setScoreboard(event.getPlayer());
    }

    private void setScoreboard(Player player) {
        if (!this.scoreboards.containsKey(player)) {
            this.scoreboards.put(player, Bukkit.getScoreboardManager().getNewScoreboard());
        }
        Scoreboard board = this.scoreboards.get(player);

        Objective buffer1 = saveGetObjective("buffer1", board);
        Objective buffer2 = saveGetObjective("buffer2", board);

        if (buffer1.getDisplaySlot() == null) {
            buffer1.unregister();
            buffer1 = board.registerNewObjective("buffer1", "-quark-display");
            this.build(player, buffer1);
            buffer1.setDisplaySlot(DisplaySlot.SIDEBAR);
            buffer2.setDisplaySlot(null);
        } else {
            buffer2.unregister();
            buffer2 = board.registerNewObjective("buffer2", "-quark-display");
            this.build(player, buffer2);
            buffer2.setDisplaySlot(DisplaySlot.SIDEBAR);
            buffer1.setDisplaySlot(null);
        }
        player.setScoreboard(board);
    }

    private void build(Player player, Objective builder) {
        Locale locale = Language.locale(player);

        Component component = TextBuilder.buildComponent(this.getLanguage().getMessage(locale, "title"));
        if (APIProfileTest.isPaperCompat()) {
            builder.displayName(component);
        } else {
            builder.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        }

        String template = Language.generateTemplate(this.getConfig(), "ui");

        String ui = this.getLanguage().buildTemplate(locale, template).replace("{player}", player.getName());
        ui = PlaceHolderService.formatPlayer(player, ui);
        List<String> uiBlock = TextBuilder.buildStringBlocks(ui);

        Map<String, Integer> existing = new HashMap<>();
        for (int i = 0; i < uiBlock.size(); i++) {
            String column = uiBlock.get(i);
            if (existing.containsKey(column)) {
                int fix = existing.get(column);
                existing.put(column, fix + 1);
                column = column + " ".repeat(fix + 1);
            } else {
                existing.put(column, 0);
            }
            builder.getScore(column).setScore(uiBlock.size() - i);
        }
    }

    private void unsetScoreboard(Player p) {
        if (!this.scoreboards.containsKey(p)) {
            return;
        }
        this.scoreboards.get(p).clearSlot(DisplaySlot.SIDEBAR);
        this.scoreboards.remove(p);
    }
}
