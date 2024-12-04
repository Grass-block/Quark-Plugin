package org.tbstcraft.quark.api;

import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

public abstract class CoreEvent extends CustomEvent {
    private final Quark instance;

    protected CoreEvent(Quark instance) {
        this.instance = instance;
    }

    public Quark getInstance() {
        return instance;
    }

    @QuarkEvent
    public static final class Launch extends CoreEvent {
        public Launch(Quark instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Launch.class);
        }
    }

    @QuarkEvent
    public static final class PostLaunch extends CoreEvent {
        public PostLaunch(Quark instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostLaunch.class);
        }
    }

    @QuarkEvent
    public static final class Dispose extends CoreEvent {
        public Dispose(Quark instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Dispose.class);
        }
    }

    @QuarkEvent
    public static final class PostDispose extends CoreEvent {
        public PostDispose(Quark instance) {
            super(instance);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostDispose.class);
        }
    }

    @QuarkEvent
    public static class Reload extends CoreEvent {
        public Reload() {
            super(null);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Reload.class);
        }
    }

    @QuarkEvent
    public static class PostReload extends CoreEvent {
        public PostReload() {
            super(null);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PostReload.class);
        }
    }
}
