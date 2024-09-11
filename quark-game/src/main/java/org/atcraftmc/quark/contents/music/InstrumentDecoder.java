package org.atcraftmc.quark.contents.music;

import java.util.Arrays;

public interface InstrumentDecoder {


    static InstrumentDecoder legacy() {
        return (key) -> EnumInstrument.PIANO;
    }

    static InstrumentDecoder modern_gpt() {
        return (type) -> {
            if (type >= 0 && type <= 7) {
                return EnumInstrument.PIANO;
            }


            // 打击乐器类
            if (type == 118) {
                return EnumInstrument.SNARE_DRUM;
            }
            if (type == 115) {
                return EnumInstrument.STICKS;
            }
            if (type == 119) {
                return EnumInstrument.BASS_DRUM;
            }
            // 长笛类
            if (type == 73) {
                return EnumInstrument.FLUTE;
            }
            // 钟声类
            if (type == 113) {
                return EnumInstrument.BELL;
            }
            // 木琴类
            if (type == 112) {
                return EnumInstrument.CHIME;
            }
            if (type == 13) {
                return EnumInstrument.XYLOPHONE;
            }
            if (type == 95) {
                return EnumInstrument.IRON_XYLOPHONE;
            }
            if (type == 11) {
                return EnumInstrument.COW_BELL;
            }
            // 其他特殊乐器
            if (type == 12) {
                return EnumInstrument.DIDGERIDOO;
            }
            if (type == 8) {
                return EnumInstrument.BIT;
            }
            if (type == 105) {
                return EnumInstrument.BANJO;
            }
            if (type == 9) {
                return EnumInstrument.PLING;
            }

            // 钢琴的变种，例如电子钢琴
            if (type >= 8 && type <= 15) {
                return EnumInstrument.PIANO;
            }
            // 吉他类
            if (type >= 24 && type <= 31) {
                return EnumInstrument.GUITAR;
            }
            // 低音吉他类
            if (type >= 32 && type <= 39) {
                return EnumInstrument.BASS_GUITAR;
            }
            if (type >= 53 && type <= 81) {
                return EnumInstrument.COW_BELL;
            }

            return switch (type) {
                case 42: // Closed Hi-Hat
                case 44: // Pedal Hi-Hat
                case 46: // Open Hi-Hat
                    yield EnumInstrument.STICKS;
                case 41: // Low Tom
                case 43: // High Floor Tom
                case 45: // Low Tom
                case 47: // Low-Mid Tom
                case 48: // Hi-Mid Tom
                case 50: // High Tom
                    yield EnumInstrument.BASS_DRUM;
                case 49: // Crash Cymbal 1
                case 51: // Ride Cymbal 1
                case 52: // Chinese Cymbal
                    yield EnumInstrument.STICKS;
                default:
                    yield EnumInstrument.UNKNOWN;
            };
        };
    }

    static InstrumentDecoder modern() {
        return SimpleMidiTable::get;
    }


    EnumInstrument dispatch(int key);

    interface SimpleMidiTable {
        EnumInstrument[] MAP = new EnumInstrument[128];

        private static void range(int start, int end, EnumInstrument instrument) {
            for (int i = start; i <= end; i++) {
                MAP[i] = instrument;
            }
        }

        private static void item(int index, EnumInstrument instrument) {
            MAP[index] = instrument;
        }

        /**
         * <a href="https://blog.csdn.net/ruyulin/article/details/84103186">MIDI instrument table</a>
         */
        static void init() {
            Arrays.fill(MAP, EnumInstrument.PIANO);

            //piano
            range(0, 3, EnumInstrument.PIANO);
            range(4, 7, EnumInstrument.PLING);

            //colored click
            item(8, EnumInstrument.IRON_XYLOPHONE);
            item(9, EnumInstrument.CHIME);
            item(10, EnumInstrument.BELL);
            item(11, EnumInstrument.IRON_XYLOPHONE);
            item(12, EnumInstrument.XYLOPHONE);
            item(13, EnumInstrument.XYLOPHONE);
            item(14, EnumInstrument.CHIME);
            item(15, EnumInstrument.PIANO);

            //organ
            range(16, 23, EnumInstrument.FLUTE);

            //guitar
            range(24, 31, EnumInstrument.GUITAR);
            item(26, EnumInstrument.PIANO);

            //bass
            range(32, 35, EnumInstrument.BASS_GUITAR);
            range(36, 39, EnumInstrument.BASS_DRUM);

            //violin
            range(40, 47, EnumInstrument.GUITAR);

            //group
            range(48, 51, EnumInstrument.GUITAR);
            range(52, 54, EnumInstrument.HUMAN_VOICE);
            item(55, EnumInstrument.PLING);

            //trumpet
            range(56, 59, EnumInstrument.FLUTE);
            range(60, 63, EnumInstrument.DIDGERIDOO);

            //sax
            range(64, 67, EnumInstrument.FLUTE);
            range(68, 71, EnumInstrument.DIDGERIDOO);

            //flute
            range(72, 79, EnumInstrument.FLUTE);

            //lead
            range(80, 81, EnumInstrument.BIT);
            range(82, 84, EnumInstrument.FLUTE);
            item(85, EnumInstrument.HUMAN_VOICE);
            range(86, 87, EnumInstrument.BASS_DRUM);

            //item(108,EnumInstrument.BASS_DRUM);
            item(96, EnumInstrument.SNARE_DRUM);

            item(102, EnumInstrument.BASS_GUITAR);

            item(112, EnumInstrument.BELL);
            item(113, EnumInstrument.CHIME);
            item(114, EnumInstrument.BASS_DRUM);
            item(115, EnumInstrument.XYLOPHONE);
            item(116, EnumInstrument.BASS_DRUM);
            item(117, EnumInstrument.BASS_DRUM);
            item(118, EnumInstrument.XYLOPHONE);
        }

        static EnumInstrument get(int key) {
            if (MAP[0] == null) {
                init();
            }
            return MAP[key];
        }
    }
}
