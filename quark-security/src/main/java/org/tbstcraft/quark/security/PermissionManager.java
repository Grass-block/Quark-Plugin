package org.tbstcraft.quark.security;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.security.command.PermissionCommand;
import org.tbstcraft.quark.service.PlayerDataService;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Objects;

public class PermissionManager extends PluginModule {
    public static final String DATA_ID = "permission_manager:/group";
    public static final String OVERRIDE_ID = "permission_manager:/override";
    public static final HashMap<String, PermissionAttachment> ATTACHMENTS = new HashMap<>();

    private final AbstractCommand command = new PermissionCommand(this);

    @Override
    public void onEnable() {
        this.registerListener();
        CommandManager.registerCommand(this.command);
    }

    @Override
    public void onDisable() {
        CommandManager.unregisterCommand(this.command);
        this.unregisterListener();
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

    public PermissionAttachment getAttachment(Player player) {
        return ATTACHMENTS.get(player.getName());
    }

    public void sync(Player p) {
        NBTTagCompound tag = PlayerDataService.getEntry(p.getName());
        PermissionAttachment attachment = getAttachment(p);
        if (attachment == null) {
            return;
        }

        ConfigurationSection groups = this.getConfig().getConfigurationSection("groups");
        if (groups == null) {
            return;
        }

        String group = tag.getString(DATA_ID);
        if (!groups.contains(group)) {
            group = this.getConfig().getString("groups");
            this.setPermissionGroup(p, group);
        }

        if (group == null) {
            return;
        }
        ConfigurationSection groupSection = groups.getConfigurationSection(group);
        if (groupSection == null) {
            return;
        }

        for (String s : Objects.requireNonNull(groupSection.getStringList("allow"))) {
            attachment.setPermission(s, true);
        }

        for (String s : Objects.requireNonNull(groupSection.getStringList("disallow"))) {
            attachment.setPermission(s, false);
        }

        if(!tag.hasKey(OVERRIDE_ID)){
            tag.setCompoundTag(OVERRIDE_ID,new NBTTagCompound());
        }
        for (String s : tag.getCompoundTag(OVERRIDE_ID).getTagMap().keySet()) {
            attachment.setPermission(s, tag.getBoolean(s));
        }

        p.recalculatePermissions();
    }

    public void setPermissionGroup(Player p, String group) {
        NBTTagCompound tag = PlayerDataService.getEntry(getId());
        tag.setString(DATA_ID, group);
        PlayerDataService.asyncSavePlayerData(p.getName());
    }

    public void setPermission(Player p, String permission, boolean value) {
        if (p == null) {
            return;
        }
        NBTTagCompound tag = PlayerDataService.getEntry(p.getName());
        if(!tag.hasKey(OVERRIDE_ID)){
            tag.setCompoundTag(OVERRIDE_ID,new NBTTagCompound());
        }
        tag.getCompoundTag(OVERRIDE_ID).setBoolean(permission, value);
        if (p.isOnline()) {
            this.getAttachment(p).setPermission(permission, value);
        }
        PlayerDataService.asyncSavePlayerData(p.getName());
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

}
