package org.tbstcraft.quark.internal;

import java.util.HashMap;
import java.util.Map;

public interface GameTestService {
    Map<String, Runnable> TESTS = new HashMap<>();

    static void register(String name, Runnable runnable) {
        TESTS.put(name, runnable);
    }

    static void unregister(String name) {
        TESTS.remove(name);
    }

    static void run(String name) {
        TESTS.get(name).run();
    }
}
