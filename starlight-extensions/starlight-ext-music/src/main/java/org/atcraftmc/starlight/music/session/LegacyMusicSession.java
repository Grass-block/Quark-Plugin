package org.atcraftmc.starlight.music.session;

import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.PlayerUIRenderer;
import org.atcraftmc.starlight.util.AsyncLock;

import java.util.concurrent.atomic.AtomicBoolean;

public final class LegacyMusicSession extends MusicSession {
    private MusicData next;
    private AsyncLock sleepLock = new AsyncLock();
    private AtomicBoolean running = new AtomicBoolean(true);

    public LegacyMusicSession(PlayerUIRenderer renderer) {
        super(renderer);
    }

    @Override
    public void destroySession() {
        this.running.set(false);
        this.sleepLock.resume();
        this.cancel();
    }

    @Override
    public void play(MusicData data) {
        this.next = data;
        if (this.active()) {
            this.cancel();
        }
    }

    public void run() {
        while (this.running.get()) {
            if (this.next == null) {
                this.sleepLock.monitor();
                continue;
            }

            MusicData data = this.next;
            this.next = null;

            this.playSelected(data);
        }
    }
}
