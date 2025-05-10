package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class ClaySoldierMeleeAttackGoal extends ClaySoldierMeleeGoal implements AlertOthersGoal {
    public ClaySoldierMeleeAttackGoal(AbstractClaySoldierEntity claySoldier, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(claySoldier, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected boolean canUsePredicate() {
        return this.claySoldier.fightsBack();
    }


    @Override
    public void start() {
        super.start();
        this.claySoldier.setAggressive(true);
    }


    @Override
    public void stop() {
        super.stop();
        this.claySoldier.setAggressive(false);
    }

    @Override
    protected void performMeleeAction(LivingEntity pTarget) {
        boolean main = claySoldier.getItemInHand(InteractionHand.MAIN_HAND).is(ModTags.Items.SOLDIER_WEAPON);
        boolean off = claySoldier.getItemInHand(InteractionHand.OFF_HAND).is(ModTags.Items.SOLDIER_WEAPON);

        if (main && off) {
            if (claySoldier.getRandom().nextBoolean()) {
                this.claySoldier.swing(InteractionHand.MAIN_HAND);
            } else {
                this.claySoldier.swing(InteractionHand.OFF_HAND);
            }
        } else if (off) {
            this.claySoldier.swing(InteractionHand.OFF_HAND);
        } else {
            this.claySoldier.swing(InteractionHand.MAIN_HAND);
        }
        this.claySoldier.doHurtTarget(pTarget);
        if (this.claySoldier.getAttackType() == AttackTypeProperty.KING) {
            alertOthersSoldiers(this.claySoldier);
        }

    }
}
