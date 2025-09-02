package org.atcraftmc.starlight.api.event;

import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.atcraftmc.starlight.framework.module.ModuleMeta;
import org.atcraftmc.starlight.util.ObjectOperationResult;

public abstract class ModuleEvent extends CustomEvent {
    private final ModuleMeta meta;

    protected ModuleEvent(ModuleMeta meta) {
        this.meta = meta;
    }

    public ModuleMeta getMeta() {
        return meta;
    }

    @SLEvent
    public static final class PreEnable extends ModuleEvent {
        public PreEnable(ModuleMeta meta) {
            super(meta);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PreEnable.class);
        }
    }

    @SLEvent
    public static final class Enable extends ModuleEvent {
        private final ObjectOperationResult result;

        public Enable(ModuleMeta meta, ObjectOperationResult result) {
            super(meta);
            this.result = result;
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Enable.class);
        }

        public ObjectOperationResult getResult() {
            return result;
        }
    }

    @SLEvent
    public static final class PreDisable extends ModuleEvent {
        public PreDisable(ModuleMeta meta) {
            super(meta);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PreDisable.class);
        }
    }

    @SLEvent
    public static final class Disable extends ModuleEvent {
        private final ObjectOperationResult result;

        public Disable(ModuleMeta meta, ObjectOperationResult result) {
            super(meta);
            this.result = result;
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(Disable.class);
        }

        public ObjectOperationResult getResult() {
            return result;
        }
    }
}
