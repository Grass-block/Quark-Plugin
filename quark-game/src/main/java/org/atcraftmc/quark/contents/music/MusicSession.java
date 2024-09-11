package org.atcraftmc.quark.contents.music;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.internal.task.TaskService;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class MusicSession implements Runnable {
    private final MusicPlayer module;
    private final Set<Player> players = new HashSet<>();
    private final AtomicInteger currentTick = new AtomicInteger(-1);

    private MusicData next;
    private boolean pause = false;
    private boolean cancel = false;
    private boolean active = false;
    private boolean killThread = false;

    private MusicData currentMusic;

    public MusicSession(MusicPlayer module) {
        this.module = module;
    }

    static String formatTime(long mss) {
        DecimalFormat fmt = new DecimalFormat("00");

        long minutes = (mss / (1000 * 60));
        long seconds = (mss % (1000 * 60)) / 1000;

        return fmt.format(minutes) + ":"
                + fmt.format(seconds);
    }

    //thread
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {

        String tid = "quark:midi:title@%s".formatted(hashCode());
        TaskService.async().timer(tid, 1, 5, () -> {
            if (this.currentMusic == null) {
                return;
            }
            for (Player p : this.players) {
                String template = Language.generateTemplate(this.module.getConfig(), "ui", (s) -> {
                    if (this.pause) {
                        s = s.replace("{msg#playing}", "{msg#paused}");
                    }
                    return s;
                });
                template = template.replace("{name}", currentMusic.getName().replace("_", " "))
                        .replace(
                                "{time}",
                                formatTime(currentMusic.getMillsLength() * currentTick.get() / currentMusic.getTickLength() / 1000)
                                )
                        .replace("{total}", formatTime(currentMusic.getMillsLength() / 1000));

                String ui = this.module.getLanguage().buildTemplate(Language.locale(p), template);
                Players.sendActionBarTitle(p, ui);
            }
        });


        while (true) {
            if (this.next == null) {
                try {
                    Thread.sleep(100);
                    Thread.yield();
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            MusicData data = this.next;
            this.next = null;

            if (this.killThread) {
                this.currentMusic = null;
                return;
            }
            this.playSelected(data);
        }
    }

    @SuppressWarnings("BusyWait")
    public void playSelected(MusicData current) {
        this.currentMusic = current;
        this.currentTick.set(0);

        try {
            this.active = true;
            int delayedTicks = 0;
            while (currentTick.get() < current.getTickLength() - 1) {
                if (this.killThread) {
                    this.currentMusic = null;
                    return;
                }

                if (this.cancel) {
                    this.cancel = false;
                    this.currentMusic = null;
                    this.active = false;
                    return;
                }

                while (this.pause) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    Thread.yield();
                }

                currentTick.incrementAndGet();
                if (current.getNodes().get(currentTick.get()) == null) {
                    delayedTicks++;
                    continue;
                }

                long delayMilliseconds = current.getMillsLength() * delayedTicks / current.getTickLength() / 1000;
                try {
                    Thread.sleep(delayMilliseconds);
                } catch (InterruptedException ignored) {
                }
                delayedTicks = 0;
                for (MusicNode node : current.getNodes().get(currentTick.get())) {
                    float power = node.getPower();
                    this.module.playNode(this.players, node.getNode(), current.getOffset(), node.getInstrument(), power);
                }
            }

        } catch (Exception ignored) {
        } finally {
            this.currentMusic = null;
            this.active = false;
        }
    }

    public void startSession() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stopSession() {
        this.killThread = true;
    }

    //control
    public void play(MusicData data) {
        if (this.active) {
            this.cancel();
        }
        this.next = data;
    }

    public void pause() {
        this.pause = true;
    }

    public void resume() {
        this.pause = false;
    }

    public void cancel() {
        this.next = null;
        this.cancel = true;
    }

    public Set<Player> getPlayers() {
        return this.players;
    }
}
