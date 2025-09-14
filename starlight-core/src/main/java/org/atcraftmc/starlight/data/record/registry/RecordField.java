package org.atcraftmc.starlight.data.record.registry;

import org.atcraftmc.starlight.core.ui.TextRenderer;

public final class RecordField<I> {
    public static final RecordField<String> WORLD = new RecordField<>("world", TextRenderer.literal("World"), DataRenderer.STRING);
    public static final RecordField<Number> X = new RecordField<>("x", TextRenderer.literal("X"), DataRenderer.NUMBER);
    public static final RecordField<Number> Y = new RecordField<>("y", TextRenderer.literal("Y"), DataRenderer.NUMBER);
    public static final RecordField<Number> Z = new RecordField<>("z", TextRenderer.literal("Z"), DataRenderer.NUMBER);
    public static final RecordField<String> TYPE = new RecordField<>("type", TextRenderer.literal("Type"), DataRenderer.STRING);

    private final String id;
    private final TextRenderer display;
    private final DataRenderer<I> renderer;

    public RecordField(String id, TextRenderer display, DataRenderer<I> renderer) {
        this.id = id;
        this.display = display;
        this.renderer = renderer;
    }
}
