package org.atcraftmc.starlight.internal.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.checkerframework.common.reflection.qual.ForName;

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
public final class Crafty {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final String PREFIX_NMS = "net.minecraft.server";
    private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
    private static final String CRAFT_SERVER = "CraftServer";
    private static final String VERSION;

    private Crafty() {
    }

    public static Class<?> needNMSClassOrElse(String nms, String... classNames) throws RuntimeException {
        Class<?> nmsClass = findNmsClass(nms);
        if (nmsClass != null) {
            return nmsClass;
        } else {
            for (String name : classNames) {
                Class<?> maybe = findClass(name);
                if (maybe != null) {
                    return maybe;
                }
            }

            throw new IllegalStateException(String.format("Couldn't find a class! NMS: '%s' or '%s'.", nms, Arrays.toString(classNames)));
        }
    }
    
    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var2) {
            return null;
        }
    }

    public static MethodHandle findMethod(Class<?> holderClass, String methodName, Class<?> returnClass, Class<?>... parameterClasses) {
        if (holderClass != null && returnClass != null) {
            for (Class<?> parameterClass : parameterClasses) {
                if (parameterClass == null) {
                    return null;
                }
            }

            try {
                return LOOKUP.findVirtual(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
            } catch (IllegalAccessException | NoSuchMethodException var8) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static MethodHandle findStaticMethod(Class<?> holderClass, String methodName, Class<?> returnClass, Class<?>... parameterClasses) {
        if (holderClass != null && returnClass != null) {
            for (Class<?> parameterClass : parameterClasses) {
                if (parameterClass == null) {
                    return null;
                }
            }

            try {
                return LOOKUP.findStatic(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
            } catch (IllegalAccessException | NoSuchMethodException var8) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Field findField(Class<?> holderClass, String fieldName) {
        return findField(holderClass, fieldName, null);
    }

    public static Field findField(Class<?> holderClass, String fieldName, Class<?> expectedType) {
        if (holderClass == null) {
            return null;
        } else {
            Field field;
            try {
                field = holderClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException var5) {
                return null;
            }

            field.setAccessible(true);
            return expectedType != null && !expectedType.isAssignableFrom(field.getType()) ? null : field;
        }
    }

    public static boolean isCraftBukkit() {
        return VERSION != null;
    }

    public static String findCraftClassName(String className) {
        return isCraftBukkit() ? PREFIX_CRAFTBUKKIT + VERSION + className : null;
    }

    public static Class<?> findCraftClass(String className) {
        String craftClassName = findCraftClassName(className);
        return craftClassName == null ? null : findClass(craftClassName);
    }

    public static Class<?> needCraftClass(String className) {
        return Objects.requireNonNull(findCraftClass(className), "Could not find org.bukkit.craftbukkit class " + className);
    }

    public static String findNmsClassName(String className) {
        return isCraftBukkit() ? PREFIX_NMS + VERSION + className : null;
    }

    public static Class<?> findNmsClass(String className) {
        String nmsClassName = findNmsClassName(className);
        return nmsClassName == null ? null : findClass(nmsClassName);
    }

    static {
        Class<?> serverClass = Bukkit.getServer().getClass();
        if (!serverClass.getSimpleName().equals(CRAFT_SERVER)) {
            VERSION = null;
        } else if (serverClass.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            VERSION = ".";
        } else {
            String name = serverClass.getName();
            name = name.substring("org.bukkit.craftbukkit".length());
            name = name.substring(0, name.length() - CRAFT_SERVER.length());
            VERSION = name;
        }

    }
}
