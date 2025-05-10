package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierInventoryHandler;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.menu.AbstractClayMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClaySoldierSlot extends AbstractClayMenuSlot {
    private final ClaySoldierInventoryHandler inventory;
    private final SoldierEquipmentSlot slotType;

    public ClaySoldierSlot(SoldierEquipmentSlot slotType, ClaySoldierInventoryHandler inventoryHandler, int pX, int pY) {
        super(slotType.getIndex(), pX, pY);
        this.slotType = slotType;
        this.inventory = inventoryHandler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!editMode()) {
            return false;
        }
        var properties = ClaySoldiersCommon.DATA_MAP.getEffect(stack);
        if (properties != null) {
            return properties.slots().contains(slotType) && inventory.couldEquipStack(properties);
        }
        return false;
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return !editMode();
    }

    @Override
    public int getMaxStackSize(ItemStack pStack) {
        return Math.min(new ItemStackWithEffect(pStack).maxSoldierStackSize(), getMaxStackSize());
    }

    @Override
    public ItemStack getItem() {
        return inventory.getItemBySlot(slotType).stack();
    }

    @Override
    public void set(ItemStack pStack) {
        inventory.setItemSlot(slotType, pStack);
    }

    @Override
    public ItemStack remove(int pAmount) {
        if (!editMode()) {
            return ItemStack.EMPTY;
        }

        ItemStackWithEffect stackWithEffect = inventory.getItemBySlot(slotType);
        int count = stackWithEffect.getCount();
        var stackToReturn = stackWithEffect.stack().copy();
        stackToReturn.setCount(Math.min(count, pAmount));
        stackWithEffect.setCount(count - pAmount);
        return stackToReturn;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public Component getDisplayName() {
        return slotType.getDisplayName();
    }

    @Override
    protected void onQuickCraft(ItemStack pStack, int pAmount) {}
}
