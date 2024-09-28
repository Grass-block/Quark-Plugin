package org.atcraftmc.quark.web;

import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.internal.task.TaskService;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public interface TokenStorageService extends Service {
    HashMap<String, Integer> TOKENS = new HashMap<>();
    Runnable UPDATE_TASK = TokenStorageService::update;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean verify(String token) {
        return TOKENS.containsKey(token);
    }

    @ServiceInject
    static void start() {
        TaskService.async().timer("quark_core:web:token_update", 0, 20, UPDATE_TASK);
    }

    @ServiceInject
    static void stop() {
        TaskService.async().cancel("quark_core:web:token_update");
    }

    static String create() {
        int lifetime = ConfigContainer.getInstance().getInt("quark-web","token-storage","expire");

        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String s = Base64.getEncoder().encodeToString(randomBytes);
        TOKENS.put(s, lifetime);
        return s;
    }

    static String destroy(String token) {
        TOKENS.remove(token);
        return token;
    }

    static void update() {
        for (String s : new ArrayList<>(TOKENS.keySet())) {
            Integer t = TOKENS.get(s);
            int time = t == null ? 114514 : t;
            if (time <= 0) {
                TOKENS.remove(s);
            }
            if (TOKENS.containsKey(s)) {
                TOKENS.put(s, time - 1);
            }
        }
    }

    static int remain(String token) {
        Integer t = TOKENS.get(token);
        return t == null ? -1 : t;
    }

    static int extend(String token, int extend) {
        Integer t = TOKENS.get(token);
        if (t == null) {
            return -1;
        }
        TOKENS.put(token, t + extend);
        return remain(token);
    }
}
