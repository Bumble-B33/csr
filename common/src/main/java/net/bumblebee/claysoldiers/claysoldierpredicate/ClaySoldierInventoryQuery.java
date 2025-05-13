package net.bumblebee.claysoldiers.claysoldierpredicate;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ClaySoldierInventoryQuery {
    ItemStackWithEffect getItemBySlot(SoldierEquipmentSlot type);
    Iterable<ItemStack> getAllSlots();
    SoldierPropertyMapReader allProperties();
    @Nullable
    MobEffectInstance getMobEffect(Holder<MobEffect> pEffect);
    /**
     * Returns the offset color of this {@code Clay Soldier}.
     * @return the offset color of this {@code Clay Soldier}
     */
    ColorHelper getOffsetColor();
    /**
     * Returns whether this {@code Clay Soldier} has an offset color.
     * @return whether this {@code Clay Soldier} has an offset color
     */
    default boolean hasOffsetColor() {
        return !getOffsetColor().isEmpty();
    }

    /**
     * Returns the {@code AttackType} of the {@code Clay Soldier}.
     */
    AttackTypeProperty getAttackType();

    /**
     * Calls {@link ColorHelper#getColor(LivingEntity, float)}, with this soldier and the give partialTicks.
     * Returns the resulting int.
     */
    int unpackDynamicColor(ColorHelper color, float partialTicks);

    @Nullable Player getClayTeamOwner();
}
