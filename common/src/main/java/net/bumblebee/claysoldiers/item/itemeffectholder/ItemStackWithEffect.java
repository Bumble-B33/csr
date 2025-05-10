package net.bumblebee.claysoldiers.item.itemeffectholder;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.ThrowableItemCapability;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessorySlot;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.RangedAttackType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ItemStackWithEffect extends ItemStackEffectHolder<SoldierHoldableEffect> {
    public static final ItemStackWithEffect EMPTY = new ItemStackWithEffect(ItemStack.EMPTY, null, null);

    @NotNull
    private final SoldierMultiWearable wearableEffect;

    public ItemStackWithEffect(ItemStack stack, @Nullable SoldierHoldableEffect effect, @Nullable SoldierMultiWearable wearableEffect) {
        super(stack, effect);
        this.wearableEffect = wearableEffect == null ? SoldierMultiWearable.empty() : wearableEffect;
    }

    public ItemStackWithEffect(ItemStack stack) {
        this(stack, ClaySoldiersCommon.DATA_MAP.getEffect(stack), ClaySoldiersCommon.DATA_MAP.getArmor(stack));
    }

    @Override
    protected SoldierHoldableEffect createEffectOnInitialisation(ItemStack stack) {
        return ClaySoldiersCommon.DATA_MAP.getEffect(stack);
    }

    public static ItemStackWithEffect parseOptional(HolderLookup.Provider provider, CompoundTag pCompoundTag) {
        return new ItemStackWithEffect(ItemStack.parseOptional(provider, pCompoundTag));
    }

    public boolean isThrowable() {
        return effect != null && effect.throwable();
    }
    public boolean throwableTypeMatchAttackType(AttackTypeProperty attackType) {
        if (effect == null || !effect.throwable()) {
            return true;
        }
        if (attackType.isSupportive()) {
            return effect.throwableType(RangedAttackType.HELPING);
        }
        if (attackType.fightsBack()) {
            return effect.throwableType(RangedAttackType.HARM);
        }
        return true;
    }
    public boolean isShield() {
        return wearableEffect.getAccessories().get(SoldierAccessorySlot.SHIELD) != null;
    }
    public boolean isGlider() {
        return effect != null && effect.properties().canGlide() && wearableEffect.getAccessories().get(SoldierAccessorySlot.GLIDER) != null;
    }
    public float dropRate() {
        return effect != null ? effect.dropRate() : 1f;
    }


    @Nullable
    public ThrowableItemCapability getThrowableCap() {
        var factory = ClaySoldiersCommon.CAPABILITY_MANGER.getThrowableItem(stack);
        if (factory != null) {
            return factory.apply(stack, effect);
        }
        return null;
    }
    @Override
    @Nullable
    public SoldierHoldableEffect effect() {
        if (effect == null) {
            return ClaySoldiersCommon.DATA_MAP.getEffect(stack);
        }
        return effect;
    }


    @NotNull
    public SoldierMultiWearable wearableEffectMap() {
        return wearableEffect;
    }

    /**
     * Returns whether this {@code ItemStackWithEffect} can be replaced by the other given one.
     */
    public boolean canBeReplaced(ItemStackWithEffect other) {
        if (stack.isEmpty() || effect == null) {
            return true;
        } else if (other.stack.isEmpty() || other.effect == null) {
            return false;
        }
        return effect.pickUpPriority() < other.effect.pickUpPriority();
    }
    public ItemStackWithEffect copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            return new ItemStackWithEffect(stack.copy());
        }
    }

    public int maxSoldierStackSize() {
        if (effect != null) {
            return Math.min(effect.getMaxStackSize(), stack.getMaxStackSize());
        }
        return 1;
    }

    @Override
    public String toString() {
        return "ItemStackWithEffect{" + stack +
                ", effect=" + effect +
                ", wearableEffect=" + wearableEffect +
                '}';
    }
}