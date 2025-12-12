package org.atcraftmc.starlight.game.mug;

import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.session.MusicSession;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class MusicGameSession {
    private final Player player;
    private final MusicSession musicSession;
    private final MusicData data;
    private final AtomicInteger tick = new AtomicInteger(0);

    public MusicGameSession(Player player, MusicSession musicSession, MusicData data) {
        this.player = player;
        this.musicSession = musicSession;
        this.data = data;
    }

    public void start(){
        TaskService.global().timer(()->{

        });
    }



}
