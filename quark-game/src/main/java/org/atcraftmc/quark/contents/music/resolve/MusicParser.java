package org.atcraftmc.quark.contents.music.resolve;

import org.atcraftmc.quark.contents.music.MusicData;

import java.io.File;

public interface MusicParser {
    MusicData load(File file, int offset, boolean remap, float speedMod, int interpolation);
}
