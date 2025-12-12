package org.atcraftmc.starlight.music.session;

import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.resolve.MusicNode;
import org.atcraftmc.starlight.util.AsyncLock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class MusicTask {
    private final AtomicBoolean cancel = new AtomicBoolean(false);
    private final AtomicInteger tick = new AtomicInteger(0);
    private final AsyncLock lock = new AsyncLock();
    private final MusicData music;
    private final TaskCallback callback;

    public MusicTask(MusicData music, TaskCallback callback) {
        this.music = music;
        this.callback = callback;
    }

    public void pause() {
        this.lock.pause();
    }

    public void resume() {
        this.lock.resume();
    }

    public void stop() {
        this.cancel.set(true);
    }

    public int getTick() {
        return tick.get();
    }

    public MusicData getMusic() {
        return music;
    }

    public void run() {
        var delayedTicks = 0;
        var firstNodePlayed = false;
        var t = TaskService.async().timer(0, 10, () -> this.callback.tick(this, this.music));

        try {
            this.callback.start(this, this.music);

            while (this.tick.get() < this.music.getTickLength() - 1) {
                if (this.cancel.get()) {
                    return;
                }

                this.lock.monitor();

                this.tick.incrementAndGet();
                if (this.music.getNodes().get(this.tick.get()) == null) {
                    delayedTicks++;
                    continue;
                }

                var delayPercentage = ((float) delayedTicks) / this.music.getTickLength();
                var delayMilliseconds = this.music.getTempo() != -1 ? (long) (this.music.getTempo() * delayedTicks) : (long) (this.music.getMillsLength() * delayPercentage);

                try {
                    if (firstNodePlayed) {
                        Thread.sleep(delayMilliseconds);

                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {
                }

                delayedTicks = 0;

                for (var node : this.music.getNodes().get(this.tick.get())) {
                    this.callback.renderNode(this, node, this.music);
                    firstNodePlayed = true;
                }
            }

        } catch (Exception ignored) {
        } finally {
            t.cancel();
            this.callback.end(this, this.music);
        }
    }

    public boolean paused() {
        return this.lock.paused();
    }

    public interface TaskCallback {
        void renderNode(MusicTask task, MusicNode node, MusicData music);

        default void tick(MusicTask task, MusicData music) {
        }

        default void start(MusicTask task, MusicData music) {
        }

        default void end(MusicTask task, MusicData music) {
        }
    }
}
