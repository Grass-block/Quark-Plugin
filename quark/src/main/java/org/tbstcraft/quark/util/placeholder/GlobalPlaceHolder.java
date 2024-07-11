package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.function.Supplier;

public interface GlobalPlaceHolder {
    static GlobalPlaceHolder value(Object value) {
        return new ValuePlaceHolder(value);
    }

    static GlobalPlaceHolder object(Supplier<Object> value) {
        return (StringPlaceHolder) () -> value.get().toString();
    }

    ComponentLike get();

    default String getText() {
        return LegacyComponentSerializer.legacySection().serialize(get().asComponent());
    }

    class ValuePlaceHolder implements StringPlaceHolder {
        private final Object value;

        public ValuePlaceHolder(Object value) {
            this.value = value;
        }

        @Override
        public String getText() {
            return this.value.toString();
        }
    }
}
