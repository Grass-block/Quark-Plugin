package org.atcraftmc.starlight.internal;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.atcraftmc.qlib.platform.ForwardingPluginPlatform;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SLModule(internal = true)
public final class PAPISupport extends PackageModule {
    private final PlaceholderSwapExtension extension = new PlaceholderSwapExtension();

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("me.clip.placeholderapi.PlaceholderAPI"));
    }

    @Override
    public void enable() {
        this.extension.register();

        PluginPlatform.global().addAfter("starlight:core", "starlight:papi-inject", new PAPIEvalPlatform());
    }

    @Override
    public void disable() {
        this.extension.unregister();

        PluginPlatform.global().remove("starlight:papi-inject");
    }

    public static class PlaceholderSwapExtension extends PlaceholderExpansion {
        @Override
        public @NotNull String getIdentifier() {
            return "starlight-injector";
        }

        @Override
        public @NotNull String getAuthor() {
            return "ATCraftMC/Starlight";
        }

        @Override
        public @NotNull String getVersion() {
            return Starlight.instance().getDescription().getVersion();
        }

        private String examine(String param, Player player) {
            if (!param.startsWith("starlight:")) {
                return null;
            }

            String value;

            if ((value = PlaceHolderService.GLOBAL_VAR.get(param)) != null) {
                return value;
            }
            if ((value = PlaceHolderService.SERVER.get(param)) != null) {
                return value;
            }

            if (player == null) {
                return null;
            }
            if ((value = PlaceHolderService.PLAYER.get(param, player)) != null) {
                return value;
            }

            return null;
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
            return this.examine(params, null);
        }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
            return this.examine(params, player);
        }
    }

    public static class PAPIEvalPlatform extends ForwardingPluginPlatform {
        @Override
        public String globalFormatMessage(String source) {
            return super.globalFormatMessage(PlaceholderAPI.setPlaceholders(null, source));
        }
    }
}
