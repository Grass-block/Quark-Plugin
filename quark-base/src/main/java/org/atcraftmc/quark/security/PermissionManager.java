package org.atcraftmc.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommandExecutor;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.permission.LazyPermissionEntry;
import org.tbstcraft.quark.internal.permission.PermissionEntry;
import org.tbstcraft.quark.internal.permission.PermissionValue;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({PermissionManager.PermissionCommand.class})
@QuarkModule(version = "1.0.3")
public final class PermissionManager extends PackageModule implements QuarkCommandExecutor {
    public static final HashMap<Permissible, PermissionEntry> CACHE = new HashMap<>();
    public static final HashMap<String, PermissionAttachment> ATTACHMENTS = new HashMap<>();
    private final Map<String, List<String>> tags = new HashMap<>();
    private final Map<String, ConfigurationSection> groups = new HashMap<>();


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

                getLogger().info("loaded configuration file %s as tag provider.".formatted(cfg));
                continue;
            }
            if (dom.contains("groups")) {
                ConfigurationSection group = dom.getConfigurationSection("groups");

                assert group != null;

                for (String groupName : group.getKeys(false)) {
                    this.groups.put(groupName, group.getConfigurationSection(groupName));
                }

                getLogger().info("loaded configuration file %s as group provider.".formatted(cfg));
                continue;
            }

            getLogger().info("skipped unknown config file %s".formatted(cfg));
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
            getLogger().info("set default permission group %s to %s".formatted(data.getString("group"), p.getName()));
            PlayerDataService.save(p.getName());
        }
        String group = data.getString("group");
        ConfigurationSection section = this.groups.get(group);

        if (section != null) {
            permissions.addAll(section.getStringList("permissions"));
            tags.addAll(section.getStringList("tags"));
        } else {
            getLogger().warning("detected unknown permission group of player %s : %s".formatted(p.getName(), group));
        }

        tags.addAll(getPermissionTags(p.getName()).getTagMap().keySet());
        Set<String> keys = new HashSet<>(data.getTagMap().keySet());
        keys.remove("group");
        keys.remove("tags");

        for (String key : keys) {
            permissions.add(data.getBoolean(key) ? "+" : "-" + key);
        }

        entry.clear();

        for (String tag : tags) {
            List<String> tagPermissions = this.tags.get(tag);
            if (tagPermissions == null) {
                getLogger().warning("find an unknown permission tag of player %s : %s".formatted(p.getName(), tag));
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

    public void addPermissionTag(String name, String tag) {
        getPermissionTags(name).setString(tag, tag);
        PlayerDataService.save(name);
    }

    public void removePermissionTag(String name, String tag) {
        getPermissionTags(name).remove(tag);
        PlayerDataService.save(name);
    }

    public void addOverridePermissionValue(String name, String permission, String value) {
        NBTTagCompound tag = PlayerDataService.getEntry(name, this.getId());
        if (Objects.equals(value, "unset")) {
            tag.remove(permission);
        } else {
            tag.setBoolean(permission, Boolean.parseBoolean(value));
        }
        Player p = Bukkit.getPlayerExact(name);

        if (p == null) {
            return;
        }
        PermissionEntry entry = get(p);
        entry.setPermission(permission, PermissionValue.parse(value));
        PlayerDataService.save(p.getName());
    }


    //command
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);

        switch (args[0]) {
            case "set" -> {
                this.addOverridePermissionValue(args[1], args[2], args[3]);
                this.getLanguage().sendMessage(sender, "cmd-perm-set", args[1], "{;}" + args[2], args[3]);
            }
            case "add-tag" -> {
                this.addPermissionTag(args[1], args[2]);
                this.getLanguage().sendMessage(sender, "cmd-tag-add", args[1], args[2]);
            }
            case "remove-tag" -> {
                this.removePermissionTag(args[1], args[2]);
                this.getLanguage().sendMessage(sender, "cmd-tag-remove", args[1], args[2]);
            }
            case "group" -> {
                NBTTagCompound tag = PlayerDataService.getEntry(args[1], this.getId());
                tag.setString("group", args[2]);
                this.getLanguage().sendMessage(sender, "cmd-group-set", args[1], args[2]);
            }
        }

        if (target == null) {
            return;
        }
        this.sync(target);
        LegacyCommandManager.sync();
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        switch (buffer.length) {
            case 1 -> {
                tabList.add("set");
                tabList.add("add-tag");
                tabList.add("remove-tag");
            }
            case 3 -> {
                switch (buffer[0]) {
                    case "set" -> tabList.addAll(PermissionEntry.getAllPermissions());
                    case "add-tag" -> tabList.addAll(this.tags.keySet());
                    case "group" -> tabList.addAll(this.groups.keySet());
                    case "remove-tag" -> tabList.addAll(getPermissionTags(sender.getName()).getTagMap().keySet());
                }
            }
            case 2 -> tabList.addAll(CachedInfo.getAllPlayerNames());
            case 4 -> {
                if (!Objects.equals(buffer[0], "set")) {
                    return;
                }
                tabList.add("true");
                tabList.add("false");
                tabList.add("unset");
            }
        }
    }

    @QuarkCommand(name = "permission", permission = "-quark.permission.command")
    public static final class PermissionCommand extends ModuleCommand<PermissionManager> {
        @Override
        public void init(PermissionManager module) {
            setExecutor(module);
        }
    }
}
