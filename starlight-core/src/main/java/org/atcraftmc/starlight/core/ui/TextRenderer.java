package org.atcraftmc.starlight.core.ui;

import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.bukkit.entity.Player;

import java.util.function.Function;

@FunctionalInterface
public interface TextRenderer extends Function<InventoryUIView, Component> {
    static TextRenderer literal(Component component) {
        return (player) -> component;
    }

    static TextRenderer literal(String text) {
        var c = TextBuilder.buildComponent(text);
        return (player) -> c;
    }

    static TextRenderer data(LanguageItem item, Object... format) {
        return (v) -> item.component(LocaleService.locale(v.getViewer()), format).asComponent();
    }

    static TextRenderer data(Function<InventoryUIView, LanguageItem> provider, Object... format) {
        return (v) -> provider.apply(v).component(LocaleService.locale(v.getViewer()), format).asComponent();
    }

    default Component applyVirtually(Player player) {
        return this.apply(new InventoryUIView(54, player, Component.empty()));
    }
}
