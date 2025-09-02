package org.atcraftmc.quark.commands;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@SLModule
@CommandProvider(CommandVariables.VariableCommand.class)
@AutoRegister(ServiceType.EVENT_LISTEN)
public class CommandVariables extends PackageModule implements PluginCommandExecutor {
    public static final Pattern EXTRACT_VARIABLES = Pattern.compile("\\$\\[.*?]");

    private final Map<String, DataStorage> storages = new HashMap<>();

    @Override
    public void enable() {
        this.storages.put("plugin", new DataStorage.PluginLifetime());
        this.storages.put("persistent", new DataStorage.Persistent());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent e) {
        if(e.getCommand().startsWith("/variable")||e.getCommand().startsWith("variable")){
            return;
        }
        e.setCommand(variables(e.getCommand()));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if(e.getMessage().startsWith("/variable")||e.getMessage().startsWith("variable")){
            return;
        }
        e.setMessage(variables(e.getMessage()));
    }

    public String variables(String command) {
        var matcher = EXTRACT_VARIABLES.matcher(command);

        while (matcher.find()) {
            var expr = matcher.group();
            var key = expr.substring(2, expr.length() - 1);

            String value = "null";

            for (var storage : this.storages.values()) {
                var v = storage.get(key);
                if (v != null) {
                    value = v;
                    break;
                }
            }

            command = command.replace(expr, value);
        }

        if(EXTRACT_VARIABLES.matcher(command).find()){
            try {
                return variables(command);
            }catch (StackOverflowError e){
                return command;
            }
        }

        return command;
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "set", "get", "delete");
        suggestion.suggest(1, storages.keySet());
        suggestion.suggest(3, "[value....]");

        if (suggestion.getBuffer().size() - 1 >= 2) {
            var storage = storages.get(suggestion.getBuffer().get(1));

            if (storage == null) {
                return;
            }

            suggestion.suggest(2, storage.list());
        }

    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();
        var sid = context.requireEnum(1, storages.keySet());
        var name = context.requireArgumentAt(2);
        var data = storages.get(sid);

        switch (context.requireEnum(0, "set", "delete", "get")) {
            case "set" -> {
                var value = context.requireRemainAsParagraph(3, true);
                data.set(name, value);
                MessageAccessor.send(this.getLanguage(), sender, "set", sid, name, value);
            }
            case "get" -> {
                var v = Objects.requireNonNullElse(data.get(name),"[null]");
                MessageAccessor.send(this.getLanguage(), sender, "get", sid, name, v);
            }
            case "delete" -> {
                data.clear(name);
                MessageAccessor.send(this.getLanguage(), sender, "delete", sid, name);
            }
        }
    }

    interface DataStorage {
        String get(String name);

        void set(String name, String value);

        default void clear(String name) {
            set(name, null);
        }

        Collection<String> list();

        class PluginLifetime extends HashMap<String, String> implements DataStorage {
            @Override
            public String get(String name) {
                return get(((Object) name));
            }

            @Override
            public void set(String name, String value) {
                put(name, value);
            }

            @Override
            public Collection<String> list() {
                return keySet();
            }

            @Override
            public void clear(String name) {
                remove(name);
            }
        }

        class Persistent implements DataStorage {

            @Override
            public String get(String name) {
                var data = ModuleDataService.get("variables");
                if (!data.hasKey(name)) {
                    return null;
                }
                return data.getString(name);
            }

            @Override
            public void set(String name, String value) {
                var data = ModuleDataService.get("variables");
                data.setString(name, value);
                data.save();
            }

            @Override
            public Collection<String> list() {
                var data = ModuleDataService.get("variables");
                return data.getTagMap().keySet();
            }

            @Override
            public void clear(String name) {
                var data = ModuleDataService.get("variables");
                data.remove(name);
                data.save();
            }
        }
    }

    @QuarkCommand(name = "variable", permission = "-quark.commands.variable")
    public static class VariableCommand extends ModuleCommand<CommandVariables> {
        @Override
        public void init(CommandVariables module) {
            setExecutor(module);
        }
    }
}
