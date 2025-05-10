package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ClaySoldierMeleeSupportGoal extends ClaySoldierMeleeGoal {
    public ClaySoldierMeleeSupportGoal(AbstractClaySoldierEntity claySoldier, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(claySoldier, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected boolean canUsePredicate() {
        return claySoldier.getAttackType().isSupportive();
    }

    @Override
    protected void performMeleeAction(LivingEntity target) {
        claySoldier.allProperties().specialAttacks(SpecialAttackType.MELEE, SpecialEffectCategory.BENEFICIAL).forEach(attack -> attack.attackEffect(claySoldier, target));
        claySoldier.setTarget(null);
    }
}
