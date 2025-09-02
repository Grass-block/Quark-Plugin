package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO0;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import me.gb2022.commons.reflect.method.MethodHandleO3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.*;

@SLService(id = "visual-scoreboard")
public interface VisualScoreboardService extends Service {
    BukkitScoreboardService INSTANCE = new BukkitScoreboardService();

    static VisualScoreboardService instance() {
        return INSTANCE;
    }

    VisualScoreboard visualScoreboard(Player player);

    interface VisualScoreboard {
        void renderSidebar(Component title, List<String> columns);

        void stopSidebarRendering();

        void setNameTag(Player target, Component prefix, Component postfix);

        void setTabColumn(Player target, int value, Component title);

        void clearTabColumn();
    }

    final class BukkitVisualScoreboard implements VisualScoreboard {
        public static final String BUFFER_1 = "sidebar-buffer1";
        public static final String BUFFER_2 = "sidebar-buffer2";
        public static final String PLAYER_LIST = "tab-buffer";

        @SuppressWarnings("Convert2MethodRef")
        public static final MethodHandleO3<Scoreboard, Player, Component, Component> SET_TEAM = MethodHandle.select((ctx) -> {
            ctx.attempt(() -> {
                Compatibility.blackListPlatform(APIProfile.FOLIA);
                return Class.forName("org.bukkit.scoreboard.Team").getEnclosingMethod();
            }, (s, p, pr, po) -> TeamAPI.set(s, p, pr, po));
            ctx.dummy((s, p, pr, po) -> {
            });
        });

        private final Scoreboard scoreboard;
        private final UUID uuid;

        public BukkitVisualScoreboard(Scoreboard scoreboard, UUID uuid) {
            this.scoreboard = scoreboard;
            this.uuid = uuid;
        }

        private Objective getObjective(String name) {
            var builder = this.scoreboard.getObjective(name);

            if (builder == null) {
                builder = this.scoreboard.registerNewObjective(name, "starlight-display-visual");
            }

            return builder;
        }

        private void setDisplayName(Objective objective, Component title) {
            if (APIProfileTest.isPaperCompat()) {
                objective.displayName(title.asComponent());
            } else {
                objective.setDisplayName(LegacyComponentSerializer.legacySection().serialize(title.asComponent()));
            }
        }

        private void build(Objective builder, Component title, List<String> columns) {
            setDisplayName(builder, title);
            var existing = new HashMap<String, Integer>();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                if (existing.containsKey(column)) {
                    int fix = existing.get(column);
                    existing.put(column, fix + 1);
                    column = column + " ".repeat(fix + 1);
                } else {
                    existing.put(column, 0);
                }
                builder.getScore(column).setScore(columns.size() - i);
            }
        }

        public Scoreboard getScoreboard() {
            return scoreboard;
        }

        public UUID getUuid() {
            return uuid;
        }

        @Override
        public void renderSidebar(Component title, List<String> columns) {
            var buffer1 = getObjective(BUFFER_1);
            var buffer2 = getObjective(BUFFER_2);

            if (buffer1.getDisplaySlot() == null) {
                buffer1.unregister();
                buffer1 = getObjective(BUFFER_1);
                this.build(buffer1, title, columns);
                buffer1.setDisplaySlot(DisplaySlot.SIDEBAR);
                buffer2.setDisplaySlot(null);
            } else {
                buffer2.unregister();
                buffer2 = getObjective(BUFFER_2);
                this.build(buffer2, title, columns);
                buffer2.setDisplaySlot(DisplaySlot.SIDEBAR);
                buffer1.setDisplaySlot(null);
            }

            Optional.ofNullable(Bukkit.getPlayer(this.uuid)).ifPresent((p) -> p.setScoreboard(this.scoreboard));
        }

        @Override
        public void stopSidebarRendering() {
            this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }

        @Override
        public void setNameTag(Player target, Component prefix, Component postfix) {
            SET_TEAM.invoke(this.scoreboard, target, prefix, postfix);
        }

        @Override
        public void setTabColumn(Player target, int value, Component title) {
            TaskService.global().run(()->{
                var tab = getObjective(PLAYER_LIST);
                tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                tab.getScore(target).setScore(value);
                setDisplayName(tab, title);
            });
        }

        @Override
        public void clearTabColumn() {
            getObjective(PLAYER_LIST).unregister();
        }

        interface TeamAPI {
            MethodHandleO2<Team, Component, Component> TEAM_PREFIX = MethodHandle.select((ctx) -> {
                ctx.attempt(() -> Team.class.getMethod("prefix", Component.class), (t, c1, c2) -> {
                    t.prefix(c1);
                    t.suffix(c2);
                });
                ctx.dummy((t, c1, c2) -> {
                    t.setPrefix(ComponentSerializer.legacy(c1));
                    t.setSuffix(ComponentSerializer.legacy(c2));
                });
            });
            MethodHandleO0<Team> SET_NAME_TAG_VISIBILITY = MethodHandle.select((ctx) -> {
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

            static void set(Scoreboard scoreboard, Player target, Component prefix, Component postfix) {
                var team = "sl@" + target.getName();
                var t = scoreboard.getTeam(team);

                if (t == null) {
                    t = scoreboard.registerNewTeam(team);
                }

                SET_NAME_TAG_VISIBILITY.invoke(t);
                TEAM_PREFIX.invoke(t, prefix, postfix);
                t.addPlayer(target);
            }
        }
    }

    final class BukkitScoreboardService implements VisualScoreboardService {
        private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
        private final Map<UUID, VisualScoreboard> handles = new HashMap<>();

        @Override
        public void onEnable() {
            BukkitUtil.registerEventListener(this);
        }

        @Override
        public void onDisable() {
            BukkitUtil.unregisterEventListener(this);
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            this.scoreboards.computeIfAbsent(event.getPlayer().getUniqueId(), (s) -> Bukkit.getScoreboardManager().getNewScoreboard());
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent event) {
            this.scoreboards.remove(event.getPlayer().getUniqueId());
        }

        @Override
        public VisualScoreboard visualScoreboard(Player player) {
            return this.handles.computeIfAbsent(player.getUniqueId(), (s) -> new BukkitVisualScoreboard(scoreboard(player), s));
        }

        public Scoreboard scoreboard(Player player) {
            return this.scoreboards.computeIfAbsent(player.getUniqueId(), (s) -> Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }


    final class ProtocolLibScoreboardService implements VisualScoreboardService {

        @Override
        public VisualScoreboard visualScoreboard(Player player) {
            return null;
        }
    }
}
