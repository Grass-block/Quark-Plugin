package org.atcraftmc.quark.contents.music;

import javax.sound.midi.*;

public interface MusicLoader {
    static MusicData loadMidi(String name, Sequence sequence, int offset, boolean dispatchInstrument, float speedMod) {
        MusicData music = new MusicData(name, offset, sequence.getTickLength(), (long) (sequence.getMicrosecondLength() / speedMod));

        Track[] tracks = sequence.getTracks();

        InstrumentDecoder decoder = dispatchInstrument ? InstrumentDecoder.modern() : InstrumentDecoder.legacy();

        for (Track track : tracks) {
            var instrument = EnumInstrument.PIANO;

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (!(message instanceof ShortMessage sm)) {
                    continue;
                }

                if (sm.getCommand() == ShortMessage.NOTE_ON) {
                    var nodeInstrument = sm.getChannel() == 9 ? EnumInstrument.STD_DRUM : instrument;
                    music.addNode((int) event.getTick(), new MusicNode(sm.getData1(), sm.getData2() / 127f, nodeInstrument));

                    continue;
                }

                if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                    instrument = decoder.dispatch(sm.getData1());
                }
            }
        }

        return music;
    }
}
