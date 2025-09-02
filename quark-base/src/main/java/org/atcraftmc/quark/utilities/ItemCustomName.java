package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.core.custom.CustomMeta;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;

import java.util.List;

@SLModule(version = "0.3")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({ItemCustomName.ItemCommandCommand.class})
public final class ItemCustomName extends PackageModule {

    @Override
    public void checkCompatibility() {
        Compatibility.requirePDC();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        refreshInventory(event.getPlayer().getInventory(), LocaleService.locale(event.getPlayer()));
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        for (ItemStack itemStack : event.getItemsHarvested()) {
            refreshItem(itemStack, LocaleService.locale(event.getPlayer()));
        }
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        this.refreshInventory(event.getClickedInventory(), LocaleService.locale(event.getWhoClicked()));
    }

    private void refreshInventory(Inventory inv, MinecraftLocale locale) {
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                continue;
            }
            if (stack.getItemMeta() == null) {
                continue;
            }
            refreshItem(stack, locale);
        }
    }

    private void refreshItem(ItemStack stack, MinecraftLocale locale) {
        ItemMeta meta = stack.getItemMeta();
        if (!CustomMeta.hasItemPDCProperty(stack, "custom_name")) {
            //meta.displayName(null);
            //stack.setItemMeta(meta);
            return;
        }

        String key = CustomMeta.getItemPDCProperty(stack, "custom_name");

        meta.displayName(this.getLanguage().item(key).component(locale).asComponent());
        stack.setItemMeta(meta);
    }

    @QuarkCommand(name = "item-custom-name", playerOnly = true)
    public static final class ItemCommandCommand extends ModuleCommand<ItemCustomName> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
            if (stack.getType() == Material.AIR) {
                MessageAccessor.send(this.getLanguage(), sender, "bind-failed");
                return;
            }
            String id = stack.getType().getKey().getKey();
            if (args[0].equals("none")) {
                CustomMeta.removeItemPDCProperty(stack, "custom_name");
                MessageAccessor.send(this.getLanguage(), sender, "unbind", id);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : args) {
                    sb.append(s).append(" ");
                }
                String cmdLine = sb.substring(0, sb.length() - 1);

                CustomMeta.setItemPDCProperty(stack, "custom_name", cmdLine);
                MessageAccessor.send(this.getLanguage(), sender, "bind", id, cmdLine);
            }

            this.getModule().refreshItem(stack, LocaleService.locale(sender));
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("none");
                tabList.add("<name>");
            }
        }
    }
}
