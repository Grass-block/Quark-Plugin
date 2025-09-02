package org.atcraftmc.starlight.core;

import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.bukkit.task.Task;
import org.atcraftmc.qlib.bukkit.task.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.foundation.TextSender;

import java.util.*;
import java.util.function.Function;

public final class PlayerView {
    public static final Map<UUID, PlayerView> INSTANCES = new HashMap<>();
    private final ChannelRenderer actionbar = new ChannelRenderer(this);
    private final Map<String, Boolean> rejections = new HashMap<>();
    private final UUID pointer;

    public PlayerView(Player pointer) {
        this.pointer = pointer.getUniqueId();
    }

    public static PlayerView getInstance(final Player player) {
        return INSTANCES.computeIfAbsent(player.getUniqueId(), (k) -> new PlayerView(player));
    }

    public Player pointer() {
        return Bukkit.getPlayer(this.pointer);
    }

    public boolean isChannelRejected(String source) {
        if (this.rejections.containsKey(source)) {
            return this.rejections.get(source);
        }
        this.rejections.put(source, false);
        return false;
    }

    public void sendMessage(String channel, Component message) {
        if (isChannelRejected(channel)) {
            return;
        }

        TextSender.sendMessage(this.pointer(), message);
    }

    public ChannelRenderer getActionbar() {
        return actionbar;
    }


    public interface ViewRenderer {
        void render(Player player, Task context);
    }

    public record GeneratedRendererRecord(String id, int priority, int interval, ViewRenderer renderer,
                                          Function<Player, TaskScheduler> scheduler) {
    }

    public static final class ChannelRenderer {
        private final Map<String, GeneratedRendererRecord> renderers = new HashMap<>();
        private final PlayerView holder;
        private boolean rejectAll;
        private Task currentTask;

        public ChannelRenderer(PlayerView holder) {
            this.holder = holder;
        }

        public void addChannel(String id, int priority, int interval, TaskScheduler target, ViewRenderer action) {
            if (this.rejectAll) {
                return;
            }

            this.renderers.put(id, new GeneratedRendererRecord(id, priority, interval, action, (p) -> target));
            this.select();
        }

        public void addChannel(String id, int priority, int interval, Function<Player, TaskScheduler> target, ViewRenderer action) {
            if (this.rejectAll) {
                return;
            }

            this.renderers.put(id, new GeneratedRendererRecord(id, priority, interval, action, target));
            this.select();
        }

        public void addChannel(String id, int priority, int interval, ViewRenderer action) {
            if (this.rejectAll) {
                return;
            }

            addChannel(id, priority, interval, TaskService.global(), action);
        }

        public void removeChannel(String id) {
            this.renderers.remove(id);
            this.select();
        }

        public GeneratedRendererRecord getChannel(String id) {
            return this.renderers.get(id);
        }

        private void select() {
            if (this.currentTask != null) {
                this.currentTask.cancel();
            }

            if (this.rejectAll) {
                return;
            }

            var list = new ArrayList<>(this.renderers.values());

            if (list.isEmpty()) {
                return;
            }

            list.sort((o1, o2) -> {
                if (o1 == o2) {
                    return 0;
                }

                int pri = -Comparator.<GeneratedRendererRecord>comparingInt((o) -> o.priority).compare(o1, o2);

                if (pri != 0) {
                    return pri;
                }

                return Comparator.comparingInt(Object::hashCode).compare(o1, o2);
            });

            var selected = list.get(0);

            this.currentTask = selected.scheduler().apply(this.holder.pointer()).timer(1, selected.interval(), (t) -> {
                if (this.holder.isChannelRejected(selected.id())) {
                    return;
                }

                selected.renderer().render(this.holder.pointer(), t);
            });
        }

        public void rejectAll(boolean enable) {
            this.rejectAll = enable;
        }
    }
}
