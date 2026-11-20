package net.bumblebee.claysoldiers.entity.goal.target;

import net.bumblebee.claysoldiers.entity.goal.AlertOthersGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

import java.util.function.Predicate;

public class ClaySoldierHurtByTargetGoal extends HurtByTargetGoal implements AlertOthersGoal {
    private final AbstractClaySoldierEntity claySoldier;
    private final Predicate<LivingEntity> targetPredicate;

    public ClaySoldierHurtByTargetGoal(AbstractClaySoldierEntity claySoldier, Predicate<LivingEntity> targetPredicate, Class<?>... pToIgnoreDamage) {
        super(claySoldier, pToIgnoreDamage);
        this.claySoldier = claySoldier;
        this.setAlertOthers();
        this.targetPredicate = targetPredicate;

    }

    @Override
    public boolean canUse() {
        if (!claySoldier.fightsBack()) {
            return false;
        }
        LivingEntity target = this.mob.getLastHurtByMob();
        if (target != null && !targetPredicate.test(target)) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public void start() {
        super.start();
        if (this.claySoldier.getAttackType() == AttackTypeProperty.QUEEN) {
            alertOthersSoldiers(this.claySoldier);
        }
    }
}
