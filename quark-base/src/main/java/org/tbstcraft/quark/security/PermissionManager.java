package org.tbstcraft.quark.security;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.service.base.permission.PermissionEntry;
import org.tbstcraft.quark.service.base.permission.PermissionValue;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.util.platform.PlayerUtil;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.*;

@ModuleService(ServiceType.EVENT_LISTEN)
@CommandProvider({PermissionManager.PermissionCommand.class})
@QuarkModule(version = "1.0.3")
public final class PermissionManager extends PackageModule {
    public static final HashMap<String, PermissionAttachment> ATTACHMENTS = new HashMap<>();

    @Override
    public void enable() {
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
    public void onPlayerJoin(PlayerJoinEvent event) {
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

        ConfigurationSection groups = this.getConfig().getConfigurationSection("groups");
        if (groups != null) {
            String group = tag.getString("group");
            if (group == null || !groups.contains(group) || group.isEmpty()) {
                group = this.getConfig().getString("default-group");
                tag.setString("group", group);
                PlayerDataService.save(p.getName());
                Quark.LOGGER.warning("fixed up permission group of %s to %s.".formatted(p.getName(), group));
            }

            assert group != null;
            ConfigurationSection groupSection = groups.getConfigurationSection(group);
            if (groupSection != null) {
                entry.clear();

                for (String s1 : Objects.requireNonNull(groupSection.getStringList("allow"))) {
                    entry.setPermission(s1, PermissionValue.TRUE);
                }
                for (String s1 : Objects.requireNonNull(groupSection.getStringList("disallow"))) {
                    entry.setPermission(s1, PermissionValue.FALSE);
                }
            }
        }

        //override
        Set<String> keys = new HashSet<>(tag.getTagMap().keySet());
        keys.remove("group");
        for (String s : keys) {
            entry.setPermission(s, PermissionValue.parse(tag.getBoolean(s)));
        }

        entry.refresh();
    }

    public PermissionAttachment getAttachment(Player player) {
        return ATTACHMENTS.get(player.getName());
    }

    public void setPermissionGroup(String name, String group) {
        NBTTagCompound tag = PlayerDataService.getEntry(name, this.getId());
        tag.setString("group", group);
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

    @QuarkCommand(name = "permission", permission = "-quark.permission.command")
    public static final class PermissionCommand extends ModuleCommand<PermissionManager> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            TaskService.asyncTask(() -> {
                switch (args[0]) {
                    case "set" -> {
                        this.getModule().addOverridePermissionValue(args[1], args[2], args[3]);
                        this.getLanguage().sendMessageTo(sender, "cmd-perm-set", args[1], args[2], args[3]);
                    }
                    case "group" -> {
                        this.getModule().setPermissionGroup(args[1], args[2]);
                        Player target = PlayerUtil.strictFindPlayer(args[1]);
                        if (target == null) {
                            return;
                        }
                        this.getModule().sync(target);
                        this.getLanguage().sendMessageTo(sender, "cmd-group-set", args[1], args[2]);
                    }
                }
                CommandManager.syncCommands();
            });
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            switch (buffer.length) {
                case 1 -> {
                    tabList.add("set");
                    tabList.add("group");
                }
                case 3 -> {
                    switch (buffer[0]) {
                        case "set" -> tabList.addAll(PermissionEntry.getAllPermissions());
                        case "group" -> {
                            ConfigurationSection groups = this.getConfig().getConfigurationSection("groups");
                            if (groups == null) {
                                return;
                            }
                            tabList.addAll(groups.getKeys(false));
                        }
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
    }
}
