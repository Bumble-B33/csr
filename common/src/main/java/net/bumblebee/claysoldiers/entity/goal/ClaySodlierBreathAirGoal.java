package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ClaySodlierBreathAirGoal extends Goal {
    private final AbstractClaySoldierEntity soldier;
    private boolean started = false;

    public ClaySodlierBreathAirGoal(AbstractClaySoldierEntity mob) {
        this.soldier = mob;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));

    }

    @Override
    public boolean canUse() {
        return started || (soldierAbleToUse() && (this.soldier.getAirSupply() < 100 || alwaysUse()));
    }

    @Override
    public boolean canContinueToUse() {
        return started || (soldierAbleToUse() && alwaysUse());
    }

    private boolean soldierAbleToUse() {
        return (this.soldier.isInWater() && this.soldier.getFluidHeight(FluidTags.WATER) > this.soldier.getFluidJumpThreshold()) && soldier.canSwim();
    }
    private boolean alwaysUse() {
        return soldier.getTarget() == null && !soldier.isInCombat() && soldier.allProperties().breathHoldDuration() < BreathHoldPropertyType.MAX_BREATH_HOLD;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void tick() {
        if (soldier.getRandom().nextFloat() < 0.8F) {
            soldier.getJumpControl().jump();
        }
        if (soldier.getAirSupply() >= soldier.getMaxAirSupply()) {
            started = false;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
