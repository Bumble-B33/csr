package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierInventoryHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class ClaySoldierItemHandler implements IItemHandler {
    private final ClaySoldierInventoryHandler inventory;

    public ClaySoldierItemHandler(ClaySoldierInventoryHandler inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return SoldierEquipmentSlot.values().length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getItemBySlot(validateSlotIndex(slot)).stack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final SoldierEquipmentSlot equipmentSlot = validateSlotIndex(slot);

        final ItemStack existing = inventory.getItemBySlot(equipmentSlot).stack();

        int limit = getSlotLimit(slot);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                inventory.setItemSlot(equipmentSlot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        final SoldierEquipmentSlot equipmentSlot = validateSlotIndex(slot);

        final ItemStack existing = inventory.getItemBySlot(equipmentSlot).stack();

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        final int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                inventory.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }

            return existing;
        } else {
            if (!simulate) {
                inventory.setItemSlot(equipmentSlot, existing.copyWithCount(existing.getCount() - toExtract));
            }

            return existing.copyWithCount(toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getItemBySlot(validateSlotIndex(slot)).maxSoldierStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        var effect = ClaySoldiersCommon.DATA_MAP.getEffect(stack);
        if (effect == null) {
            return false;
        }
        return inventory.couldEquipStack(effect);
    }

    protected SoldierEquipmentSlot validateSlotIndex(final int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
        }

        return SoldierEquipmentSlot.values()[slot];
    }
}
