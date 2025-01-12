package org.atcraftmc.quark;

import org.atcraftmc.quark.contents.*;
import org.atcraftmc.quark.contents.music.MusicPlayer;
import org.atcraftmc.quark.storage.ItemDropSecure;
import org.atcraftmc.quark.storage.PortableFunctionalBlocks;
import org.atcraftmc.quark.storage.PortableShulkerBox;
import org.atcraftmc.quark.tweaks.*;
import org.atcraftmc.quark.warps.BackToDeath;
import org.atcraftmc.quark.warps.RTP;
import org.atcraftmc.quark.warps.TPA;
import org.atcraftmc.quark.warps.Waypoint;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.initializer.PackageBuilderInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkGame extends MultiPackageProvider {
    public static Set<PackageInitializer> initializers() {
        return Set.of(
                PackageBuilderInitializer.of("quark-tweaks", FeatureAvailability.BOTH, (i) -> {
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
                PackageBuilderInitializer.of("quark-contents", FeatureAvailability.BOTH, (i) -> {
                    i.module("elevator", Elevator.class);
                    i.module("stair-seat", StairSeat.class);
                    i.module("realistic-minecart", RealisticMinecart.class);
                    i.module("custom-recipe", CustomRecipe.class);
                    i.module("114514", _114514.class);
                    i.module("neko", Neko.class);
                    i.module("elytra-aeronautics", ElytraAeronautics.class);
                    i.module("music-player", MusicPlayer.class);
                    i.module("hats", Hats.class);
                    i.module("sit-on-player", SitOnPlayer.class);

                    i.language("quark-contents", "zh_cn");
                    i.config("quark-contents");
                }),
                PackageBuilderInitializer.of("quark-storage", FeatureAvailability.BOTH, (i) -> {
                    i.module("portable-shulker-box", PortableShulkerBox.class);
                    i.module("portable-functional-blocks", PortableFunctionalBlocks.class);
                    i.module("item-drop-secure", ItemDropSecure.class);
                    i.language("quark-storage", "zh_cn");
                    i.config("quark-storage");
                }),
                PackageBuilderInitializer.of("quark-warps", FeatureAvailability.BOTH, (i) -> {
                    i.module("waypoint", Waypoint.class);
                    i.module("back-to-death", BackToDeath.class);
                    i.module("rtp", RTP.class);
                    i.module("tpa", TPA.class);

                    i.language("quark-warps", "zh_cn");
                    i.config("quark-warps");
                })
        );
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }

}
