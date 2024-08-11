package org.tbstcraft.quark.contents.music;

public final class MusicNode {
    private final int node;
    private final float power;
    private final EnumInstrument instrument;

    public MusicNode(int node, float power, EnumInstrument instruments) {
        this.node = node;
        this.power = power;
        this.instrument = instruments;
    }

    public EnumInstrument getInstrument() {
        return instrument;
    }

    public float getPower() {
        return power;
    }

    public int getNode() {
        return node;
    }
}
