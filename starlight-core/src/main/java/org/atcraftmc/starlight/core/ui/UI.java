package org.atcraftmc.starlight.core.ui;

import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;
import org.atcraftmc.starlight.core.ui.element.SimpleElement;
import org.atcraftmc.starlight.core.ui.element.UIElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface UI {
    ElementCallback SOUND_DISABLE = UI.sound(Sound.BLOCK_NOTE_BLOCK_HARP, 0);
    ElementCallback SOUND_CLICK = UI.sound(Sound.UI_BUTTON_CLICK, 1);

    static Function<Player, String> value(String value) {
        return (p) -> value;
    }

    static ItemStack icon(Material icon) {
        return new ItemStack(icon);
    }

    static SimpleElement component(IconRenderer icon, TextRenderer r, LoreRenderer lr) {
        return new SimpleElement(icon, r, lr);
    }

    static UIElement command(IconRenderer stack, TextRenderer r, LoreRenderer lr, Function<Player, String> command) {
        return component(stack, r, lr).setCallback(command(command));
    }

    static UIElement connect(IconRenderer stack, TextRenderer r, LoreRenderer lr, Function<Player, String> server) {
        return component(stack, r, lr).setCallback(connect(server));
    }

    static UIElement close(IconRenderer stack, TextRenderer r, LoreRenderer lr) {
        return component(stack, r, lr).setCallback(close());
    }

    static ElementBuilder builder() {
        return new ElementBuilder();
    }

    static ElementCallback connect(Function<Player, String> server) {
        return (view, player, action) -> {
            var out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server.apply(player));
            player.sendPluginMessage(Starlight.instance(), "BungeeCord", out.toByteArray());
        };
    }

    static ElementCallback command(Function<Player, String> command) {
        return (view, player, action) -> TaskService.entity(player).run(() -> Bukkit.dispatchCommand(player, command.apply(player)));
    }

    static ElementCallback close() {
        return (view, player, action) -> view.close();
    }

    static ElementCallback forwarding(ElementCallback... callbacks) {
        return (view, player, action) -> {
            for (ElementCallback callback : callbacks) {
                callback.click(view, player, action);
            }
        };
    }

    static ItemStack enchanted(Material material) {
        var stack = new ItemStack(material);
        stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.addUnsafeEnchantment(Enchantment.LUCK, 1);
        CustomMeta.setItemPDCIdentifier(stack, "quark::menu");
        return stack;
    }

    static ElementCallback sound(Sound sound, int pitch) {
        return (view, player, action) -> player.playSound(player.getLocation(), sound, 1, pitch);
    }

    static ItemStack icon(Material material, int page) {
        var stack = new ItemStack(material, page);
        CustomMeta.setItemPDCIdentifier(stack, "quark::menu");
        return stack;
    }

    static void buildComponent(InventoryUI ui, int position, Consumer<ElementBuilder> o) {
        var builder = new ElementBuilder();
        o.accept(builder);
        builder.build(ui, position);
    }


    class ElementBuilder {
        private final List<ElementCallback> callbacks = new ArrayList<>();
        private final List<TextRenderer> lore = new ArrayList<>();
        private LoreRenderer finalLore;
        private TextRenderer name;
        private IconRenderer icon;


        public ElementBuilder name(TextRenderer name) {
            this.name = name;
            return this;
        }

        public ElementBuilder lore(TextRenderer lore) {
            this.lore.add(lore);
            return this;
        }

        public ElementBuilder finalLore(LoreRenderer flore) {
            this.finalLore = flore;
            return this;
        }

        public ElementBuilder icon(ItemStack icon) {
            this.icon = (v) -> icon;
            return this;
        }

        public ElementBuilder icon(IconRenderer icon) {
            this.icon = icon;
            return this;
        }

        public ElementBuilder operation(ElementCallback op) {
            this.callbacks.add(op);
            return this;
        }

        public UIElement build() {
            var lore = this.finalLore != null ? this.finalLore : LoreRenderer.forwarding(this.lore.toArray(new TextRenderer[0]));
            return component(this.icon, this.name, lore).setCallback(forwarding(this.callbacks.toArray(new ElementCallback[0])));
        }

        public ElementBuilder next() {
            return new ElementBuilder();
        }

        public ElementBuilder build(InventoryUI ui, int pos) {
            ui.setElement(pos, this.build());
            return this;
        }
    }
}
