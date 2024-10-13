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

    static Sound legacyBukkit(EnumInstrument map){
        return switch (map) {
            case BASS_GUITAR -> Sound.valueOf("NOTE_BASS_GUITAR");
            case SNARE_DRUM -> Sound.valueOf("NOTE_SNARE_DRUM");
            case STICKS, HAT -> Sound.valueOf("NOTE_STICKS");
            case BASS_DRUM -> Sound.valueOf("NOTE_BASS_DRUM");
            case DIDGERIDOO -> Sound.valueOf("NOTE_BASS");
            case PLING -> Sound.valueOf("NOTE_PLING");
            case STD_DRUM -> Sound.valueOf("NOTE_BASS");//this shouldn't happen
            default -> Sound.valueOf("NOTE_PIANO");
        };
    }

    static Sound bukkit(EnumInstrument map) {
        try {
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
        }catch (NoSuchFieldError ignored){
            return legacyBukkit(map);
        }
    }

    public Sound bukkit() {
        return bukkit(this);
    }
}
