package net.bumblebee.claysoldiers.menu;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractClayMenuSlot extends Slot {
    private static Container emptyInventory = new SimpleContainer(0);

    public AbstractClayMenuSlot(int pSlot, int pX, int pY) {
        super(emptyInventory, pSlot, pX, pY);
    }

    @Override
    public abstract ItemStack getItem();

    @Override
    public abstract void set(ItemStack pStack);

    @Override
    public abstract boolean mayPlace(ItemStack pStack);

    @Override
    public abstract boolean mayPickup(Player pPlayer);

    @Override
    public abstract ItemStack remove(int pAmount);

    @Override
    public abstract int getMaxStackSize();

    /**
     * Returns the display name of the Slot.
     */
    public abstract Component getDisplayName();
    /**
     * Returns whether the Inventory can be edited.
     */
    protected boolean editMode() {
        return ClaySoldiersCommon.claySolderMenuModify;
    }
}
