package org.atcraftmc.quark.lobby;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.data.ModuleDataService;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.List;

@QuarkModule(version = "1.0")
@QuarkCommand(name = "default-inventory", playerOnly = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class DefaultInventory extends CommandModule {
    public static final String TITLE = ChatColor.LIGHT_PURPLE + "Default Inventory Editor";
    Inventory inventory = Bukkit.createInventory(null, InventoryType.PLAYER, TITLE);


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cover(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().isOp()) {
            return;
        }
        cover(event.getWhoClicked());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemHarvest(PlayerAttemptPickupItemEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        event.setCancelled(true);
    }

    private void cover(HumanEntity player) {
        player.getInventory().setContents(this.inventory.getContents());
        player.getInventory().setHeldItemSlot(4);
    }

    @Override
    public void enable() {
        super.enable();

        NBTTagCompound tag = ModuleDataService.getEntry(this.getFullId()).getCompoundTag("inventory");

        if (tag == null) {
            return;
        }

        for (String s : tag.getTagMap().keySet()) {
            inventory.setItem(Integer.parseInt(s), ItemStack.deserializeBytes(tag.getByteArray(s)));
        }
    }

    @Override
    public void disable() {
        super.disable();
        this.save();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != this.inventory) {
            return;
        }

        this.save();

        getLanguage().sendMessage(event.getPlayer(), "edit-complete");
    }

    private void save() {
        int index = 0;
        NBTTagCompound entry = new NBTTagCompound();

        for (ItemStack stack : this.inventory.getContents()) {
            if (stack == null) {
                index++;
                continue;
            }

            entry.setByteArray(String.valueOf(index), stack.serializeAsBytes());
            index++;
        }

        NBTTagCompound tag = ModuleDataService.getEntry(this.getFullId());
        tag.setCompoundTag("inventory", entry);
        ModuleDataService.save(this.getFullId());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "edit" -> {
                ((Player) sender).openInventory(this.inventory);
                getLanguage().sendMessage(sender, "edit-start");
            }
            case "cover" -> {
                cover((Player) sender);
                getLanguage().sendMessage(sender, "cover");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("cover");
            tabList.add("edit");
        }
    }
}
