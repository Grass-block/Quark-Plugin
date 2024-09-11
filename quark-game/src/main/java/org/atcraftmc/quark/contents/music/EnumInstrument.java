package org.atcraftmc.quark.contents.music;

import org.bukkit.Sound;

public enum EnumInstrument {
    PIANO,
    BASS_DRUM,
    SNARE_DRUM,
    STICKS,
    BASS_GUITAR,
    FLUTE,
    BELL,
    GUITAR,
    CHIME,
    XYLOPHONE,
    IRON_XYLOPHONE,
    COW_BELL,
    DIDGERIDOO,
    BIT,
    BANJO,
    PLING,
    UNKNOWN,
    HUMAN_VOICE, STD_DRUM, HAT;

    static Sound bukkit(EnumInstrument map) {
        return switch (map) {
            case PIANO, UNKNOWN -> Sound.BLOCK_NOTE_BLOCK_HARP;
            case BASS_GUITAR -> Sound.BLOCK_NOTE_BLOCK_BASS;
            case SNARE_DRUM -> Sound.BLOCK_NOTE_BLOCK_SNARE;
            case STICKS, HAT -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case BASS_DRUM -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case FLUTE -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case BELL -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case GUITAR -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case CHIME -> Sound.BLOCK_NOTE_BLOCK_CHIME;
            case XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case COW_BELL -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case BIT -> Sound.BLOCK_NOTE_BLOCK_BIT;
            case BANJO -> Sound.BLOCK_NOTE_BLOCK_BANJO;
            case PLING -> Sound.BLOCK_NOTE_BLOCK_PLING;
            case HUMAN_VOICE -> Sound.BLOCK_NOTE_BLOCK_HARP;
            case STD_DRUM -> Sound.BLOCK_NOTE_BLOCK_BASS;//this shouldn't happen
        };
    }

    public Sound bukkit() {
        return bukkit(this);
    }
}
