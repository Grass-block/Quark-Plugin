package org.tbstcraft.quark;

import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.task.Task;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

public class Audience {
    private final Map<String, Boolean> rejections = new HashMap<>();
    private Player pointer;

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

        Players.sendMessage(this.pointer, message);
    }

    public void sendTitle(String channel, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        if (isChannelRejected(channel)) {
            return;
        }
    }


    public static final class Renderer {
        private final Audience holder;

        int currentPriority;
        String currentTask;
        Runnable currentCommand;

        Stack<Task> commands;
        Stack<Integer> layers;
        Set<String> scheduled;

        public Renderer(Audience holder) {
            this.holder = holder;
        }

        private String hash(String channel) {
            return "quark:ui:render@%s:%s".formatted(this.holder.pointer.getName(), channel);
        }

        public void cancel(String channel, int period) {

        }

        public void render(String channel, int period, int layer, boolean async, Consumer<Player> task) {
            if (this.scheduled.contains(channel)) {
                return;
            }

            TaskService.async().timer(0, period, (ctx) -> {
                if (this.holder.isChannelRejected(channel)) {
                    ctx.cancel();
                }
                if (this.currentPriority >= layer) {
                    return;
                }

                task.accept(this.holder.pointer);
            });
        }

    }
}
