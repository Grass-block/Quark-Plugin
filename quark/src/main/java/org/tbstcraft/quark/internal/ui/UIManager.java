package org.tbstcraft.quark.internal.ui;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.internal.ui.builder.UIBuilder;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.util.*;

@QuarkService(id = "ui", impl = UIManager.Impl.class)
public interface UIManager extends Service {
    ObjectContainer<UIManager> INSTANCE = new ObjectContainer<>();

    static void init() {
        CommandManager.registerCommand(new UICommand());
        /*
        registerUI("quark:debug", new InventoryUIBuilder(27)
                .title((p) -> "Debug")
                .listener(7, 2, ((p, ui, x, y) -> p.sendMessage("raw event (7,2) clicked!")))
                .close(8, 2)
                .command(0, 0, new InventoryIcon((p) -> "Click to reload quark", Material.COMMAND_BLOCK), "quark reload")
                .initializer((p) -> p.sendMessage("debug menu opened")));

         */
    }

    static void stop() {
        INSTANCE.get().closeAll();
    }

    static void registerUI(String s, UIBuilder builder) {
        INSTANCE.get().register(s, builder.build());
    }

    void closeAll();

    default void show(Player player, String id) {
        this.show(player, this.get(id));
    }

    default void show(Player player, UIBuilder builder) {
        this.show(player, builder.build());
    }

    void register(String id, UI ui);

    UI get(String id);

    void show(Player player, UI ui);

    void close(Player player);

    UIInstance current(Player player);

    final class Impl implements UIManager {
        private final Map<String, UI> ui = new HashMap<>();
        private final Map<Player, UIInstance> showing = new HashMap<>();

        @Override
        public void register(String id, UI ui) {
            this.ui.put(id, ui);
        }

        @Override
        public UI get(String id) {
            return this.ui.get(id);
        }

        @Override
        public void show(Player player, UI ui) {
            UIInstance instance = ui.render(player);
            instance.onOpen();
            this.showing.put(player, instance);
        }

        @Override
        public void close(Player player) {
            this.current(player).close();
            this.showing.remove(player);
        }

        @Override
        public UIInstance current(Player player) {
            return this.showing.get(player);
        }

        @Override
        public void closeAll() {
            for (Player p : new HashSet<>(this.showing.keySet())) {
                this.close(p);
            }
        }
    }


    @QuarkCommand(name = "ui", permission = "-quark.ui")
    final class UICommand extends CoreCommand {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (Objects.equals(args[0], "debug")) {
                INSTANCE.get().show(((Player) sender), "quark:debug");
                return;
            }
            INSTANCE.get().show(((Player) sender), args[0]);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length != 1) {
                return;
            }
            tabList.add("debug");
        }
    }
}
