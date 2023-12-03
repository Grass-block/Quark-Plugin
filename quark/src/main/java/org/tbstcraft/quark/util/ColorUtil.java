package org.tbstcraft.quark.util;

public class ColorUtil {
    public static final String DEFAULT = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BACKGROUND_BLACK = "\u001B[40m";
    public static final String BACKGROUND_RED = "\u001B[41m";
    public static final String BACKGROUND_GREEN = "\u001B[42m";
    public static final String BACKGROUND_YELLOW = "\u001B[43m";
    public static final String BACKGROUND_BLUE = "\u001B[44m";
    public static final String BACKGROUND_PURPLE = "\u001B[45m";
    public static final String BACKGROUND_CYAN = "\u001B[46m";
    public static final String BACKGROUND_WHITE = "\u001B[47m";

    public static byte[] int1ToByte3(int c) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        return new byte[] { r, g, b };
    }

    public static byte[] int1ToByte4(int c) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        return new byte[] { r, g, b, 127 };
    }

    public static byte[] int1ToByte4_n(int c, int a) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        return new byte[] { r, g, b, (byte) a };
    }

    public static float[] byte3ToFloat3(byte r, byte g, byte b) {
        float r2 = (r & 0xFF) / 255.0f;
        float g2 = (g & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;
        return new float[] { r2, g2, b2, 1.0f };
    }

    public static float[] byte4ToFloat4(byte r, byte g, byte b, byte a) {
        float r2 = (r & 0xFF) / 255.0f;
        float g2 = (g & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;
        float a2 = (a & 0xFF) / 255.0f;
        return new float[] { r2, g2, b2, a2 };
    }

    public static float[] int1Byte1ToFloat4(int c, byte alpha) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        float r2 = (r & 0xFF) / 255.0f;
        float g2 = (g & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;
        return new float[] { r2, g2, b2, (alpha & 0xFF) / 255f };
    }

    public static float[] int1Float1ToFloat4(int c, float alpha) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        float r2 = (r & 0xFF) / 255.0f;
        float g2 = (g & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;
        return new float[] { r2, g2, b2, alpha };
    }

    public static int float3toInt1(float r, float g, float b) {
        byte r2 = (byte) (r * 255);
        byte g2 = (byte) (g * 255);
        byte b2 = (byte) (b * 255);
        return (b2 & 0xFF) | ((g2 & 0xFF) << 8) | ((r2 & 0xFF) << 16);
    }

    public static float[] int1ToFloat3(int c) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        float r2 = (r & 0xFF) / 255.0f;
        float g2 = (g & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;
        return new float[] { r2, g2, b2 };
    }

    public static byte[] int1Byte1ToByte4(int c, byte alpha) {
        byte r = (byte) (c >> 16);
        byte g = (byte) (c >> 8);
        byte b = (byte) c;
        return new byte[] { r, g, b, alpha };
    }
}
