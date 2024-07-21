package org.tbstcraft.quark.internal.audience;

import net.kyori.adventure.audience.Audience;

import java.util.Locale;

public final class PlayerAudience {
    private final Audience target;

    public PlayerAudience(Audience target) {
        this.target = target;
    }

    public Locale getLanguage() {
        return null;
    }


    public void render() {

    }
}
