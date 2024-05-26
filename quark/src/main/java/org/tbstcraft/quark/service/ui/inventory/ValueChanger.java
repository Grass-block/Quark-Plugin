package org.tbstcraft.quark.service.ui.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tbstcraft.quark.service.ui.callback.InventoryActionListener;
import org.tbstcraft.quark.service.ui.callback.ValueEventHandler;
import org.tbstcraft.quark.util.platform.BukkitUtil;

public final class ValueChanger implements InventoryComponent, InventoryActionListener {
    private static final int[] SCALES = new int[]{1, 5, 10, 50};
    private final int baseX;
    private final int baseY;
    private final ValueEventHandler handler;
    private final int min;
    private final int max;
    private final int halfWidth;
    private final boolean vertical;
    private final InventoryIcon icon = null;
    private int value;

    public ValueChanger(boolean vertical, int baseX, int baseY, int min, int max, int initial, int halfWidth, ValueEventHandler handler) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.min = min;
        this.max = max;
        this.value = initial;
        this.handler = handler;
        this.halfWidth = halfWidth;
        this.vertical = vertical;
    }

    @Override
    public void invoke(Player p, InventoryUI.InventoryUIInstance ui, int x, int y) {
        int delta = this.vertical ? y - this.baseY : x - this.baseX;

        if (!this.test(delta)) {
            return;
        }
        this.handler.invoke(p, ui, this.value);
        //ui.getInventory().setItem(this.baseY * 9 + this.baseX );
       // ui.setIcon(this.baseX, this.baseY, Material.GRAY_STAINED_GLASS_PANE, this.value);
    }

    private boolean test(int delta) {
        if (this.halfWidth > 3) {
            if (this.toValue(4, delta, SCALES[3])) {
                return true;
            }
        }
        if (this.halfWidth > 2) {
            if (this.toValue(3, delta, SCALES[2])) {
                return true;
            }
        }
        if (this.halfWidth > 1) {
            if (this.toValue(2, delta, SCALES[1])) {
                return true;
            }
        }
        return this.toValue(1, delta, SCALES[0]);
    }

    private boolean toValue(int scale, int position, int value) {
        int delta = 0;
        if (position == scale) {
            delta = value;
        }
        if (position == -scale) {
            delta = -value;
        }
        if (this.value + delta < this.min) {
            this.value = this.min;
        } else this.value = Math.min(this.value + delta, this.max);
        return delta != 0;
    }


    @Override
    public void onAttached(InventoryUI ui, int x, int y) {
        for (int i = 1; i < 5; i++) {
            if (this.halfWidth < i) {
                continue;
            }

            int v = SCALES[i - 1];
            ItemStack s1 = BukkitUtil.createStack(Material.RED_STAINED_GLASS_PANE, v, "-" + v);
            ItemStack s2 = BukkitUtil.createStack(Material.GREEN_STAINED_GLASS_PANE, v, "-" + v);

            if (!this.vertical) {
                ui.setComponent(this.baseX - i, this.baseY, new Button(new InventoryIcon(s1), this));
                ui.setComponent(this.baseX + i, this.baseY, new Button(new InventoryIcon(s2), this));
            } else {
                ui.setComponent(this.baseX, this.baseY - i, new Button(new InventoryIcon(s1), this));
                ui.setComponent(this.baseX, this.baseY + i, new Button(new InventoryIcon(s2), this));
            }
        }
    }
}
