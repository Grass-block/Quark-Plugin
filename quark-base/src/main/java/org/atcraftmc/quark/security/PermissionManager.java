package org.atcraftmc.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.permission.LazyPermissionEntry;
import org.tbstcraft.quark.internal.permission.PermissionEntry;
import org.tbstcraft.quark.internal.permission.PermissionValue;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({PermissionManager.PermissionCommand.class})
@QuarkModule(version = "1.0.3")
public final class PermissionManager extends PackageModule implements QuarkCommandExecutor {
    public static final HashMap<Permissible, PermissionEntry> CACHE = new HashMap<>();
    public static final HashMap<String, PermissionAttachment> ATTACHMENTS = new HashMap<>();
    private final Map<String, List<String>> tags = new HashMap<>();
    private final Map<String, ConfigurationSection> groups = new HashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    @Inject("permission;false")
    private AssetGroup permissionConfigs;

    static PermissionEntry get(Permissible target) {
        if (CACHE.containsKey(target)) {
            return CACHE.get(target);
        }
        PermissionEntry entry = new LazyPermissionEntry(target);
        CACHE.put(target, entry);
        return entry;
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

                this.logger.info("loaded configuration file %s as tag provider.".formatted(cfg));
                continue;
            }
            if (dom.contains("groups")) {
                ConfigurationSection group = dom.getConfigurationSection("groups");

                assert group != null;

                for (String groupName : group.getKeys(false)) {
                    this.groups.put(groupName, group.getConfigurationSection(groupName));
                }

                this.logger.info("loaded configuration file %s as group provider.".formatted(cfg));
                continue;
            }

            this.logger.info("skipped unknown config file %s".formatted(cfg));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            attach(p);
        }
    }

    @Override
    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            detach(p);
        }

        for (PermissionEntry entry : CACHE.values()) {
            entry.getAttachment().remove();
        }

        CACHE.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.attach(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.detach(event.getPlayer());
    }


    //attachment
    public void attach(Player player) {
        PermissionAttachment attachment = player.addAttachment(Quark.getInstance());
        ATTACHMENTS.put(player.getName(), attachment);
        this.sync(player);
    }

    public void detach(Player player) {
        PermissionAttachment attachment = ATTACHMENTS.get(player.getName());
        if (attachment == null) {
            return;
        }
        ATTACHMENTS.remove(player.getName());
        player.removeAttachment(attachment);
        player.recalculatePermissions();
    }

    public void sync(Player p) {
        PermissionEntry entry = get(p);
        NBTTagCompound data = PlayerDataService.getEntry(p.getName(), this.getId());

        var permissions = new ArrayList<String>();
        var tags = new ArrayList<String>();

        if (!data.hasKey("group")) {
            data.setString("group", p.isOp() ? "--operator" : "--player");
            this.logger.info("set default permission group %s to %s".formatted(data.getString("group"), p.getName()));
            PlayerDataService.save(p.getName());
        }
        String group = data.getString("group");
        ConfigurationSection section = this.groups.get(group);

        if (section != null) {
            permissions.addAll(section.getStringList("permissions"));
            tags.addAll(section.getStringList("tags"));
        } else {
            this.logger.warn("detected unknown permission group of player %s : %s".formatted(p.getName(), group));
        }

        tags.addAll(getPermissionTags(p.getName()).getTagMap().keySet());

        var keys = new HashSet<>(data.getTagMap().keySet());

        keys.remove("group");
        keys.remove("tags");

        for (String key : keys) {
            try {
                permissions.add(data.getBoolean(key) ? "+" : "-" + key);
            } catch (ClassCastException ignored) {
                //this.logger.warning("attempt to fetch invalid permission data %s for %s".formatted(key, p.getName()));;
            }
        }

        entry.clear();

        for (String tag : tags) {
            List<String> tagPermissions = this.tags.get(tag);
            if (tagPermissions == null) {
                this.logger.warn("find an unknown permission tag of player %s : %s".formatted(p.getName(), tag));
                continue;
            }

            setPermission(entry, tagPermissions);
        }

        setPermission(entry, permissions);
        LegacyCommandManager.sync();
        p.recalculatePermissions();
    }

    private void setPermission(PermissionEntry entry, List<String> permissions) {
        for (String item : permissions) {
            String name = item.substring(1);

            if (item.charAt(0) == '+') {
                entry.setPermission(name, PermissionValue.TRUE);
            }
            if (item.charAt(0) == '-') {
                entry.setPermission(name, PermissionValue.FALSE);
            }
        }
    }


    //modify
    private NBTTagCompound getPermissionTags(String name) {
        NBTTagCompound tag = PlayerDataService.getEntry(name, this.getId());
        if (!tag.hasKey("tags")) {
            tag.setTag("tags", new NBTTagCompound());
        }
        return tag.getCompoundTag("tags");
    }


    //command
    @Override
    public void execute(CommandExecution context) {
        var playerName = context.requireArgumentAt(1);
        var target = context.requireOfflinePlayer(1).getPlayer();
        var name = context.requireArgumentAt(2);
        var sender = context.getSender();

        var data = PlayerDataService.get(playerName).getTable(this.getId());

        switch (context.requireEnum(0, "set", "add-tag", "remove-tag", "group")) {
            case "set" -> {
                var value = context.requireArgumentAt(3);

                if (Objects.equals(value, "unset")) {
                    data.remove(name);
                } else {
                    data.setBoolean(name, Boolean.parseBoolean(value));
                }

                if (target != null) {
                    PermissionEntry entry = get(target);
                    entry.setPermission(name, PermissionValue.parse(value));
                }

                this.language.sendMessage(sender, "cmd-perm-set", playerName, "{;}" + name, value);
            }
            case "add-tag" -> {
                getPermissionTags(playerName).setString(name, name);
                this.language.sendMessage(sender, "cmd-tag-add", playerName, name);
            }
            case "remove-tag" -> {
                getPermissionTags(playerName).remove(name);
                this.language.sendMessage(sender, "cmd-tag-remove", playerName, name);
            }
            case "group" -> {
                data.setString("group", name);
                this.language.sendMessage(sender, "cmd-group-set", playerName, name);
            }
        }

        PlayerDataService.save(playerName);
        if (target == null) {
            return;
        }
        this.sync(target);
        LegacyCommandManager.sync();
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
