package org.tbstcraft.quark.framework.customcontent;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.tbstcraft.quark.Quark;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public interface CustomMeta {
    Pattern CUSTOM_ITEM_META_LORE = Pattern.compile(ChatColor.DARK_GRAY + "[a-z]+::[a-z]+");

    String ID_PROPERTY_NAME = "block_usage";

    //PDC
    static void setPDCIdentifier(PersistentDataHolder holder, String data) {
        setPDCProperty(holder, ID_PROPERTY_NAME, data);
    }

    static String getPDCIdentifier(PersistentDataHolder holder) {
        return getPDCProperty(holder, ID_PROPERTY_NAME);
    }

    static boolean matchPDCIdentifier(PersistentDataHolder holder, String data) {
        return Objects.equals(getPDCIdentifier(holder), data);
    }

    static boolean hasPDCIdentifier(PersistentDataHolder holder) {
        return getPDCIdentifier(holder) != null;
    }

    static void removePDCIdentifier(PersistentDataHolder holder) {
        removePDCProperty(holder, ID_PROPERTY_NAME);
    }

    static void setPDCProperty(PersistentDataHolder holder, String id, String data) {
        NamespacedKey key = new NamespacedKey(Quark.PLUGIN, id);
        holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
    }

    static String getPDCProperty(PersistentDataHolder holder, String id) {
        NamespacedKey key = new NamespacedKey(Quark.PLUGIN, id);
        try {
            return holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        } catch (Exception ignored) {
            return null;
        }
    }

    static void removePDCProperty(PersistentDataHolder holder, String id) {
        NamespacedKey key = new NamespacedKey(Quark.PLUGIN, id);
        holder.getPersistentDataContainer().remove(key);
    }

    static boolean hasPDCProperty(PersistentDataHolder holder, String id) {
        return getPDCProperty(holder, id) != null;
    }


    //item
    static boolean hasItemIdentifier(ItemStack stack) {
        if (stack.getItemMeta().getLore() == null) {
            return false;
        }

        return getItemIdentifier(stack) != null;
    }

    static String getItemIdentifier(ItemStack stack) {
        if (stack.getItemMeta().getLore() == null) {
            return null;
        }

        for (String s : Objects.requireNonNull(stack.getItemMeta().getLore())) {
            if (!CUSTOM_ITEM_META_LORE.matcher(s).matches()) {
                continue;
            }
            return s.replaceFirst(String.valueOf(ChatColor.DARK_GRAY), "");
        }

        return null;
    }

    static void setItemIdentifier(ItemStack stack, String namespace, String id) {
        if (hasItemIdentifier(stack)) {
            removeItemIdentifier(stack);
        }

        String tag = ChatColor.DARK_GRAY + "%s::%s".formatted(namespace, id);

        ItemMeta meta = stack.getItemMeta();
        setLoreTag(stack, meta, tag);
    }

    static void setItemIdentifier(ItemStack stack, String value) {
        setItemIdentifier(stack, "quark", value);
    }

    static void removeItemIdentifier(ItemStack stack) {
        Pattern p = Pattern.compile(ChatColor.DARK_GRAY + "[a-z]+::[a-z]+");

        ItemMeta meta = stack.getItemMeta();
        if (meta.getLore() == null) {
            return;
        }

        List<String> lore = meta.getLore();
        Objects.requireNonNull(lore).removeIf(s -> p.matcher(s).matches());
        meta.setLore(lore);

        stack.setItemMeta(meta);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean matchItemIdentifier(ItemStack stack, String value) {
        if (!value.contains("::")) {
            value = "quark::" + value;
        }
        return Objects.equals(getItemIdentifier(stack), value);
    }

    static void setItemProperty(ItemStack stack, String key, String value) {
        String tag = ChatColor.DARK_GRAY + "cim::%s=%s".formatted(key, value);

        if (hasItemProperty(stack, key)) {
            removeItemProperty(stack, key);
        }

        ItemMeta meta = stack.getItemMeta();
        setLoreTag(stack, meta, tag);
    }

    static void setLoreTag(ItemStack stack, ItemMeta meta, String tag) {
        if (meta.getLore() == null) {
            meta.setLore(List.of(tag));
            stack.setItemMeta(meta);
            return;
        }

        List<String> lore = meta.getLore();
        Objects.requireNonNull(lore).add(tag);
        meta.setLore(lore);

        stack.setItemMeta(meta);
    }

    static void removeItemProperty(ItemStack stack, String key) {
        ItemMeta meta = stack.getItemMeta();
        if (stack.getItemMeta().getLore() == null) {
            return;
        }

        List<String> lore = meta.getLore();
        Objects.requireNonNull(lore).removeIf(s -> s.startsWith(ChatColor.DARK_GRAY + "cim::" + key + "="));
        meta.setLore(lore);

        stack.setItemMeta(meta);
    }

    static boolean hasItemProperty(ItemStack stack, String key) {
        return getItemProperty(stack, key) != null;
    }

    static String getItemProperty(ItemStack stack, String key) {
        String match = ChatColor.DARK_GRAY + "cim::" + key + "=";

        if (stack.getItemMeta().getLore() == null) {
            return null;
        }

        for (String s : Objects.requireNonNull(stack.getItemMeta().getLore())) {
            if (!s.startsWith(match)) {
                continue;
            }
            return s.replaceFirst(match, "");
        }

        return null;
    }

    //PDC item
    static void setItemPDCProperty(ItemStack stack, String id, String data) {
        ItemMeta meta = stack.getItemMeta();
        setPDCProperty(meta, id, data);
        stack.setItemMeta(meta);
    }

    static String getItemPDCProperty(ItemStack stack, String prop) {
        return getPDCProperty(stack.getItemMeta(), prop);
    }

    static boolean hasItemPDCProperty(ItemStack stack, String prop) {
        return hasPDCProperty(stack.getItemMeta(), prop);
    }

    static void removeItemPDCProperty(ItemStack stack, String prop) {
        ItemMeta meta = stack.getItemMeta();
        removePDCProperty(meta, prop);
        stack.setItemMeta(meta);
    }


    static void setItemPDCIdentifier(ItemStack stack, String data) {
        setItemPDCProperty(stack, ID_PROPERTY_NAME, data);
    }

    static String getItemPDCIdentifier(ItemStack stack) {
        return getPDCProperty(stack.getItemMeta(), ID_PROPERTY_NAME);
    }

    static boolean hasItemPDCIdentifier(ItemStack stack) {
        return hasPDCProperty(stack.getItemMeta(), ID_PROPERTY_NAME);
    }

    static void removeItemPDCIdentifier(ItemStack stack) {
        removeItemPDCProperty(stack, ID_PROPERTY_NAME);
    }
}
