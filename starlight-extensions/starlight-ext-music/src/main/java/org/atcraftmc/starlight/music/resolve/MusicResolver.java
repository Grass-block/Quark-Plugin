package org.atcraftmc.starlight.music.resolve;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.atcraftmc.starlight.music.MusicService.UNSUPPORTED_FORMAT;

public final class MusicResolver {
    public static final Map<String, MusicParser> DISPATCHERS = new HashMap<>();

    static {
        registerFormat(new MIDIParser(), "mid", "midi");
        registerFormat(new NBSParser(), "nbs");
    }

    static void registerFormat(MusicParser dispatcher, String... formats) {
        for (var format : formats) {
            DISPATCHERS.put(format, dispatcher);
        }
    }

    public static MusicData resolve(File f, int pitch, boolean dispatchInstrument, float speedMod, int interpolation) {
        var names = f.getName().split("\\.");

        var resolver = DISPATCHERS.get(names[names.length - 1]);

        if (resolver == null) {
            throw new RuntimeException(UNSUPPORTED_FORMAT);
        }

        return resolver.load(f, pitch, dispatchInstrument, speedMod, interpolation);
    }
}
