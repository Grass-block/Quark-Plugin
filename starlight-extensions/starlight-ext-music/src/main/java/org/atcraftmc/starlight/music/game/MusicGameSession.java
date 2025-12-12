package org.atcraftmc.starlight.music.game;

import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.music.MusicGame;
import org.atcraftmc.starlight.music.PlayerUIRenderer;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.music.session.MusicSession;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class MusicGameSession extends MusicSession {
    private final MusicData data;
    private final Location origin;
    private final Player audience;
    private final boolean edit;

    public MusicGameSession(PlayerUIRenderer renderer, MusicData data, Location origin, Player audience, boolean edit) {
        super(renderer);
        this.data = data;
        this.origin = origin;
        this.audience = audience;
        this.edit = edit;
        this.addPlayer(audience);
    }

    public static Location getSelectedLocation(Player player) {
        var eye = player.getEyeLocation();

        var dx = 8;
        var pitch = eye.getYaw() + 270;

        var dz = Math.tan(Math.toRadians(pitch)) * dx;

        if (Double.isInfinite(dz) || Double.isNaN(dz)) {
            return null;
        }

        if (Math.abs(dz) > 2) {
            dz = dz > 0 ? 2 : -2;
        }

        var bx = Math.round(eye.getX() + dx);
        var bz = Math.round(eye.getZ() - 0.5 + dz);

        return new Location(eye.getWorld(), bx, eye.getBlockY() - 3, bz);
    }

    @Override
    public void play(MusicData data) {
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TaskService.entity(this.audience).timer("sl:music-game-timer", 1, 1, () -> {
            this.audience.setVelocity(new Vector(0.3, 0, 0));

            var loc = getSelectedLocation(this.audience);
            if (loc == null) {
                return;
            }

            this.audience.spawnParticle(
                    Particle.END_ROD,
                    loc.getBlockX() + 0.5,
                    loc.getBlockY() + 0.5,
                    loc.getBlockZ() + 0.5,
                    0,
                    0,
                    0,
                    0,
                    0,
                    null
            );
        });
        this.playSelected(this.data);
        TaskService.entity(this.audience).cancel("sl:music-game-timer");
    }

    public void punch(MusicGame.MusicGamePunchItem pi, Player player, Location loc) {
        loc = MusicGameSession.getSelectedLocation(player);

        if (loc == null) {
            return;
        }

        if (this.edit) {
            loc.getBlock().setType(Material.BLUE_STAINED_GLASS);
        } else {
            loc.getBlock().breakNaturally();
        }

        var px = loc.getBlockX();
        var py = loc.getBlockY();
        var pz = loc.getBlockZ();

        var rx = px - this.origin.getBlockX();
        var ry = py - this.origin.getBlockY();
        var rz = pz - this.origin.getBlockZ();

        player.sendMessage("%d/%d/%d".formatted(rx, ry, rz));
    }
}
