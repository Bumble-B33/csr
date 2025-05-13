package net.bumblebee.claysoldiers.claypoifunction;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClaySoldierInventorySetter {
    void setItemSlot(SoldierEquipmentSlot slot, ItemStack stack);
    void setItemSlot(SoldierEquipmentSlot pSlot, ItemStackWithEffect pStack);
    boolean setSlotIfEmpty(SoldierEquipmentSlot slot, ItemStack stack);
    void setSlotAndDrop(SoldierEquipmentSlot slot, ItemStack stack);
    @Nullable
    ItemEntity dropItemSlotWithChance(SoldierEquipmentSlot slot);
    @Nullable
    ItemEntity dropItemStack(ItemStack stack);
    @Nullable
    ItemEntity dropItemStack(ItemStackWithEffect stackWithEffect);
    @Nullable
    ItemEntity dropItemStackWithChance(ItemStackWithEffect stackWithEffect);

    boolean addMobEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity);
    boolean removeMobEffect(Holder<MobEffect> pEffect);

    /**
     * Increase the given effect if it is already present otherwise adds it
     * @param effectInstance the effect to increase
     * @param entity source
     * @return whether the effect could be applied
     */
    boolean increaseEffect(MobEffectInstance effectInstance, @Nullable Entity entity);

    void setOffsetColor(ColorHelper color);
    void addOffsetColor(ColorHelper color);

    @NotNull
    RandomSource getClaySoldierRandom();
}