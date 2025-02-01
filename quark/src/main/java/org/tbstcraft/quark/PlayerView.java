package org.tbstcraft.quark;

import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.task.Task;
import org.atcraftmc.qlib.task.TaskScheduler;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class PlayerView {
    public static final Map<Player, PlayerView> INSTANCES = new HashMap<>();
    private final ChannelRenderer actionbar = new ChannelRenderer(this);
    private final Map<String, Boolean> rejections = new HashMap<>();
    private final Player pointer;

    public PlayerView(Player pointer) {
        this.pointer = pointer;
    }

    public static PlayerView getInstance(final Player player) {
        return INSTANCES.computeIfAbsent(player, PlayerView::new);
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

        TextSender.sendMessage(this.pointer, message);
    }

    public ChannelRenderer getActionbar() {
        return actionbar;
    }

    public interface ViewRenderer {
        void render(Player player, Task context);
    }

    public record GeneratedRendererRecord(String id, int priority, int interval, ViewRenderer renderer, TaskScheduler target) {
    }

    public static final class ChannelRenderer {
        public static final int INTERVAL = 3;
        private final Map<String, GeneratedRendererRecord> renderers = new HashMap<>();
        private final PlayerView holder;
        private boolean rejectAll;
        private Task currentTask;

        public ChannelRenderer(PlayerView holder) {
            this.holder = holder;
        }

        public void addChannel(String id, int priority, int interval, TaskScheduler target, ViewRenderer action) {
            if(this.rejectAll){
                return;
            }

            this.renderers.put(id, new GeneratedRendererRecord(id, priority, interval, action, target));
            this.select();
        }

        public void addChannel(String id, int priority, int interval, ViewRenderer action) {
            if(this.rejectAll){
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

            if(this.rejectAll){
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

            this.currentTask = selected.target().timer(1, selected.interval(), (t) -> {
                if (this.holder.isChannelRejected(selected.id())) {
                    return;
                }

                selected.renderer().render(this.holder.pointer, t);
            });
        }

        public void rejectAll(boolean enable) {
            this.rejectAll = enable;
        }
    }
}
