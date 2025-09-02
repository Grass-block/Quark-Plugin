package org.atcraftmc.starlight.api.event;

import org.atcraftmc.starlight.Starlight;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

public abstract class CoreEvent extends CustomEvent {
    private final Starlight instance;

    protected CoreEvent(Starlight instance) {
        this.instance = instance;
    }

    public Starlight getInstance() {
        return instance;
    }

    @SLEvent
    public static final class Launch extends CoreEvent {
        public Launch(Starlight instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Launch.class);
        }
    }

    @SLEvent
    public static final class PostLaunch extends CoreEvent {
        public PostLaunch(Starlight instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostLaunch.class);
        }
    }

    @SLEvent
    public static final class Dispose extends CoreEvent {
        public Dispose(Starlight instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Dispose.class);
        }
    }

    @SLEvent
    public static final class PostDispose extends CoreEvent {
        public PostDispose(Starlight instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostDispose.class);
        }
    }

    @SLEvent
    public static class Reload extends CoreEvent {
        public Reload() {
            super(null);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Reload.class);
        }
    }

    @SLEvent
    public static class PostReload extends CoreEvent {
        public PostReload() {
            super(null);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostReload.class);
        }
    }
}
