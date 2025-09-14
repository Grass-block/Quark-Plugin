package org.atcraftmc.starlight.security;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.bukkit.util.permission.PermissionEventHandler;
import org.atcraftmc.qlib.bukkit.util.permission.PlayerPermissionManager;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.core.JDBCService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.data.JDBCBasedDataService;
import org.atcraftmc.starlight.core.permission.PermissionEntry;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@AutoRegister(Registers.BUKKIT_EVENT)
@CommandProvider({PermissionManager.PermissionCommand.class})
@SLModule(version = "1.0.3")
public final class PermissionManager extends PackageModule implements PluginCommandExecutor, PermissionEventHandler {
    private final PlayerPermissionManager service = new PlayerPermissionManager(Starlight.instance(), this);
    private final Map<String, List<String>> tags = new HashMap<>();
    private final Map<String, ConfigurationSection> groups = new HashMap<>();
    private final PermissionStorageService dataService = new PermissionStorageService();

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    private static void setPermission(Map<String, Boolean> attachment, List<String> permissions) {
        for (var item : permissions) {
            var name = item.substring(1);

            if (item.charAt(0) == '+') {
                attachment.put(name, true);
            }
            if (item.charAt(0) == '-') {
                attachment.put(name, false);
            }
        }
    }

    @Override
    public void onAttachmentCreated(UUID uuid, PermissionAttachment attachment) {
        var player = Bukkit.getPlayer(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player not found: " + uuid);
        }

        this.sync(player, attachment);
    }

    public PermissionData data(OfflinePlayer player) {
        try {
            if (!this.dataService.exist(player.getUniqueId())) {
                this.dataService.add(player.getUniqueId(), new PermissionData(player.isOp() ? "--operator" : "--player"));
                this.logger.info("created permission data for {}({})", player.getName(), player.getUniqueId());
            }

            return this.dataService.get(player.getUniqueId()).orElseThrow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void sync(Player player, PermissionAttachment attachment) {
        var data = data(player);
        var tags = new ArrayList<String>();
        var map = new HashMap<String, Boolean>();
        var groupData = this.groups.get(data.group);

        if (groupData != null) {
            setPermission(map, groupData.getStringList("permissions"));
            tags.addAll(groupData.getStringList("tags"));
        } else {
            this.logger.warn("detected unknown permission group of player {}: {}", player.getName(), data.group);
        }

        tags.addAll(data.tags);

        for (var tag : tags) {
            if (tag.trim().isEmpty()) {
                continue;
            }

            var tagPermissions = this.tags.get(tag);
            if (tagPermissions == null) {
                this.logger.warn("find an unknown permission tag of player {} : {}", player.getName(), tag);
                continue;
            }

            setPermission(map, tagPermissions);
        }

        for (var s : data.allowedPermissions) {
            map.put(s, true);
        }
        for (var s : data.disallowedPermissions) {
            map.put(s, false);
        }

        map.forEach(attachment::setPermission);

        TaskService.async().delay(10, LegacyCommandManager::sync);
    }

    @Override
    public void enable() {
        try {
            this.dataService.init(JDBCService.getDB(JDBCService.SL_LOCAL).orElseThrow());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Configurations.groupedYML("permission-tags", Set.of()).forEach((k, v) -> {
            for (String tagName : v.getKeys(false)) {
                this.tags.put(tagName, v.getStringList(tagName));
            }
        });

        Configurations.groupedYML("permission-groups", Set.of()).forEach((k, v) -> {
            for (String groupName : v.getKeys(false)) {
                this.groups.put(groupName, v.getConfigurationSection(groupName));
            }
        });

        this.service.initialize();
    }

    @Override
    public void disable() {
        this.service.release();
    }

    //command
    @Override
    public void execute(CommandExecution context) {
        var target = context.requireOfflinePlayer(1);
        var playerName = context.requireArgumentAt(1);
        var name = context.requireArgumentAt(2);
        var sender = context.getSender();
        var data = data(target);

        switch (context.requireEnum(0, "set", "add-tag", "remove-tag", "group")) {
            case "set" -> {
                var value = context.requireArgumentAt(3);
                var id = context.requireArgumentAt(2);

                if (Objects.equals(value, "unset")) {
                    data.allowedPermissions.remove(id);
                    data.disallowedPermissions.remove(id);
                } else {
                    if (Boolean.parseBoolean(value)) {
                        data.allowedPermissions.add(id);
                        data.disallowedPermissions.remove(id);
                    } else {
                        data.disallowedPermissions.add(id);
                        data.allowedPermissions.remove(id);
                    }
                }

                MessageAccessor.send(this.language, sender, "cmd-perm-set", playerName, "{;}" + name, value);
            }
            case "add-tag" -> {
                data.tags.add(name);
                MessageAccessor.send(this.language, sender, "cmd-tag-add", playerName, name);
            }
            case "remove-tag" -> {
                data.tags.remove(name);
                MessageAccessor.send(this.language, sender, "cmd-tag-remove", playerName, name);
            }
            case "group" -> {
                data.group = name;
                MessageAccessor.send(this.language, sender, "cmd-group-set", playerName, name);
            }
        }

        try {
            this.dataService.update(target.getUniqueId(), data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (!target.isOnline()) {
            return;
        }

        var attachment = this.service.attachment(target.getPlayer());
        this.sync(Objects.requireNonNull(target.getPlayer()), attachment);
        attachment.refresh();
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "set", "add-tag", "remove-tag", "group");
        suggestion.matchArgument(0, "set", (c) -> c.suggest(2, PermissionEntry.getAllPermissions()));
        suggestion.matchArgument(0, "add-tag", (c) -> c.suggest(2, this.tags.keySet()));
        suggestion.matchArgument(0, "group", (c) -> c.suggest(2, this.groups.keySet()));
        suggestion.matchArgument(0, "remove-tag", (c) -> c.suggest(2, "<tag-name>"));
        suggestion.suggestPlayers(1);
        suggestion.matchArgument(0, "set", (c) -> c.suggest(3, "true", "false", "unset"));
    }

    @QuarkCommand(name = "permission", permission = "-quark.permission.command")
    public static final class PermissionCommand extends ModuleCommand<PermissionManager> {
        @Override
        public void init(PermissionManager module) {
            setExecutor(module);
        }
    }


    public static final class PermissionData {
        private final Set<String> tags;
        private final Set<String> allowedPermissions;
        private final Set<String> disallowedPermissions;
        private String group;

        public PermissionData(String group, Set<String> tags, Set<String> allowedPermissions, Set<String> disallowedPermissions) {
            this.group = group;
            this.tags = tags;
            this.allowedPermissions = allowedPermissions;
            this.disallowedPermissions = disallowedPermissions;
        }

        public PermissionData(String group) {
            this(group, new HashSet<>(), new HashSet<>(), new HashSet<>());
        }
    }

    public static final class PermissionStorageService extends JDBCBasedDataService<PermissionData> {
        public PermissionStorageService() {
            super("_null_");
        }

        @Override
        public PreparedStatement attemptCreateTable(Connection conn) throws SQLException {
            var sql = """
                        CREATE TABLE SL_PERMISSION(
                            uuid CHAR(36) PRIMARY KEY,
                            perm_group VARCHAR(32),
                            perm_tags VARCHAR(512),
                            perm_allowed VARCHAR(1024),
                            perm_disallowed VARCHAR(1024)
                        )
                    """;

            return conn.prepareStatement(sql);
        }

        @Override
        public void encode(PreparedStatement ps, PermissionData data) throws SQLException {
            ps.setString(1, data.group);
            ps.setString(2, String.join(";", data.tags));
            ps.setString(3, String.join(";", data.allowedPermissions));
            ps.setString(4, String.join(";", data.disallowedPermissions));
        }

        @Override
        public PermissionData decode(ResultSet rs) throws SQLException {
            var group = rs.getString("perm_group");
            var tags = new HashSet<>(List.of(rs.getString("perm_tags").split(";")));
            var allowedPermissions = new HashSet<>(List.of(rs.getString("perm_allowed").split(";")));
            var disallowedPermissions = new HashSet<>(List.of(rs.getString("perm_disallowed").split(";")));

            return new PermissionData(group, tags, allowedPermissions, disallowedPermissions);
        }

        public boolean exist(UUID uuid) throws SQLException {
            try (var p = connection.prepareStatement("SELECT uuid FROM SL_PERMISSION WHERE uuid = ? LIMIT 1")) {
                p.setString(1, uuid.toString());
                try (var rs = p.executeQuery()) {
                    return rs.next();
                }
            }
        }

        public boolean add(UUID uuid, PermissionData data) throws SQLException {
            if (exist(uuid)) {
                return false;
            }

            try (var p = this.connection.prepareStatement(
                    "INSERT INTO SL_PERMISSION (perm_group, perm_tags, perm_allowed, perm_disallowed,uuid) VALUES (?, ?, ?, ?, ?)")) {
                encode(p, data);
                p.setString(5, uuid.toString());
                return p.executeUpdate() > 0;
            }
        }

        public boolean update(UUID uuid, PermissionData data) throws SQLException {
            var sql = "UPDATE SL_PERMISSION SET perm_group = ?, perm_tags = ?, perm_allowed = ?, perm_disallowed = ? WHERE uuid = ?";

            try (var ps = connection.prepareStatement(sql)) {
                encode(ps, data);
                ps.setString(5, uuid.toString());
                return ps.executeUpdate() > 0;
            }
        }

        public Optional<PermissionData> get(UUID uuid) throws SQLException {
            try (var p = connection.prepareStatement("SELECT * FROM SL_PERMISSION WHERE uuid = ? LIMIT 1")) {
                p.setString(1, uuid.toString());
                try (var rs = p.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(decode(rs));
                    }
                    return Optional.empty();
                }
            }
        }
    }
}
