package org.tbstcraft.quark.service.network.http;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public interface TokenStorage {
    HashMap<String, Integer> TOKENS = new HashMap<>();
    Runnable UPDATE_TASK = TokenStorage::update;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean verify(String token) {
        return TOKENS.containsKey(token);
    }

    static String create() {
        int lifetime = 1800;//todo: core config(seconds)

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
