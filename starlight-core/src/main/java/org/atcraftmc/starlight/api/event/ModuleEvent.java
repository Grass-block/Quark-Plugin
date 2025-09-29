package org.atcraftmc.starlight.api.event;

import org.atcraftmc.starlight.framework.module.SLModuleHandle;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import me.gb2022.modular.ObjectOperationResult;

public abstract class ModuleEvent extends CustomEvent {
    private final SLModuleHandle meta;

    protected ModuleEvent(SLModuleHandle meta) {
        this.meta = meta;
    }

    public SLModuleHandle getMeta() {
        return meta;
    }

    @SLEvent
    public static final class PreEnable extends ModuleEvent {
        public PreEnable(SLModuleHandle meta) {
            super(meta);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PreEnable.class);
        }
    }

    @SLEvent
    public static final class Enable extends ModuleEvent {
        private final ObjectOperationResult result;

        public Enable(SLModuleHandle meta, ObjectOperationResult result) {
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
        public PreDisable(SLModuleHandle meta) {
            super(meta);
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(PreDisable.class);
        }
    }

    @SLEvent
    public static final class Disable extends ModuleEvent {
        private final ObjectOperationResult result;

        public Disable(SLModuleHandle meta, ObjectOperationResult result) {
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
