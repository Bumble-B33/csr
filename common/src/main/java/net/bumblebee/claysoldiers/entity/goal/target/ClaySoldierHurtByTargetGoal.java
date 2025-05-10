package net.bumblebee.claysoldiers.entity.goal.target;

import net.bumblebee.claysoldiers.entity.goal.AlertOthersGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class ClaySoldierHurtByTargetGoal extends HurtByTargetGoal implements AlertOthersGoal {
    private final AbstractClaySoldierEntity claySoldier;

    public ClaySoldierHurtByTargetGoal(AbstractClaySoldierEntity claySoldier, Class<?>... pToIgnoreDamage) {
        super(claySoldier, pToIgnoreDamage);
        this.claySoldier = claySoldier;
        this.setAlertOthers();

    }

    @Override
    public boolean canUse() {
        if (!claySoldier.fightsBack()) {
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
