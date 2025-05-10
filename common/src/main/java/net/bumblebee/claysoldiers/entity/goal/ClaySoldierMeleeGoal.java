package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public abstract class ClaySoldierMeleeGoal extends Goal {
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

    public ClaySoldierMeleeGoal(AbstractClaySoldierEntity claySoldier, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.claySoldier = claySoldier;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    protected abstract boolean canUsePredicate();

    @Override
    public boolean canUse() {
        if (!canUsePredicate()) {
            return false;
        }

        long gameTime = this.claySoldier.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS) {
            return false;
        } else {
            this.lastCanUseCheck = gameTime;
            LivingEntity target = this.claySoldier.getTarget();
            if (!(target instanceof ClayMobEntity) && !isTargetFromOwner(target)) {
                return false;
            } else if (!target.isAlive()) {
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

    protected boolean isTargetFromOwner(LivingEntity target) {
        var owner = claySoldier.getClayTeamOwner();
        if (owner != null && target != null) {
            return target.equals(owner.getLastHurtMob()) || target.equals(owner.getLastHurtByMob());
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.claySoldier.getTarget();
        if (!(target instanceof ClayMobEntity) && !isTargetFromOwner(target)){
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.claySoldier.getNavigation().isDone();
        } else return this.claySoldier.isWithinRestriction(target.blockPosition());
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
    public void tick() {
        LivingEntity target = this.claySoldier.getTarget();
        if (target != null) {
            this.claySoldier.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if ((this.followingTargetEvenIfNotSeen || this.claySoldier.getSensing().hasLineOfSight(target))
                    && this.ticksUntilNextPathRecalculation <= 0
                    && (
                    this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                            || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
                            || this.claySoldier.getRandom().nextFloat() < 0.05F)
            ) {
                this.pathedTargetX = target.getX();
                this.pathedTargetY = target.getY();
                this.pathedTargetZ = target.getZ();
                this.ticksUntilNextPathRecalculation = 2;
                double distanceTarget = this.claySoldier.distanceToSqr(target);
                if (distanceTarget > 1024.0) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (distanceTarget > 256.0) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.claySoldier.getNavigation().moveTo(target, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformMeleeEffect(target);
        }
    }

    protected void checkAndPerformMeleeEffect(LivingEntity pTarget) {
        if (canPerformMeleeAction(pTarget)) {
            this.resetAttackCooldown();
            performMeleeAction(pTarget);
            claySoldier.indicateMeleeItemUse();
        }
    }

    protected abstract void performMeleeAction(LivingEntity target);

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(COOLDOWN_BETWEEN_CAN_ATTACKS);
    }

    protected boolean isTimeToPerformMeleeAction() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean canPerformMeleeAction(LivingEntity target) {
        return this.isTimeToPerformMeleeAction() && this.claySoldier.isWithinMeleeAttackRange(target) && this.claySoldier.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
