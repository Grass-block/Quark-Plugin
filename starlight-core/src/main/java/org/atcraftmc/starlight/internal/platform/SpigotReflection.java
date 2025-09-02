package org.atcraftmc.starlight.internal.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.joml.Vector3i;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/*
MIT License

Copyright (c) [year] [author]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

/**
 * from <a href="https://github.com/jpenilla/TabTPS">TabTPS</a>(MIT licence)<br>
 * Special thanks!
 */
public final class SpigotReflection {
    public static final SpigotReflection INSTANCE = new SpigotReflection();
    private static final Class<?> C_NMS = NMSAccessor.nms();
    private static final Class<?> C_CRAFT_PLAYER = Crafty.needCraftClass("entity.CraftPlayer");
    private static final Class<?> C_SERVER_PLAYER = Crafty.needNMSClassOrElse(
            "EntityPlayer",
            "net.minecraft.server.level.EntityPlayer",
            "net.minecraft.server.level.ServerPlayer"
    );
    private static final MethodHandle M_OBC_PLAYER_GET_HANDLE;
    private static final MethodHandle M_NMS_GET_SERVER;
    private static final Field F_SERVER_PLAYER_LATENCY;
    private static final Field F_NMS_RECENT_TPS;
    private static final Field F_NMS_RECENT_MSPT;

    static {
        M_OBC_PLAYER_GET_HANDLE = needMethod(C_CRAFT_PLAYER, "getHandle", C_SERVER_PLAYER);
        M_NMS_GET_SERVER = needStaticMethod(C_NMS, "getServer", C_NMS);
        F_NMS_RECENT_TPS = needField(C_NMS, "recentTps");
        F_SERVER_PLAYER_LATENCY = NMSAccessor.pingField();
        F_NMS_RECENT_MSPT = NMSAccessor.tickTimesField();
    }

    private SpigotReflection() {
    }

    public static SpigotReflection instance() {
        return INSTANCE;
    }

    static Vector3i getVersion() {
        var bukkitVersion = Bukkit.getVersion();

        var versionPattern = Pattern.compile("(?i)\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?(?: (Pre-Release|Release Candidate) )?(\\d)?\\)");
        var matcher = versionPattern.matcher(bukkitVersion);

        var version = 0;
        var patchVersion = 0;
        var preReleaseVersion = -1;

        if (!matcher.find()) {
            new Vector3i(0, 0, -1);
        }

        var matchResult = matcher.toMatchResult();

        try {
            version = Integer.parseInt(matchResult.group(2), 10);
        } catch (Exception ignored) {
        }

        if (matchResult.groupCount() >= 3) {
            try {
                patchVersion = Integer.parseInt(matchResult.group(3), 10);
            } catch (Exception ignored) {
            }
        }

        if (matchResult.groupCount() >= 5) {
            try {
                int ver = Integer.parseInt(matcher.group(5));
                if (matcher.group(4).toLowerCase(Locale.ENGLISH).contains("pre")) {
                    preReleaseVersion = ver;
                }
            } catch (Exception ignored) {
            }
        }


        return new Vector3i(version, patchVersion, preReleaseVersion);
    }

    private static MethodHandle needMethod(final Class<?> holderClass, final String methodName, final Class<?> returnClass, final Class<?>... parameterClasses) {
        return Objects.requireNonNull(
                Crafty.findMethod(holderClass, methodName, returnClass, parameterClasses),
                String.format(
                        "Could not locate method '%s' in class '%s'",
                        methodName,
                        holderClass.getCanonicalName()
                )
        );
    }

    private static MethodHandle needStaticMethod(final Class<?> holderClass, final String methodName, final Class<?> returnClass, final Class<?>... parameterClasses) {
        return Objects.requireNonNull(
                Crafty.findStaticMethod(holderClass, methodName, returnClass, parameterClasses),
                String.format(
                        "Could not locate static method '%s' in class '%s'",
                        methodName,
                        holderClass.getCanonicalName()
                )
        );
    }

    private static Field needField(final Class<?> holderClass, final String fieldName) {
        if (holderClass == null) {
            throw new NullPointerException("holderClass is null");
        }

        try {
            Field field = holderClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Throwable e) {
            throw new IllegalStateException(String.format(
                    "Unable to find field '%s' in class '%s'",
                    fieldName,
                    holderClass.getCanonicalName()
            ), e);
        }
    }

    private static Object invokeOrThrow(final MethodHandle methodHandle, final Object... params) {
        try {
            return params.length == 0 ? methodHandle.invoke() : methodHandle.invokeWithArguments(params);
        } catch (Throwable var3) {
            throw new IllegalStateException(String.format("Unable to invoke method with args '%s'", Arrays.toString(params)), var3);
        }
    }

    //access
    public int ping(final Player player) {
        if (F_SERVER_PLAYER_LATENCY == null) {
            throw new IllegalStateException("ServerPlayer_latency_field is null");
        } else {
            Object nmsPlayer = invokeOrThrow(M_OBC_PLAYER_GET_HANDLE, player);

            try {
                return F_SERVER_PLAYER_LATENCY.getInt(nmsPlayer);
            } catch (IllegalAccessException var4) {
                throw new IllegalStateException(String.format("Failed to get ping for player: '%s'", player.getName()), var4);
            }
        }
    }

    public double mspt() {
        if(F_NMS_RECENT_MSPT == null) {
            return 0.00;
        }

        var server = invokeOrThrow(M_NMS_GET_SERVER);

        try {
            long[] recentMspt = (long[]) F_NMS_RECENT_MSPT.get(server);
            return recentMspt[0] / 1000000f;
        } catch (Throwable var3) {
            return 0.00;
        }
    }

    public double tps() {
        Object server = invokeOrThrow(M_NMS_GET_SERVER);

        try {
            return (((double[]) F_NMS_RECENT_TPS.get(server)))[0];
        } catch (IllegalAccessException var3) {
            return 20.0;
        }
    }


    interface NMSAccessor {
        private static Field pingField() {
            var mojangMapped = Crafty.findField(C_SERVER_PLAYER, "latency");
            if (mojangMapped != null) {
                return mojangMapped;
            } else {
                return Crafty.findField(C_SERVER_PLAYER, "ping");
            }
        }

        /**
         * equals:
         * <code>
         * <br>int ver = getVersion().x();
         * <br>int ver = PaperLib.getMinecraftVersion();
         * <br>    String tickTimes;
         * <br>    if (ver < 13) { tickTimes = "h"; }
         * <br>    else if (ver == 13) { tickTimes = "d"; }
         * <br>    else if (ver != 14 && ver != 15) {
         * <br>        if (ver == 16) { tickTimes = "h";} else if (ver == 17) {
         * tickTimes = "n";
         * } else if (ver == 18) {
         * tickTimes = "o";
         * } else if (ver != 19 && (ver != 20 || PaperLib.getMinecraftPatchVersion() >= 3)) {
         * if (ver == 20 && PaperLib.getMinecraftPatchVersion() < 6) {
         * tickTimes = "ac";
         * } else {
         * if (ver != 20 && ver != 21) {
         * throw new IllegalStateException("Don't know tickTimes field name!");
         * }
         * <p>
         * tickTimes = "ab";
         * }
         * } else {
         * tickTimes = "k";
         * }
         * } else {
         * tickTimes = "f";
         * }
         * <p>
         * return needField(MinecraftServer_class, tickTimes);
         * </code>
         */

        private static Field tickTimesField() {
            var name = ((Supplier<String>) () -> {
                var version = getVersion().x();
                var patchVersion = getVersion().y();

                if (version == 13) {
                    return "d";
                }
                if (version == 14 || version == 15) {
                    return "f";
                }
                if (version == 16) {
                    return "h";
                }
                if (version == 17) {
                    return "n";
                }
                if (version == 18) {
                    return "o";
                }
                if (version == 19 || (version == 20 && patchVersion < 3)) {
                    return "k";
                }
                if (version == 20 && patchVersion < 6) {
                    return "ac";
                }
                if (version != 20 && version != 21) {
                    return null;
                }

                return "ab";
            }).get();

            if (name == null) {
                return null;
            }

            return needField(nms(), name);
        }

        static Class<?> nms() {
            return Crafty.needNMSClassOrElse("MinecraftServer", "net.minecraft.server.MinecraftServer");
        }
    }
}
