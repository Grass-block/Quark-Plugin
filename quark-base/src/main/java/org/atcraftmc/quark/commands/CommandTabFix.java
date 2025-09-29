package org.atcraftmc.quark.commands;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.starlight.core.TaskService;
import me.gb2022.modular.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.util.FilePath;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@ApplicationModule(version = "1.2.0")
@AutoRegister(Registrations.SERVER_EVENT)
@ComponentProvider({CommandTabFix.WEAddition.class,})
public final class CommandTabFix extends SLPackageModule {
    public static boolean isCommandNameMatch(TabCompleteEvent event, String... names) {
        for (var name : names) {
            if (event.getBuffer().startsWith(name) || event.getBuffer().startsWith("/" + name)) {
                return true;
            }
        }

        return false;
    }

    public static String[] getArguments(TabCompleteEvent event) {
        String[] args = event.getBuffer().split(" ");
        if (args.length <= 1) {
            return new String[0];
        }

        return args;
    }

    public static String getLastArgument(TabCompleteEvent event) {
        var args = getArguments(event);

        return args[args.length - 1];
    }

    public static void handleCompletion(TabCompleteEvent event, Consumer<List<String>> action) {
        var list = new ArrayList<>(event.getCompletions());
        action.accept(list);
        event.setCompletions(list);
    }

    @Override
    public void enable() {
        TaskService.global().delay(1000, LegacyCommandManager::sync);
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.event.server.TabCompleteEvent"));
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        List<String> match = new ArrayList<>();

        String[] args = event.getBuffer().split(" ");
        if (args.length <= 1) {
            return;
        }
        String lastArg = args[args.length - 1];

        if (event.getBuffer().charAt(event.getBuffer().length() - 1) != ' ') {
            for (String s : event.getCompletions()) {
                if (!s.contains(lastArg)) {
                    continue;
                }
                match.add(s);
            }
            event.setCompletions(match);
        }

        if (isCommandNameMatch(event, "reload")) {
            if (!event.getCompletions().contains("confirm")) {
                List<String> list = new ArrayList<>(event.getCompletions());
                list.add("confirm");
                event.setCompletions(list);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        LegacyCommandManager.sync();
    }

    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class WEAddition extends SLModuleComponent<CommandTabFix> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requirePlugin("WorldEdit");
        }

        @EventHandler
        public void onTabComplete(TabCompleteEvent event) {
            if (isCommandNameMatch(event, "/schem", "/schematic")) {
                if (!(getLastArgument(event).equals("load") || getLastArgument(event).equals("delete"))) {
                    return;
                }

                var folder = new File(FilePath.pluginsFolder() + "/WorldEdit/schematics");
                handleCompletion(event, (list) -> {
                    for (File f : Objects.requireNonNull(folder.listFiles())) {
                        list.add(f.getName());
                    }
                });
            }

            if (isCommandNameMatch(event, "/set") && getArguments(event).length <= 2) {
                handleCompletion(event, (list) -> list.add("hand"));
            }

            if (isCommandNameMatch(event, "/replace") && getArguments(event).length <= 3) {
                handleCompletion(event, (list) -> list.add("hand"));
            }
        }
    }

    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class MVAddition extends SLModuleComponent<CommandTabFix> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requirePlugin("Multiverse-Core");
        }

        @EventHandler
        public void onTabComplete(TabCompleteEvent event) {

        }
    }
}
