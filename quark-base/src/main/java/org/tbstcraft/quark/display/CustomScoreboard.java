package org.tbstcraft.quark.display;

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
import org.tbstcraft.quark.framework.config.Queries;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.framework.text.TextBuilder;
import org.tbstcraft.quark.util.api.APIProfile;
import org.tbstcraft.quark.util.api.APIProfileTest;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "0.1",compatBlackList = {APIProfile.FOLIA})
public class CustomScoreboard extends PackageModule {
    private final Map<Player, Scoreboard> scoreboards = new HashMap<>();

    static Objective saveGetObjective(String id, Scoreboard scoreboard) {
        Objective obj = scoreboard.getObjective(id);
        if (obj == null) {
            obj = scoreboard.registerNewObjective(id, "-quark-display");
        }
        return obj;
    }

    @Override
    public void enable() {
        TaskService.timerTask("quark://scoreboard/update", 0, 20, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                setScoreboard(p);
            }
        });
    }

    @Override
    public void disable() {
        TaskService.cancelTask("quark://scoreboard/update");
        for (Player p : Bukkit.getOnlinePlayers()) {
            unsetScoreboard(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unsetScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
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
        }else{
            buffer2.unregister();
            buffer2 = board.registerNewObjective("buffer2", "-quark-display");
            this.build(player, buffer2);
            buffer2.setDisplaySlot(DisplaySlot.SIDEBAR);
            buffer1.setDisplaySlot(null);
        }
        player.setScoreboard(board);
    }

    private void build(Player player, Objective builder) {
        String locale = PlayerUtil.getLocale(player);

        Component component = TextBuilder.buildComponent(this.getLanguage().getMessage(locale, "title"));
        if (APIProfileTest.isPaperCompat()) {
            builder.displayName(component);
        } else {
            builder.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        }

        String ui = this.getLanguage().buildUI(this.getConfig(), "ui", locale).replace("{player}", player.getName());
        ui = Queries.PLAYER_TEMPLATE_ENGINE.handle(player, ui);
        List<String> uiBlock = TextBuilder.buildStringBlocks(ui);

        Map<String, Integer> existing = new HashMap<>();
        for (int i = 0; i < uiBlock.size(); i++) {
            String column = uiBlock.get(i);
            if (existing.containsKey(column)) {
                int fix = existing.get(column);
                existing.put(column, fix + 1);
                column = column + " ".repeat(fix+1);
            } else {
                existing.put(column, 0);
            }
            builder.getScore(column).setScore(uiBlock.size() - i);
        }
    }


    private void unsetScoreboard(Player p) {
        this.scoreboards.get(p).clearSlot(DisplaySlot.SIDEBAR);
        this.scoreboards.remove(p);
    }
}
