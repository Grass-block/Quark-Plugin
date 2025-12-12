package org.atcraftmc.starlight;

import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.music.MusicGame;
import org.atcraftmc.starlight.music.MusicPlayer;
import org.atcraftmc.starlight.music.MusicService;

import java.util.Set;

public final class SLMusicPackage extends MultiPackageProvider {
    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return Set.of(SLPackageBuilder.of("starlight-music", FeatureAvailability.BOTH, (i) -> {
            i.service(MusicService.class);

            i.module(MusicPlayer.class);
            i.module(MusicGame.class);

            i.config("starlight-music");
            i.language("starlight-music", "zh_cn");
        }));
    }
}
