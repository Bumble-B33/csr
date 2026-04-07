package net.bumblebee.claysoldiers.entity.goal.target;

import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class ClaySoldierNearestTargetGoal extends NearestAttackableTargetGoal<AbstractClaySoldierEntity> {
    protected TargetingConditions specificTargetCondition;


    public ClaySoldierNearestTargetGoal(AbstractClaySoldierEntity pMob, boolean pMustSee, TargetingConditions.Selector pTargetPredicate, TargetingConditions.Selector special) {
        super(pMob, AbstractClaySoldierEntity.class, pMustSee, pTargetPredicate);
        this.specificTargetCondition = TargetingConditions.forCombat().range(this.getFollowDistance()).selector((e, l) -> pTargetPredicate.test(e, l) && special.test(e, l));
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            findTargetSpecial();
            if(target == null) {
                this.findTarget();
            }
            return this.target != null;
        }
    }

    protected void findTargetSpecial() {
        this.target = getServerLevel(mob)
                .getNearestEntity(
                        this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), soldier -> true),
                        this.specificTargetCondition,
                        this.mob,
                        this.mob.getX(),
                        this.mob.getEyeY(),
                        this.mob.getZ()
                );
    }

}
