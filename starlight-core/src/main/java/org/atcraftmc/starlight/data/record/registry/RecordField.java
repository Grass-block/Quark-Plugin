package org.atcraftmc.starlight.data.record.registry;

import org.atcraftmc.starlight.core.ui.TextRenderer;

public final class RecordField<I> {
    private final String id;
    private final TextRenderer display;
    private final DataRenderer<I> renderer;

    public RecordField(String id, TextRenderer display, DataRenderer<I> renderer) {
        this.id = id;
        this.display = display;
        this.renderer = renderer;
    }
}
