package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ClayMobFollowTeamOwner extends Goal {
    private final ClayMobEntity clayMobEntity;
    @Nullable
    private LivingEntity owner;
    private final float speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public ClayMobFollowTeamOwner(ClayMobEntity clayMobEntity, float speedModifier, float startDistance, float stopDistance) {
        this.clayMobEntity = clayMobEntity;
        this.speedModifier = speedModifier;
        this.navigation = clayMobEntity.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(clayMobEntity.getNavigation() instanceof GroundPathNavigation) && !(clayMobEntity.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }
    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.clayMobEntity.getClayTeamOwner();
        if (livingentity == null) {
            return false;
        } else if (this.clayMobEntity.unableToMoveToOwner()) {
            return false;
        } else if (this.clayMobEntity.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return !this.clayMobEntity.unableToMoveToOwner() && !(this.clayMobEntity.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.clayMobEntity.getPathfindingMalus(PathType.WATER);
        this.clayMobEntity.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.clayMobEntity.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean shouldTryTeleportToOwner = this.clayMobEntity.shouldTryTeleportToOwner();
        if (!shouldTryTeleportToOwner) {
            this.clayMobEntity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.clayMobEntity.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (shouldTryTeleportToOwner) {
                this.clayMobEntity.tryToTeleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }
        }
    }
}
