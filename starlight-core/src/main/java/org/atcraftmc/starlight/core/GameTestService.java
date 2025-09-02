package org.atcraftmc.starlight.core;

import org.atcraftmc.starlight.framework.service.ServiceInject;

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


    @ServiceInject
    static void start(){

    }

    @ServiceInject
    static void stop(){

    }
}
