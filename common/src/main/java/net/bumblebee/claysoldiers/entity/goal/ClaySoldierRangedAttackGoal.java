package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ClaySoldierRangedAttackGoal extends Goal {
    private static final int DEFAULT_ATTACK_TIME = 40;
    private final AbstractClaySoldierEntity mob;
    @Nullable
    private LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private int attackIntervalMin;

    private final float attackRadius;
    private final float attackRadiusSqr;

    public ClaySoldierRangedAttackGoal(AbstractClaySoldierEntity pRangedAttackMob, double pSpeedModifier, float pAttackRadius) {
        this.mob = pRangedAttackMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = DEFAULT_ATTACK_TIME;
        this.attackRadius = pAttackRadius;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.canPerformRangeAttack()) {
            return false;
        }
        LivingEntity livingentity = this.mob.getTarget();
        if (!(livingentity instanceof AbstractClaySoldierEntity)) {
            return false;
        }
        if (livingentity.isAlive()) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double distanceToTarget = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(this.target);
        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(distanceToTarget > (double) this.attackRadiusSqr) && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!canSeeTarget) {
                return;
            }

            float f = (float) Math.sqrt(distanceToTarget) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);
            this.mob.performRangedAttack(this.target, f1);
            this.attackTime = Mth.floor(f * (float) this.attackIntervalMin);
            updateAttackInterval();
        } else if (this.attackTime < 0) {
            this.attackTime = attackIntervalMin;
        }
    }

    private void updateAttackInterval() {
        if (mob.getAttackType().isSupportive()) {
            attackIntervalMin = 220;
        } else {
            attackIntervalMin = DEFAULT_ATTACK_TIME;
        }
    }
}
