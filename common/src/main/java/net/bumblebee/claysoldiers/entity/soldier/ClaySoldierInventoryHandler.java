package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.claypoifunction.ClaySoldierInventorySetter;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClaySoldierInventoryHandler extends ClaySoldierInventoryQuery, ClaySoldierInventorySetter {
    /**
     * Sets the given {@code ItemStack} in the given {@code Slot},
     * replacing the current one.
     */
    @Override
    void setItemSlot(SoldierEquipmentSlot slot, ItemStack stack);

    /**
     * Sets the given {@code ItemStack} in the given {@code Slot},
     * replacing the current one.
     */
    @Override
    void setItemSlot(SoldierEquipmentSlot pSlot, ItemStackWithEffect pStack);

    /**
     * Returns the item in the given slot.
     */
    @Override
    ItemStackWithEffect getItemBySlot(SoldierEquipmentSlot slot);

    /**
     * Set the given ItemStack in the given slot if it is isEmpty
     *
     * @return whether the stack got replaced successfully
     */
    default boolean setSlotIfEmpty(SoldierEquipmentSlot slot, ItemStack stack) {
        if (getItemBySlot(slot).isEmpty()) {
            setItemSlot(slot, stack);
            return true;
        }
        return false;
    }

    /**
     * Drops the ItemStack in the given slot and replace it with the given one.
     *
     * @param slot  the slot to drop and replace
     * @param stack the stack to replace with
     */
    default void setSlotAndDrop(SoldierEquipmentSlot slot, ItemStack stack) {
        dropItemStack(getItemBySlot(slot));
        setItemSlot(slot, stack);
    }

    /**
     * Drops the given ItemStack on the ground.
     * Does not affect the stack in the inventory
     *
     * @param stack the stack to drop
     * @return the newly spawned ItemEntity
     */
    @Nullable
    @Override
    ItemEntity dropItemStack(ItemStack stack);

    /**
     * Drops the ItemStack in the given on the ground.
     * Does not affect the stack in the inventory
     *
     * @param slot to drop from
     * @return the newly spawned ItemEntity
     */
    @Override
    default @Nullable ItemEntity dropItemSlotWithChance(SoldierEquipmentSlot slot) {
        return dropItemStackWithChance(getItemBySlot(slot));
    }

    /**
     * Drops the given ItemStack on the ground.
     * Does not affect the stack in the inventory
     *
     * @param stackWithEffect the stack to drop
     * @return the newly spawned ItemEntity
     */
    @Nullable
    default ItemEntity dropItemStack(ItemStackWithEffect stackWithEffect) {
        return dropItemStack(stackWithEffect.stack());
    }

    /**
     * Drops the given ItemStack on the ground with the chance of the drop rate.
     * Does not affect the stack in the inventory
     *
     * @param stackWithEffect the stack to drop
     * @return the newly spawned ItemEntity
     */
    @Nullable
    @Override
    default ItemEntity dropItemStackWithChance(ItemStackWithEffect stackWithEffect) {
        if (getClaySoldierRandom().nextFloat() < stackWithEffect.dropRate()) {
            return dropItemStack(stackWithEffect);
        }
        return null;
    }

    @Override
    default boolean increaseEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        var current = getMobEffect(effectInstance.getEffect());
        if (current == null) {
            return addMobEffect(effectInstance, entity);
        }
        return addMobEffect(new MobEffectInstance(effectInstance.getEffect(), effectInstance.getDuration() + current.getDuration(), effectInstance.getAmplifier() + effectInstance.getAmplifier()), entity);
    }

    /**
     * Reruns whether the Clay Soldier could equip this ItemStackWith with the given effect,
     * does not take into consideration if the Soldier has any inventory space for it.
     */
    boolean couldEquipStack(@NotNull SoldierHoldableEffect effect);
}
