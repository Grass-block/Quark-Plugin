package org.tbstcraft.quark.internal;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;

@QuarkModule(internal = true)
public final class PAPIGlobalVarsInjector extends PackageModule {
    private final PlaceholderSwapExtension extension = new PlaceholderSwapExtension();

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("me.clip.placeholderapi.PlaceholderAPI"));
    }

    @Override
    public void enable() {
        this.extension.register();
    }

    @Override
    public void disable() {
        this.extension.unregister();
    }

    public static class PlaceholderSwapExtension extends PlaceholderExpansion {
        @Override
        public @NotNull String getIdentifier() {
            return "quark-plugin-injector";
        }

        @Override
        public @NotNull String getAuthor() {
            return "_quark_plugin_framework";
        }

        @Override
        public @NotNull String getVersion() {
            return Quark.getInstance().getDescription().getVersion();
        }

        private String examine(String param, Player player) {
            if (!param.startsWith("quark:")) {
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
}
