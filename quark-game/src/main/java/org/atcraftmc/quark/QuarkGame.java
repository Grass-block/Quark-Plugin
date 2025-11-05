package org.atcraftmc.quark;

import org.atcraftmc.quark.contents.*;
import org.atcraftmc.quark.contents.music.MusicPlayer;
import org.atcraftmc.quark.storage.ItemDropSecure;
import org.atcraftmc.quark.storage.PortableFunctionalBlocks;
import org.atcraftmc.quark.storage.PortableShulkerBox;
import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.SLPackageProvider;

import java.util.Set;

@SLPackageProvider
public final class QuarkGame extends MultiPackageProvider {
    public static Set<SLPackageInitializer> initializers() {
        return Set.of(
                SLPackageBuilder.of("quark-tweaks", FeatureAvailability.BOTH, (i) -> {
                    i.module("time-scale", TimeScale.class);
                    i.module("vein-miner", VeinMiner.class);
                    i.module("fly-speed-modifier", FlySpeedModifier.class);
                    i.module("freecam", FreeCam.class);
                    i.module("crop-click-harvest", CropClickHarvest.class);
                    i.module("double-door-sync", DoubleDoorSync.class);
                    i.module("entity-leash", EntityLeash.class);
                    i.module("dispenser-block-placer", DispenserBlockPlacer.class);
                    i.module("realistic-sleep", RealisticSleep.class);

                    i.language("quark-tweaks", "zh_cn");
                    i.config("quark-tweaks");
                }),
                SLPackageBuilder.of("quark-contents", FeatureAvailability.BOTH, (i) -> {
                    i.module("elevator", Elevator.class);
                    i.module("stair-seat", StairSeat.class);
                    i.module("realistic-minecart", RealisticMinecart.class);
                    i.module("114514", _114514.class);
                    i.module("neko", Neko.class);
                    i.module("elytra-aeronautics", ElytraAeronautics.class);
                    i.module("music-player", MusicPlayer.class);
                    i.module("hats", Hats.class);
                    i.module("sit-on-player", SitOnPlayer.class);

                    i.language("quark-contents", "zh_cn");
                    i.language("quark-contents", "en_us");
                    i.config("quark-contents");
                }),
                SLPackageBuilder.of("quark-storage", FeatureAvailability.BOTH, (i) -> {
                    i.module("portable-shulker-box", PortableShulkerBox.class);
                    i.module("portable-functional-blocks", PortableFunctionalBlocks.class);
                    i.module("item-drop-secure", ItemDropSecure.class);
                    i.language("quark-storage", "zh_cn");
                    i.config("quark-storage");
                })
        );
    }

    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return initializers();
    }

}
