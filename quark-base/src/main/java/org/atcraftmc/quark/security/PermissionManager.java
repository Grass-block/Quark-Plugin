package org.atcraftmc.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.data.storage.StorageTable;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.permission.PermissionEntry;
import org.atcraftmc.starlight.core.TaskService;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({PermissionManager.PermissionCommand.class})
@SLModule(version = "1.0.3")
public final class PermissionManager extends PackageModule implements PluginCommandExecutor, PermissionEventHandler {
    private final PlayerPermissionManager service = new PlayerPermissionManager(Starlight.instance(), this);
    private final Map<String, List<String>> tags = new HashMap<>();
    private final Map<String, ConfigurationSection> groups = new HashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    @Inject("permission;false")
    private AssetGroup permissionConfigs;

    private static void setPermission(PermissionAttachment attachment, List<String> permissions) {
        for (var item : permissions) {
            var name = item.substring(1);

            if (item.charAt(0) == '+') {
                attachment.setPermission(name, true);
            }
            if (item.charAt(0) == '-') {
                attachment.setPermission(name, false);
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

    public void sync(Player player, PermissionAttachment attachment) {
        var data = PlayerDataService.getEntry(player.getName(), this.getId());
        var permissions = new ArrayList<String>();
        var tags = new ArrayList<String>();

        if (!data.hasKey("group")) {
            data.setString("group", player.isOp() ? "--operator" : "--player");
            this.logger.info("set default permission group {} to {}", data.getString("group"), player.getName());
            PlayerDataService.save(player.getName());
        }

        var group = data.getString("group");
        var section = this.groups.get(group);

        if (section != null) {
            permissions.addAll(section.getStringList("permissions"));
            tags.addAll(section.getStringList("tags"));
        } else {
            this.logger.warn("detected unknown permission group of player {}: {}",player.getName(), group);
        }

        tags.addAll(getPermissionTags(player.getName()).getTagMap().keySet());

        var keys = new HashSet<>(data.getTagMap().keySet());

        keys.remove("group");
        keys.remove("tags");

        for (var key : keys) {
            try {
                permissions.add(data.getBoolean(key) ? "+" : "-" + key);
            } catch (ClassCastException ignored) {
                this.logger.warn("attempt to fetch invalid permission data {} for %{}", key, player.getName());
            }
        }

        for (var tag : tags) {
            var tagPermissions = this.tags.get(tag);
            if (tagPermissions == null) {
                this.logger.warn("find an unknown permission tag of player {} : {}", player.getName(), tag);
                continue;
            }

            setPermission(attachment, tagPermissions);
        }

        setPermission(attachment, permissions);
        TaskService.async().delay(10, LegacyCommandManager::sync);
    }

    @Override
    public void enable() {
        if (!this.permissionConfigs.existFolder()) {
            this.permissionConfigs.save("worldedit.yml");
            this.permissionConfigs.save("minecraft.yml");
            this.permissionConfigs.save("default-groups.yml");
        }

        for (String cfg : this.permissionConfigs.list()) {
            ConfigurationSection dom = YamlConfiguration.loadConfiguration(this.permissionConfigs.getFile(cfg));

            if (dom.contains("tags")) {
                ConfigurationSection tags = dom.getConfigurationSection("tags");

                assert tags != null;

                for (String tagName : tags.getKeys(false)) {
                    this.tags.put(tagName, dom.getStringList(tagName));

                    for (String item : tags.getStringList(tagName)) {
                        this.tags.get(tagName).add(item);
                    }
                }

                this.logger.info("loaded configuration file {} as tag provider.", cfg);
                continue;
            }
            if (dom.contains("groups")) {
                ConfigurationSection group = dom.getConfigurationSection("groups");

                assert group != null;

                for (String groupName : group.getKeys(false)) {
                    this.groups.put(groupName, group.getConfigurationSection(groupName));
                }

                this.logger.info("loaded configuration file {} as group provider.", cfg);
                continue;
            }

            this.logger.info("skipped unknown config file {}", cfg);
        }

        this.service.initialize();
    }

    @Override
    public void disable() {
        this.service.release();
    }

    //modify
    private StorageTable getPermissionTags(String name) {
        var data = PlayerDataService.get(name);
        if (!data.hasKey("tags")) {
            data.setTag("tags", new NBTTagCompound());
        }
        return data.getTable("tags");
    }

    //command
    @Override
    public void execute(CommandExecution context) {
        var target = context.requireOfflinePlayer(1);
        var playerName = context.requireArgumentAt(1);
        var name = context.requireArgumentAt(2);
        var sender = context.getSender();
        var data = PlayerDataService.get(target);
        var table = data.getTable(this.getId());

        switch (context.requireEnum(0, "set", "add-tag", "remove-tag", "group")) {
            case "set" -> {
                var value = context.requireArgumentAt(3);

                if (Objects.equals(value, "unset")) {
                    table.remove(name);
                } else {
                    table.setBoolean(name, Boolean.parseBoolean(value));
                }

                MessageAccessor.send(this.language, sender, "cmd-perm-set", playerName, "{;}" + name, value);
            }
            case "add-tag" -> {
                getPermissionTags(playerName).setString(name, name);
                MessageAccessor.send(this.language, sender, "cmd-tag-add", playerName, name);
            }
            case "remove-tag" -> {
                getPermissionTags(playerName).remove(name);
                MessageAccessor.send(this.language, sender, "cmd-tag-remove", playerName, name);
            }
            case "group" -> {
                data.setString("group", name);
                MessageAccessor.send(this.language, sender, "cmd-group-set", playerName, name);
            }
        }

        data.save();
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
}
