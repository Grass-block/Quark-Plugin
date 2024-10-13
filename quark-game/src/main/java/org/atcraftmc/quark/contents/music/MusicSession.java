package org.atcraftmc.quark.contents.music;

import org.bukkit.entity.Player;
import org.tbstcraft.quark.PlayerView;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
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

        return fmt.format(minutes) + ":" + fmt.format(seconds);
    }

    private String rendererID() {
        return "quark:music-player:ui@" + this.hashCode();
    }


    //thread
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
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

    public void render(Player player) {
        PlayerView.getInstance(player).getActionbar().addChannel(this.rendererID(), 5, 3, TaskService.async(), (a, t) -> {
            if (!this.active) {
                PlayerView.getInstance(a).getActionbar().removeChannel(this.rendererID());
                return;
            }
            if (this.currentMusic == null) {
                PlayerView.getInstance(a).getActionbar().removeChannel(this.rendererID());
                return;
            }
            renderUI(a);
        });
    }

    private void renderUI(Player player) {
        String template = Language.generateTemplate(this.module.getConfig(), "ui", (s) -> {
            if (this.pause) {
                s = s.replace("{msg#playing}", "{msg#paused}");
            }
            return s;
        });
        template = template.replace("{name}", currentMusic.getName().replace("_", " "))
                .replace("{time}", formatTime(currentMusic.getMillsLength() * currentTick.get() / currentMusic.getTickLength() / 1000))
                .replace("{total}", formatTime(currentMusic.getMillsLength() / 1000));

        String ui = this.module.getLanguage().buildTemplate(Language.locale(player), template);
        TextSender.sendActionbarTitle(player, TextBuilder.build(ui));
    }


    @SuppressWarnings("BusyWait")
    public void playSelected(MusicData current) {
        var isFirstMusicPlayed = false;

        for (Player p : this.players) {
            render(p);
        }

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
                    if (isFirstMusicPlayed) {
                        Thread.sleep(delayMilliseconds);
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {
                }
                delayedTicks = 0;
                for (MusicNode node : current.getNodes().get(currentTick.get())) {
                    float power = node.getPower();
                    this.module.playNode(this.players, node.getNode(), current.getOffset(), node.getInstrument(), power);
                    isFirstMusicPlayed = true;
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

    public void addPlayer(Player player) {
        this.players.add(player);
        if (!this.active) {
            return;
        }
        this.render(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        if (!this.active) {
            return;
        }
        PlayerView.getInstance(player).getActionbar().removeChannel(this.rendererID());
    }
}
