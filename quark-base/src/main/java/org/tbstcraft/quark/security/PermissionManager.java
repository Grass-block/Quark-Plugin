package org.tbstcraft.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.PlayerDataService;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.foundation.command.*;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.permission.PermissionEntry;
import org.tbstcraft.quark.internal.permission.PermissionValue;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({PermissionManager.PermissionCommand.class})
@QuarkModule(version = "1.0.3")
public final class PermissionManager extends PackageModule implements CommandExecutor {
    public static final HashMap<String, PermissionAttachment> ATTACHMENTS = new HashMap<>();
    private final Map<String, List<String>> tags = new HashMap<>();

    @Inject("permission;false")
    private AssetGroup permissionConfigs;

    @Override
    public void enable() {
        if (!this.permissionConfigs.existFolder()) {
            this.permissionConfigs.save("worldedit.yml");
            this.permissionConfigs.save("minecraft.yml");
        }

        for (String cfg : this.permissionConfigs.list()) {
            ConfigurationSection dom = YamlConfiguration.loadConfiguration(this.permissionConfigs.getFile(cfg)).getConfigurationSection("tags");

            assert dom != null;

            for (String tagName : dom.getKeys(false)) {
                this.tags.put(tagName, dom.getStringList(tagName));

                for (String item : dom.getStringList(tagName)) {
                    this.tags.get(tagName).add(item);
                }
            }

            getLogger().info("loaded configuration file %s.".formatted(cfg));
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
    }

    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        this.attach(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.detach(event.getPlayer());
    }

    public void attach(Player player) {
        PermissionAttachment attachment = player.addAttachment(Quark.PLUGIN);
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
        PermissionEntry entry = PermissionEntry.get(p);
        NBTTagCompound tag = PlayerDataService.getEntry(p.getName(), this.getId());

        entry.clear();

        for (String t : getPermissionTags(p.getName()).getTagMap().keySet()) {
            List<String> tagPermissions = this.tags.get(t);

            if (tagPermissions == null) {
                getLogger().warning("find an unknown permission tag: " + t);
                continue;
            }

            for (String item : tagPermissions) {
                String name = item.substring(1);

                if (item.charAt(0) == '+') {
                    entry.setPermission(name, PermissionValue.TRUE);
                }
                if (item.charAt(0) == '-') {
                    entry.setPermission(name, PermissionValue.FALSE);
                }
            }
        }

        Set<String> keys = new HashSet<>(tag.getTagMap().keySet());
        keys.remove("group");
        keys.remove("tags");
        for (String s : keys) {
            entry.setPermission(s, PermissionValue.parse(tag.getBoolean(s)));
        }

        entry.refresh();
    }

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
        Player p = PlayerUtil.strictFindPlayer(name);

        if (p == null) {
            return;
        }
        PermissionEntry entry = PermissionEntry.get(p);
        entry.setAndRefresh(permission, PermissionValue.parse(value));
        PlayerDataService.save(p.getName());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target = PlayerUtil.strictFindPlayer(args[1]);

        switch (args[0]) {
            case "set" -> {
                this.addOverridePermissionValue(args[1], args[2], args[3]);
                this.getLanguage().sendMessage(sender, "cmd-perm-set", args[1], args[2], args[3]);
            }
            case "add-tag" -> {
                this.addPermissionTag(args[1], args[2]);
                this.getLanguage().sendMessage(sender, "cmd-tag-add", args[1], args[2]);
            }
            case "remove-tag" -> {
                this.removePermissionTag(args[1], args[2]);
                this.getLanguage().sendMessage(sender, "cmd-tag-remove", args[1], args[2]);
            }
        }

        if (target == null) {
            return;
        }
        this.sync(target);
        CommandManager.sync();
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
