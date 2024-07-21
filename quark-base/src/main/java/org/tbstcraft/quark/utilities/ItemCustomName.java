package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.customcontent.CustomMeta;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.List;
import java.util.Locale;

@QuarkModule(version = "0.3")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider({ItemCustomName.ItemCommandCommand.class})
public final class ItemCustomName extends PackageModule {

    @Override
    public void checkCompatibility() throws Throwable {
        Class.forName("org.bukkit.persistence.PersistentDataHolder");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final DelayedPlayerJoinEvent event) {
        refreshInventory(event.getPlayer().getInventory(), Language.locale(event.getPlayer()));
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        for (ItemStack itemStack : event.getItemsHarvested()) {
            refreshItem(itemStack, Language.locale(event.getPlayer()));
        }
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        this.refreshInventory(event.getClickedInventory(), Language.locale(event.getWhoClicked()));
    }

    private void refreshInventory(Inventory inv, Locale locale) {
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                continue;
            }
            refreshItem(stack, locale);
        }
    }

    private void refreshItem(ItemStack stack, Locale locale) {
        ItemMeta meta = stack.getItemMeta();
        if (!CustomMeta.hasItemPDCProperty(stack, "custom_name")) {
            meta.displayName(null);
            stack.setItemMeta(meta);
            return;
        }

        String key = CustomMeta.getItemPDCProperty(stack, "custom_name");

        meta.displayName(this.getLanguage().getMessageComponent(locale, key).asComponent());
        stack.setItemMeta(meta);
    }

    @QuarkCommand(name = "item-custom-name", playerOnly = true)
    public static final class ItemCommandCommand extends ModuleCommand<ItemCustomName> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
            if (stack.getType() == Material.AIR) {
                this.getLanguage().sendMessage(sender, "bind-failed");
                return;
            }
            String id = stack.getType().getKey().getKey();
            if (args[0].equals("none")) {
                CustomMeta.removeItemPDCProperty(stack, "custom_name");
                this.getLanguage().sendMessage(sender, "unbind", id);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : args) {
                    sb.append(s).append(" ");
                }
                String cmdLine = sb.substring(0, sb.length() - 1);

                CustomMeta.setItemPDCProperty(stack, "custom_name", cmdLine);
                this.getLanguage().sendMessage(sender, "bind", id, cmdLine);
            }

            this.getModule().refreshItem(stack, Language.locale(sender));
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
