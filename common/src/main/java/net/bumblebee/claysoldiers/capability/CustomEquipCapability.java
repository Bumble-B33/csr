package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierInventoryHandler;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public interface CustomEquipCapability {
    BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability> SHEARS = (stack, effect) -> (soldier) -> {
        if (soldier.setSlotIfEmpty(SoldierEquipmentSlot.OFFHAND, ModItems.SHEAR_BLADE.get().getDefaultInstance())) {
            if (soldier.setSlotIfEmpty(SoldierEquipmentSlot.MAINHAND, ModItems.SHEAR_BLADE.get().getDefaultInstance())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    };
    BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability> GOLD_BLOCK = (stack, effect) -> (soldier) -> royaltyEquip(stack, soldier, Items.GOLD_INGOT);
    BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability> DIAMOND_BLOCK = (stack, effect) -> (soldier) -> royaltyEquip(stack, soldier, Items.DIAMOND);
    BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability> IRON_NUGGET = (stack, effect) -> (soldier) -> {
        if (soldier.getItemBySlot(SoldierEquipmentSlot.OFFHAND).is(Items.BOWL)) {
            soldier.setItemSlot(SoldierEquipmentSlot.OFFHAND, stack);
            return stack;
        }
        if (soldier.getItemBySlot(SoldierEquipmentSlot.MAINHAND).is(Items.BOWL)) {
            soldier.setItemSlot(SoldierEquipmentSlot.MAINHAND, stack);
            return stack;
        }

        return ItemStack.EMPTY;
    };

    Map<ItemLike, BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability>> CUSTOM_EQUIP_MAP = new HashMap<>(Map.of(
            Items.SHEARS, SHEARS,
            Items.GOLD_BLOCK, GOLD_BLOCK,
            Items.DIAMOND_BLOCK, DIAMOND_BLOCK,
            Items.IRON_NUGGET, IRON_NUGGET
    ));

    /**
     * Called when a {@code Clay Soldier} wants to equip the item with this capability.
     * @param claySoldierInventory the {@code Clay Soldier}
     * @return the ItemStack the {@code Clay Soldier} picked up
     */
    ItemStack equip(ClaySoldierInventoryHandler claySoldierInventory);

    static ItemStack royaltyEquip(ItemStack stack, ClaySoldierInventoryHandler inventory, Item toReplace) {
        if (inventory.getItemBySlot(SoldierEquipmentSlot.HEAD).is(toReplace)) {
            inventory.setSlotAndDrop(SoldierEquipmentSlot.CHEST, stack);
            return stack;
        }

        return ItemStack.EMPTY;
    }
}
