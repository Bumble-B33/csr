package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class UndeadClaySoldier extends AbstractClaySoldierEntity {
    protected UndeadClaySoldier(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel, AttackTypeProperty defaultAttackType) {
        super(pEntityType, pLevel, defaultAttackType);
        if (defaultAttackType.getStyle() == null) {
            throw new IllegalArgumentException("the default AttackType of Undead ClaySoldier cannot be %s because style is null".formatted(defaultAttackType));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RestrictSunGoal(this));
        this.goalSelector.addGoal(1, new FleeSunGoal(this, getSunFleeSpeed()));
        super.registerGoals();
    }

    /**
     * @return the speed multiplier at which this {@code UndeadClaySoldier} runs away from the sun.
     */
    protected double getSunFleeSpeed() {
        return 1;
    }

    protected boolean isItemStackHelm(ItemStack stack) {
        var effect = new ItemStackWithEffect(stack).effect();
        return effect != null && effect.slots().contains(SoldierEquipmentSlot.HEAD);
    }

    @Override
    public void aiStep() {
        boolean sunBurnTick = this.isSunBurnTick() && this.getItemBySlot(SoldierEquipmentSlot.HEAD).isEmpty();
        if (sunBurnTick) {
            this.igniteForSeconds(6.0F);
        }

        super.aiStep();
    }

    @Override
    protected int decreaseAirSupply(int pCurrentAir) {
        return pCurrentAir;
    }

    @Override
    public boolean canSwim() {
        return false;
    }

    @Override
    public boolean canBeRevived() {
        return false;
    }

    @Override
    protected Component getInventoryName() {
        return Component.empty().append(getDisplayName()).withStyle(defaultAttackType.getAnimatedStyle(this));
    }
}
