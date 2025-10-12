package org.atcraftmc.starlight.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.core.data.JDBCBasedDataService;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@ApplicationModule(id = "command-variables")
@CommandProvider(CommandVariables.VariableCommand.class)
@AutoRegister(Registrations.SERVER_EVENT)
public class CommandVariables extends SLPackageModule implements PluginCommandExecutor {
    public static final Pattern EXTRACT_VARIABLES = Pattern.compile("\\$\\{.*?}");

    private final Map<String, DataStorage> storages = new HashMap<>();

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        this.storages.put("plugin", new DataStorage.PluginLifetime());
        this.storages.put("persistent", new DataStorage.Persistent());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent e) {
        if (e.getCommand().startsWith("/variable") || e.getCommand().startsWith("variable")) {
            return;
        }
        e.setCommand(variables(e.getCommand()));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().startsWith("/variable") || e.getMessage().startsWith("variable")) {
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

        if (EXTRACT_VARIABLES.matcher(command).find()) {
            try {
                return variables(command);
            } catch (StackOverflowError e) {
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
                MessageAccessor.send(this.language, sender, "set", sid, name, value);
            }
            case "get" -> {
                var v = Objects.requireNonNullElse(data.get(name), "[null]");
                MessageAccessor.send(this.language, sender, "get", sid, name, v);
            }
            case "delete" -> {
                data.clear(name);
                MessageAccessor.send(this.language, sender, "delete", sid, name);
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

        class Persistent extends JDBCBasedDataService<String> implements DataStorage {
            private final Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(2)).build();

            protected Persistent() {
                super("");
            }

            @Override
            public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
                return conn.prepareStatement("create table sl_command_variables(name varchar(32),value varchar(256))");
            }


            @Override
            public String get(String name) {
                try {
                    return this.cache.get(name, () -> {
                        try (var p = this.connection.prepareStatement("select value from sl_command_variables where name = ?")) {
                            p.setString(1, name);

                            try (var r = p.executeQuery()) {
                                if (r.next()) {
                                    return r.getString(1);
                                }
                                return "undefined";
                            }
                        }
                    });
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
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
