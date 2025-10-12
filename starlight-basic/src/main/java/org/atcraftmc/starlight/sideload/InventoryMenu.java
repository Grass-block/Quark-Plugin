package org.atcraftmc.starlight.sideload;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import org.atcraftmc.starlight.core.permission.PermissionService;
import org.atcraftmc.starlight.core.ui.*;
import org.atcraftmc.starlight.core.ui.element.ElementCallback;
import org.atcraftmc.starlight.core.ui.view.InventoryUIView;
import org.atcraftmc.starlight.framework.module.SLCommandModule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ApplicationModule(id = "inventory-menu")
@AutoRegister(Registrations.SERVER_EVENT)
@QuarkCommand(name = "gui", permission = "-starlight.gui.admin")
public final class InventoryMenu extends SLCommandModule {
    private final Map<String, MenuData> menus = new HashMap<>();
    private final Map<String, Component> components = new HashMap<>();

    @Inject
    private Logger logger;

    public static TextRenderer text(ConfigurationSection section) {
        return text(section.getValues(false));
    }

    public static TextRenderer text(Map<String, ?> section) {
        if (section.containsKey("text")) {
            return TextRenderer.literal(TextBuilder.buildComponent(section.get("text").toString()));
        }
        if (section.containsKey("lang")) {
            var key = Objects.requireNonNull(section.get("lang").toString()).split(":");
            return TextRenderer.data(Starlight.lang().item(key[0], key[1], key[2]));
        }
        throw new IllegalArgumentException("unexpected text component");
    }

    public static ElementCallback operations(List<String> list) {
        var result = new ElementCallback[list.size()];

        for (int i = 0; i < list.size(); i++) {
            var encoded = list.get(i);

            if (encoded.startsWith("[close]")) {
                result[i] = UI.close();
                continue;
            }
            if (encoded.startsWith("[command]")) {
                var line = encoded.substring(10);
                result[i] = UI.command(UI.value(line));
                continue;
            }
            if (encoded.startsWith("[sound]")) {
                var line = encoded.substring(7);
                var sound = Sound.valueOf(line.replace(".", "_").toUpperCase());
                result[i] = UI.sound(sound, 1);
            }
            if (encoded.startsWith("[connect]")) {
                var line = encoded.substring(9);
                result[i] = UI.connect(UI.value(line));
            }
        }

        return UI.forwarding(result);
    }

    public static IconRenderer icon(ConfigurationSection section) {
        var type = section.getString("type");

        @SuppressWarnings("DataFlowIssue") var amount = section.contains("amount") ? Integer.parseInt(((String) section.get("amount"))) : 1;

        var material = Material.getMaterial(Objects.requireNonNull(type).toUpperCase());
        var stack = section.getBoolean("enchanted", false) ? UI.enchanted(material) : UI.icon(material);
        CustomMeta.setItemPDCIdentifier(stack, "quark::menu");
        stack.setAmount(amount);

        return (v) -> stack;
    }

    public static ElementCallback callbacks(ConfigurationSection component) {
        var left = new ElementCallback() {
            private final ElementCallback data = operations(component.getStringList("left-click"));

            @Override
            public void click(InventoryUIView view, Player player, InventoryAction action) {
                if (action == InventoryAction.PICKUP_ALL) {
                    this.data.click(view, player, action);
                }
            }
        };

        var right = new ElementCallback() {
            private final ElementCallback data = operations(component.getStringList("right-click"));

            @Override
            public void click(InventoryUIView view, Player player, InventoryAction action) {
                if (action == InventoryAction.PICKUP_HALF) {
                    this.data.click(view, player, action);
                }
            }
        };

        var both = operations(component.getStringList("any-click"));

        return UI.forwarding(left, right, both);
    }

    public static LoreRenderer lore(List<?> list) {
        if (list.isEmpty()) {
            return LoreRenderer.none();
        }

        var result = new TextRenderer[list.size()];

        for (int i = 0; i < list.size(); i++) {
            var sect = list.get(i);
            result[i] = text((Map<String, ?>) sect);
        }

        return LoreRenderer.forwarding(result);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        var inv = event.getPlayer().getInventory();
        for (var i = 0; i < inv.getSize(); i++) {
            var item = inv.getItem(i);
            if (item == null) {
                continue;
            }
            if (Objects.equals(CustomMeta.getItemPDCIdentifier(item), "quark::menu")) {
                inv.setItem(i, null);
            }
        }
    }

    public void load() {
        this.unload();

        var uiDefaults = Set.of("inventory-ui/example-menu.yml", "inventory-ui/view-distance-selector.yml", "inventory-ui/warp-menu.yml");
        var compDefaults = Set.of("inventory-ui-component/default-components.yml", "inventory-ui-component/default-opens.yml");
        var uis = Configurations.groupedYML("inventory-ui", uiDefaults);
        var comps = Configurations.groupedYML("inventory-ui-component", compDefaults);

        comps.forEach((id, dom) -> loadComponents(Objects.requireNonNull(dom), id));
        uis.forEach((id, dom) -> loadPage(Objects.requireNonNull(dom), id));

        for (var md : this.menus.values()) {
            md.register();
        }
    }

    public void unload() {
        for (var md : this.menus.values()) {
            md.destroy();
        }
        this.components.clear();
        this.menus.clear();
    }


    @Override
    public void enable() throws Exception {
        super.enable();
        this.load();
    }

    @Override
    public void disable() throws Exception {
        super.disable();
        this.unload();
    }

    public void loadPage(ConfigurationSection sect, String file) {
        try {
            var id = sect.getString("id");
            var capacity = sect.getInt("capacity");
            var perm = sect.getString("permission", "");
            var commands = sect.getStringList("commands");
            var title = text(Objects.requireNonNull(sect.getConfigurationSection("title")));
            var open = operations(sect.getStringList("open"));
            var ui = new InventoryUI(capacity, title, open);
            var permission = perm.isEmpty() ? null : PermissionService.getPermission(perm);

            this.menus.put(id, new MenuData(ui, permission, commands));

            var components = Objects.requireNonNull(sect.getConfigurationSection("components"));

            for (var idx : components.getKeys(false)) {
                for (var pos : idx.split(",")) {
                    if (components.isString(idx)) {
                        this.components.get(components.getString(idx)).build(ui, Integer.parseInt(pos));
                        continue;
                    }

                    var section = Objects.requireNonNull(components.getConfigurationSection(idx));

                    UI.buildComponent(ui, Integer.parseInt(pos), (b) -> {
                        b.icon(icon(Objects.requireNonNull(section.getConfigurationSection("icon"))));
                        b.name(text(Objects.requireNonNull(section.getConfigurationSection("name"))));
                        b.finalLore(lore(Objects.requireNonNull(section.getList("lore", new ArrayList<>()))));
                        b.operation(callbacks(section));
                    });
                }
            }

            this.logger.info("loaded page configuration {} ({})", id, file);
        } catch (Exception e) {
            this.logger.info("found error while loading page {}:", file);
            this.logger.catching(e);
        }
    }

    public void loadComponents(ConfigurationSection sect, String file) {
        for (var s : sect.getKeys(false)) {
            this.components.put(s, new Component(Objects.requireNonNull(sect.getConfigurationSection(s))));
        }

        this.logger.info("component provider {}", file);
    }

    @Override
    public void execute(CommandExecution context) {
        switch (context.requireEnum(0, "open", "reload")) {
            case "open" -> this.menus.get(context.requireArgumentAt(1)).open(context.requireSenderAsPlayer());
            case "load" -> load();
        }
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.matchArgument(0, "open", (ctx) -> ctx.suggest(1, this.menus.keySet()));
        suggestion.suggest(0, "open", "reload");
    }

    public static final class MenuData {
        private final List<UIOpenCommand> commands = new ArrayList<>();
        private final InventoryUI ui;
        private final Permission permission;

        public MenuData(InventoryUI ui, Permission permission, List<String> commands) {
            this.ui = ui;
            this.permission = permission;

            try {

                for (var command : commands) {
                    this.commands.add(new UIOpenCommand(command.substring(1), this));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void open(Player player) {
            if (this.permission != null && !player.hasPermission(this.permission)) {
                Starlight.instance().getCommandManager().sendPermissionMessage(player, this.permission.getName());
                return;
            }

            this.ui.createInventoryUI(player).open();
        }

        public void register() {
            for (var command : this.commands) {
                Starlight.instance().getCommandManager().register(command);
            }
        }

        public void destroy() {
            for (var command : this.commands) {
                Starlight.instance().getCommandManager().unregister(command);
            }
        }
    }

    public static final class Component {
        private final IconRenderer icon;
        private final TextRenderer name;
        private final LoreRenderer lore;
        private final ElementCallback callbacks;

        public Component(ConfigurationSection section) {
            this.icon = icon(Objects.requireNonNull(section.getConfigurationSection("icon")));
            this.name = text(Objects.requireNonNull(section.getConfigurationSection("name")));
            this.lore = lore(Objects.requireNonNull(section.getList("lore", new ArrayList<>())));
            this.callbacks = callbacks(section);
        }

        public void build(InventoryUI ui, int slot) {
            UI.buildComponent(ui, slot, (b) -> {
                b.icon(this.icon);
                b.name(this.name);
                b.finalLore(this.lore);
                b.operation(this.callbacks);
            });
        }
    }

    @QuarkCommand(name = "__")
    public static final class UIOpenCommand extends AbstractCommand {
        private final String name;
        private final MenuData ref;

        public UIOpenCommand(String name, MenuData ref) {
            this.name = name;
            this.ref = ref;
        }

        @Override
        public void execute(CommandExecution context) {
            this.ref.open(context.requireSenderAsPlayer());
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }
    }
}
