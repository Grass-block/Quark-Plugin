package org.atcraftmc.quark.contents.music;

import java.util.ArrayList;
import java.util.List;

public final class MusicData {
    private final List<List<MusicNode>> nodes;
    private final int offset;
    private final long millsLength;
    private final double tempo;
    private final String name;
    private long tickLength;

    public MusicData(String name, int offset, long tickLength, long millsLength, double tempo) {
        this.name = name;
        this.offset = offset;
        this.tickLength = tickLength;
        this.millsLength = millsLength;

        this.nodes = new ArrayList<>((int) tickLength);
        this.tempo = tempo;
    }


    public void addNode(int tick, MusicNode node) {
        while (this.getNodes().size() <= tick) {
            this.getNodes().add(null);
        }
        if (this.getNodes().get(tick) == null) {
            this.getNodes().set(tick, new ArrayList<>());
        }
        this.getNodes().get(tick).add(node);

        if (tick > this.tickLength) {
            this.tickLength = tick;
        }
    }

    public String getName() {
        return name;
    }

    public List<List<MusicNode>> getNodes() {
        return nodes;
    }

    public int getOffset() {
        return offset;
    }

    public long getTickLength() {
        return tickLength;
    }

    public long getMillsLength() {
        return millsLength;
    }

    public double getTempo() {
        return tempo;
    }
}
