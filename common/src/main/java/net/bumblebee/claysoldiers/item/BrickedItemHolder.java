package net.bumblebee.claysoldiers.item;

import net.minecraft.world.item.ItemStack;


public interface BrickedItemHolder {
    /**
     * Returns the {@code BrickedItem} version of the original {@code Item}.
     */
    ItemStack getBrickedItem(ItemStack original);
}
