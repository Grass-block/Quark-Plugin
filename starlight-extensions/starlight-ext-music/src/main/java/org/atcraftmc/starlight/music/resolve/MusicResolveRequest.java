package org.atcraftmc.starlight.music.resolve;

public final class MusicResolveRequest {
    private final String actor;
    private final String music;
    private final int pitch;
    private final boolean dispatchInstrument;
    private final float speedMod;
    private final int interpolation;

    public MusicResolveRequest(String actor, String music, int pitch, boolean dispatchInstrument, float speedMod, int interpolation) {
        this.actor = actor;
        this.music = music;
        this.pitch = pitch;
        this.dispatchInstrument = dispatchInstrument;
        this.speedMod = speedMod;
        this.interpolation = interpolation;
    }

    public String actor() {
        return actor;
    }

    public String music() {
        return music;
    }

    public int pitch() {
        return pitch;
    }

    public boolean dispatchInstrument() {
        return dispatchInstrument;
    }

    public float speedMod() {
        return speedMod;
    }

    public int interpolation() {
        return interpolation;
    }


}
