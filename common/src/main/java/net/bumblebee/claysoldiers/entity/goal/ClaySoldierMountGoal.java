package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class ClaySoldierMountGoal extends Goal {
    protected final AbstractClaySoldierEntity claySoldier;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private long lastCanUseCheck;
    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 10L;
    private static final int COOLDOWN_BETWEEN_CAN_ATTACKS = 2;

    public ClaySoldierMountGoal(AbstractClaySoldierEntity pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
        this.claySoldier = pMob;
        this.speedModifier = pSpeedModifier;
        this.followingTargetEvenIfNotSeen = pFollowingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    @Override
    public boolean canUse() {
        if (claySoldier.isPassenger()) {
            return false;
        }

        long gameTime = this.claySoldier.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS) {
            return false;
        } else {
            this.lastCanUseCheck = gameTime;
            LivingEntity target = this.claySoldier.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive() || !claySoldier.canMountEntity(target)) {
                return false;
            } else {
                this.path = this.claySoldier.getNavigation().createPath(target, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.claySoldier.isWithinMeleeAttackRange(target);
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (claySoldier.isPassenger()) {
            return false;
        }

        LivingEntity livingentity = this.claySoldier.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive() || !claySoldier.canMountEntity(livingentity)) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.claySoldier.getNavigation().isDone();
        } else return this.claySoldier.isWithinRestriction(livingentity.blockPosition());
    }

    @Override
    public void start() {
        this.claySoldier.getNavigation().moveTo(this.path, this.speedModifier);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }


    @Override
    public void stop() {
        LivingEntity livingentity = this.claySoldier.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.claySoldier.setTarget(null);
        }
        this.claySoldier.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }


    @Override
    public void tick() {
        LivingEntity livingentity = this.claySoldier.getTarget();
        if (livingentity != null) {
            this.claySoldier.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if ((this.followingTargetEvenIfNotSeen || this.claySoldier.getSensing().hasLineOfSight(livingentity))
                    && this.ticksUntilNextPathRecalculation <= 0
                    && (
                    this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                            || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
                            || this.claySoldier.getRandom().nextFloat() < 0.05F)
            ) {
                this.pathedTargetX = livingentity.getX();
                this.pathedTargetY = livingentity.getY();
                this.pathedTargetZ = livingentity.getZ();
                this.ticksUntilNextPathRecalculation = 2;
                double distanceTarget = this.claySoldier.distanceToSqr(livingentity);
                if (distanceTarget > 1024.0) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (distanceTarget > 256.0) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.claySoldier.getNavigation().moveTo(livingentity, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.tryRiding(livingentity);
        }
    }

    protected void tryRiding(LivingEntity pTarget) {
        if (this.canTryToRide(pTarget)) {
            claySoldier.startRiding(pTarget);
            claySoldier.setTarget(null);

            resetRideCooldown();
        }
    }

    protected void resetRideCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(COOLDOWN_BETWEEN_CAN_ATTACKS);
    }

    protected boolean isTimeToTryToRide() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean canTryToRide(LivingEntity pEntity) {
        return this.isTimeToTryToRide() && this.claySoldier.isWithinMeleeAttackRange(pEntity) && this.claySoldier.getSensing().hasLineOfSight(pEntity);
    }

}
