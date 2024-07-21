package org.tbstcraft.quark.framework.customcontent.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.framework.customcontent.CustomMeta;

public abstract class CustomBlock {
    private final String id;
    private final Material icon;
    private final boolean glow;
    private final DropChance chance;

    private final LanguageItem displayName;

    public CustomBlock(LanguageItem displayName) {
        this.displayName = displayName;
        this.id = this.getIdentifier().id();
        this.icon = this.getIdentifier().icon();
        this.chance = this.getIdentifier().chance();
        this.glow = this.getIdentifier().enchantGlow();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!CustomMeta.matchItemIdentifier(event.getItemInHand(), this.id)) {
            return;
        }
        setBlockId(((TileState) event.getBlockPlaced().getState()), this.id);
    }

    @EventHandler

    public abstract String getDisplayName(CommandSender target);

    public QuarkBlock getIdentifier() {
        return this.getClass().getAnnotation(QuarkBlock.class);
    }


    @EventHandler
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent event){
        for (ItemStack stack:event.getItemsHarvested()){
            if (!CustomMeta.matchItemIdentifier(stack, this.id)) {
                continue;
            }
        }

    }


    public ItemStack makeItemStack(int amount) {
        Material icon = this.getIdentifier().icon();
        ItemStack stack = new ItemStack(icon, amount);
        return stack;
    }


    public static void setBlockId(PersistentDataHolder holder, String id) {
        NamespacedKey key = new NamespacedKey(Quark.PLUGIN, "block_usage");
        holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
    }


    public void setData(ItemStack stack){

    }
}
