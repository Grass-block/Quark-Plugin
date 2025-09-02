package org.atcraftmc.quark.contents.music.resolve;

import me.gb2022.commons.container.Pair;
import org.atcraftmc.quark.contents.music.EnumInstrument;
import org.atcraftmc.quark.contents.music.MusicData;
import org.atcraftmc.quark.contents.music.MusicNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class NBSParser implements MusicParser {

    public static Pair<MusicData, Boolean> parseHeader(InputStream input, String name) throws IOException {
        var ticks = readShortLE(input);
        var version = 0;
        var legacy = ticks != 0;

        if (!legacy) {
            version = input.read();
            input.skip(1); // Vanilla instrument count
            ticks = readShortLE(input);
        }

        input.skip(2); // Layer count
        var nbs_name = readString(input);
        var nbs_author = readString(input);
        var nbs_originAuthor = readString(input);
        readString(input); // Song description
        var tempo = readShortLE(input) / 100f;

        if (!nbs_name.isEmpty()) {
            name = nbs_name;
        }

        input.skip(23);
        readString(input);
        if (version >= 4) {
            input.skip(4); //Loop on/off(1), Max loop count(1), Loop start tick(2)
        }

        return new Pair<>(new MusicData(name, 0, ticks, (long) (ticks * (1000 / tempo)), (1000 / tempo)), legacy);
    }

    public static EnumInstrument getInstrument(int nbs) {
        return switch (nbs) {
            case 1 -> EnumInstrument.BASS_GUITAR;
            case 2 -> EnumInstrument.BASS_DRUM;
            case 3 -> EnumInstrument.SNARE_DRUM;
            case 4 -> EnumInstrument.STICKS;
            case 5 -> EnumInstrument.GUITAR;
            case 6 -> EnumInstrument.FLUTE;
            case 7 -> EnumInstrument.BELL;
            case 8 -> EnumInstrument.CHIME;
            case 9 -> EnumInstrument.XYLOPHONE;
            case 10 -> EnumInstrument.IRON_XYLOPHONE;
            case 11 -> EnumInstrument.COW_BELL;
            case 12 -> EnumInstrument.DIDGERIDOO;
            case 13 -> EnumInstrument.BIT;
            case 14 -> EnumInstrument.BANJO;
            case 15 -> EnumInstrument.PLING;
            default -> EnumInstrument.PIANO;
        };
    }

    public static MusicData parseNBS(Path path) {
        var currentTick = 0;
        var jumpToNextTick = Short.MIN_VALUE;

        try (var input = Files.newInputStream(path)) {
            var rec = parseHeader(input, path.getFileName().toString());
            var data = rec.getLeft();
            var legacy = rec.getRight();

            while ((jumpToNextTick = readShortLE(input)) != 0) {
                currentTick += jumpToNextTick + 1;

                while (readShortLE(input) != 0) {
                    var instrument = getInstrument(input.read());
                    var key = input.read() + 21;
                    var velocity = 128f;

                    if (!legacy) {
                        velocity = input.read();
                        input.skip(3); //skip panning(1)+pitch(2)
                    }

                    var node = new MusicNode(key, velocity / 128f, instrument);
                    data.addNode(currentTick, node);
                }
            }

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static short readShortLE(InputStream input) throws IOException {
        return (short) (input.read() & 0xFF | input.read() << 8);
    }

    private static int readIntLE(InputStream input) throws IOException {
        return input.read() | input.read() << 8 | input.read() << 16 | input.read() << 24;
    }

    private static String readString(InputStream input) throws IOException {
        return new String(input.readNBytes(readIntLE(input)));
    }

    @Override
    public MusicData load(File file, int offset, boolean remap, float speedMod, int interpolation) {
        return parseNBS(Path.of(file.toURI()));
    }
}
