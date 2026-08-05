package net.bumblebee.claysoldiers.menu;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class AbstractClayMobMenuSlot extends Slot {
    private final static Container EMPTY_INVENTORY = new SimpleContainer(0);

    public AbstractClayMobMenuSlot(int pSlot, int pX, int pY) {
        super(EMPTY_INVENTORY, pSlot, pX, pY);
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
    public abstract @NonNull Component getDisplayName();
    /**
     * Returns whether the Inventory can be edited.
     */
    protected boolean editMode() {
        return ClaySoldiersCommon.CONFIG.getCommonConfig().canModifyClayMobMenu();
    }
}
