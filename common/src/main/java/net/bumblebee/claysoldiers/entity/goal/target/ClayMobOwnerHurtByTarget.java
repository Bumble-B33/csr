package net.bumblebee.claysoldiers.entity.goal.target;

import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class ClayMobOwnerHurtByTarget extends TargetGoal {
    private final ClayMobEntity clayMob;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public ClayMobOwnerHurtByTarget(ClayMobEntity clayMob) {
        super(clayMob, false);
        this.clayMob = clayMob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (clayMob.isOrderedToIgnoreOwner()) {
            return false;
        }

        LivingEntity owner = this.clayMob.getClayTeamOwner();
        if (owner != null && !this.clayMob.getOrderedCommand()) {
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int lastHurtByMobTimestamp = owner.getLastHurtByMobTimestamp();
            return lastHurtByMobTimestamp != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                    && clayMob.wantsToAttackCommanded(ownerLastHurtBy, owner);
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity livingentity = this.clayMob.getClayTeamOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
