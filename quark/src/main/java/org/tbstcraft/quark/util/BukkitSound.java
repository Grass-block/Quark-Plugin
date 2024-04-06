package org.tbstcraft.quark.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum BukkitSound {
    ANNOUNCE(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
    DENY(Sound.BLOCK_NOTE_BLOCK_BASS),
    WARP(Sound.ENTITY_ENDERMAN_TELEPORT),
    ;

    final Sound content;

    BukkitSound(Sound sound) {
        this.content = sound;
    }

    static void play(Player p, BukkitSound sound) {
        p.playSound(p.getLocation(), sound.content, 1, 0);
    }

    public void play(Player p) {
        play(p, this);
    }
}
