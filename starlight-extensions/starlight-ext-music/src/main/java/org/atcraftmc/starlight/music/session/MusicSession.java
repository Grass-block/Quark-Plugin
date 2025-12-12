package org.atcraftmc.starlight.music.session;

import org.atcraftmc.starlight.core.PlayerView;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.music.*;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.resolve.MusicNode;
import org.atcraftmc.starlight.util.PlayerList;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicReference;

public abstract class MusicSession implements Runnable, MusicTask.TaskCallback {
    private final PlayerUIRenderer renderer;
    private final PlayerList players = new PlayerList();
    private final AtomicReference<MusicTask> current = new AtomicReference<>(null);

    public MusicSession(PlayerUIRenderer renderer) {
        this.renderer = renderer;
    }

    private String rendererID() {
        return "quark:music-player:ui@" + this.hashCode();
    }

    @Override
    public void start(MusicTask task, MusicData music) {
        for (var p : this.players.getPlayerObjects()) {
            startRender(p);
        }
    }

    @Override
    public void end(MusicTask task, MusicData music) {
        for (var player : this.players.getPlayerObjects()) {
            PlayerView.getInstance(player).getActionbar().removeChannel(this.rendererID());
        }
    }


    public void startRender(Player player) {
        PlayerView.getInstance(player).getActionbar().addChannel(this.rendererID(), 5, 3, TaskService.async(), (a, t) -> {
            var tt = this.current.get().getTick();
            this.renderer.renderUI(player, this.current.get().getMusic(), tt, this.current.get().paused());
        });
    }


    @Override
    public void renderNode(MusicTask task, MusicNode node, MusicData music) {
        float power = node.getPower();
        MusicService.playNode(this.players.getPlayerObjects(), node.getNode(), music.getOffset(), node.getInstrument(), power);
    }

    public void playSelected(MusicData current) {
        if (this.active()) {
            this.cancel();
        }
        this.current.set(new MusicTask(current, this));
        this.current.get().run();
        this.current.set(null);
    }

    public Thread startSession() {
        var thread = new Thread(this, this.getThreadId());
        thread.start();
        return thread;
    }

    public String getThreadId() {
        return "Starlight-MusicPlayer-" + this.hashCode();
    }

    public void destroySession() {
    }

    //control
    public abstract void play(MusicData data);

    public boolean active() {
        return this.current.get() != null;
    }

    public final void pause() {
        if (this.active()) {
            this.current.get().pause();
        }
    }

    public final void resume() {
        if (this.active()) {
            this.current.get().resume();
        }
    }

    public final void cancel() {
        if (this.active()) {
            this.current.get().stop();
        }
    }

    public final void addPlayer(Player player) {
        this.players.add(player);
        if (!this.active()) {
            return;
        }
        this.startRender(player);
    }

    public final void removePlayer(Player player) {
        this.players.remove(player);
        PlayerView.getInstance(player).getActionbar().removeChannel(this.rendererID());
    }

}
