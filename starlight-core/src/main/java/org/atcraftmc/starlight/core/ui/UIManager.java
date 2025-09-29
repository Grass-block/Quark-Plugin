package org.atcraftmc.starlight.core.ui;

import me.gb2022.commons.container.ObjectContainer;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.core.ui.builder.UIBuilder;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@ApplicationService(id = "ui", impl = UIManager.Impl.class)
public interface UIManager extends SLService {
    @ServiceInject
    ServiceHolder<UIManager> INSTANCE = new ServiceHolder<>();

    static void init() {
        StarlightCommandManager.getInstance().register(new UICommand());
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

    void register(String id, AbstractUI ui);

    AbstractUI get(String id);

    void show(Player player, AbstractUI ui);

    void close(Player player);

    UIInstance current(Player player);

    final class Impl implements UIManager {
        private final Map<String, AbstractUI> ui = new HashMap<>();
        private final Map<Player, UIInstance> showing = new HashMap<>();

        @Override
        public void register(String id, AbstractUI ui) {
            this.ui.put(id, ui);
        }

        @Override
        public AbstractUI get(String id) {
            return this.ui.get(id);
        }

        @Override
        public void show(Player player, AbstractUI ui) {
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
