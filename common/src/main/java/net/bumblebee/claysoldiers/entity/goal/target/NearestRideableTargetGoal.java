package net.bumblebee.claysoldiers.entity.goal.target;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class NearestRideableTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

    public NearestRideableTargetGoal(ClayMobEntity pMob) {
        super(pMob, LivingEntity.class, 50, true, true, pMob::canMountEntity);
    }

    @Override
    public boolean canUse() {
        return ((ClayMobEntity) mob).isAbleToRide() && super.canUse();
    }
}
