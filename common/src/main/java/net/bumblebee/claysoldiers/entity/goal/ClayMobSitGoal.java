package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ClayMobSitGoal extends Goal {
    private static final int DELAY_BEFORE_LAST_ATTACK = 60;
    protected final ClayMobEntity clayMobEntity;

    public ClayMobSitGoal(ClayMobEntity clayMobEntity) {
        this.clayMobEntity = clayMobEntity;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    protected boolean isCurrentlyFighting() {
        return clayMobEntity.getLastHurtByMobTimestamp() + DELAY_BEFORE_LAST_ATTACK > clayMobEntity.tickCount;
    }

    @Override
    public boolean canContinueToUse() {
        if (isCurrentlyFighting()) {
            return false;
        }
        return this.clayMobEntity.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        if (isCurrentlyFighting()) {
            return false;
        }

        LivingEntity owner = this.clayMobEntity.getClayTeamOwner();
        if (owner == null) {
            return false;
        } else if (!this.clayMobEntity.isOrderedToSit()) {
            return false;
        } else if (owner.getLastHurtByMob() != null && this.clayMobEntity.distanceToSqr(owner) < 144.0) {
            return false;
        }

        Entity vehicle = clayMobEntity.getVehicle();
        if (vehicle != null) {
            return !vehicle.isInWaterOrBubble();
        } else {
            if (this.clayMobEntity.isInWaterOrBubble()) {
                return false;
            } else {
                return this.clayMobEntity.onGround();
            }
        }
    }



    @Override
    public void start() {
        this.clayMobEntity.getNavigation().stop();
        Entity vehicle = clayMobEntity.getVehicle();
        this.clayMobEntity.setInSittingPose(true);

        if (vehicle == null) {
            this.clayMobEntity.setPose(Pose.SITTING);
            return;
        }

        if (vehicle instanceof ClayMobEntity clayVehicle) {
            clayVehicle.setPose(Pose.SITTING);
            clayVehicle.setInSittingPose(true);
        }
    }

    @Override
    public void stop() {
        this.clayMobEntity.setInSittingPose(false);
        this.clayMobEntity.setPose(Pose.STANDING);

        if (clayMobEntity.getVehicle() instanceof ClayMobEntity clayVehicle) {
            clayVehicle.setPose(Pose.STANDING);
            clayVehicle.setInSittingPose(false);
        }
    }
}
