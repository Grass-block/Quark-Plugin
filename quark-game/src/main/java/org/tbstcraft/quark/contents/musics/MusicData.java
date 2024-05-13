package org.tbstcraft.quark.contents.musics;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MusicData {
    private final List<List<MusicNode>> nodes;
    private final int globalNodeOffset;
    private final long tickLength;
    private final long millsLength;

    private final String name;

    private MusicData(String name, int globalNodeOffset, long tickLength, long millsLength) {
        this.name = name;
        this.globalNodeOffset = globalNodeOffset;
        this.tickLength = tickLength;
        this.millsLength = millsLength;

        this.nodes = new ArrayList<>((int) tickLength);
    }

    public static MusicData fromMidi(String name, Sequence sequence, int offset) {
        MusicData data = new MusicData(name, offset, sequence.getTickLength(), sequence.getMicrosecondLength());

        Map<Track, EnumInstrument> channelInstrumentCache = new HashMap<>();

        Track[] tracks = sequence.getTracks();
        for (Track track : tracks) {
            channelInstrumentCache.put(track, EnumInstrument.PIANO);

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (!(message instanceof ShortMessage sm)) {
                    continue;
                }

                switch (sm.getCommand()) {
                    case ShortMessage.NOTE_ON -> {
                        int note = sm.getData1();
                        int velocity = sm.getData2();
                        EnumInstrument instrument = channelInstrumentCache.get(track);
                        data.addNode((int) event.getTick(), new MusicNode(note, velocity, instrument));
                    }

                    case ShortMessage.PROGRAM_CHANGE -> {
                        int type = sm.getData1();

                        channelInstrumentCache.put(track, switch (type) {
                            case 32, 33, 34, 35, 36, 37, 38, 39 -> EnumInstrument.BASS;
                            default -> EnumInstrument.PIANO;
                        });
                    }
                }
            }
        }

        return data;
    }

    public void addNode(int tick, MusicNode node) {
        while (this.getNodes().size()<=tick){
            this.getNodes().add(null);
        }
        if (this.getNodes().get(tick) == null) {
            this.getNodes().set(tick, new ArrayList<>());
        }
        this.getNodes().get(tick).add(node);
    }

    public String getName() {
        return name;
    }

    public List<List<MusicNode>> getNodes() {
        return nodes;
    }

    public int getGlobalNodeOffset() {
        return globalNodeOffset;
    }

    public long getTickLength() {
        return tickLength;
    }

    public long getMillsLength() {
        return millsLength;
    }
}
