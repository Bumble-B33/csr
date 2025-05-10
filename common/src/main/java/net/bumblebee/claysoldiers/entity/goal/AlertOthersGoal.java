package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.List;

public interface AlertOthersGoal {
    AlertOthersGoal INSTANCE = new AlertOthersGoal() {};

    default void alertOthersSoldiers(AbstractClaySoldierEntity self) {
        double d0 = getFollowDistance(self) / 2;
        AABB aabb = AABB.unitCubeFromLowerCorner(self.position()).inflate(d0, 6.0, d0);
        final List<? extends AbstractClaySoldierEntity> list = self.level().getEntitiesOfClass(AbstractClaySoldierEntity.class, aabb, EntitySelector.NO_SPECTATORS);
        final Iterator<? extends AbstractClaySoldierEntity> iterator = list.iterator();

        while(true) {
            AbstractClaySoldierEntity otherSoldiers;
            while(true) {
                if (!iterator.hasNext()) {
                    return;
                }

                otherSoldiers = iterator.next();
                if (self != otherSoldiers
                        && (otherSoldiers.getTarget() == null || self.getAttackType() == AttackTypeProperty.KING)
                        && (self.getLastHurtByMob() != null && !otherSoldiers.isAlliedTo(self.getLastHurtByMob()))
                ) {
                    if (otherSoldiers.fightsBack() && !otherSoldiers.getAttackType().isRoyalty()) {
                        break;
                    }

                }
            }

            this.alertOther(otherSoldiers, self.getLastHurtByMob());
        }

    }
    default void alertOther(AbstractClaySoldierEntity pMob, LivingEntity pTarget) {
        pMob.setTarget(pTarget);
    }
    default double getFollowDistance(AbstractClaySoldierEntity self) {
        return self.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
}
