package net.bumblebee.claysoldiers.integration;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.WalkOrRunToWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;

import java.util.List;
import java.util.Map;

public interface ClaySoldierBrain<T extends AbstractClaySoldierEntity & SmartBrainOwner<T>> extends SmartBrainOwner<T> {
    int DELAY_BEFORE_LAST_ATTACK = 60;


    @Override
    default List<? extends ExtendedSensor<? extends T>> getSensors() {
        return ObjectArrayList.of(
                new HurtBySensor<T>().setPredicate((d, self) -> self.fightsBack())
                // Ride Target
                // Owner Hurt By Target
                // Owner Attack
                // Nearest target + specific
                // nearby Block/Item Poi
                // Walk Target = owner

        );
    }

    @Override
    default BrainActivityGroup<? extends T> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new Idle<T>()
                        .startCondition(s -> this.canSitCondition(s, true))
                        .stopIf(s -> !this.canSitCondition(s, false)),
                new LookAtTargetSink(40, 300),
                new MoveToWalkTarget<>()
                // Mount
        );
    }

    @Override
    default BrainActivityGroup<? extends T> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new WalkOrRunToWalkTarget<>()

        );
    }

    @Override
    default BrainActivityGroup<? extends T> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new AnimatableRangedAttack<T>(0)
                        .attackRadius(10f)
                        .cooldownFor(self -> self.getAttackType().isSupportive() ? 220 : 40)
                        .startCondition(AbstractClaySoldierEntity::canPerformRangeAttack),
                new AnimatableMeleeAttack<T>(0)
                        .startCondition(AbstractClaySoldierEntity::fightsBack)
                        .whenStarting(s -> s.setAggressive(true))
                        .whenStopping(s -> s.setAggressive(false))
                // Melee Supprot Goal
        );
    }

    @Override
    default Map<Activity, BrainActivityGroup<? extends T>> getAdditionalTasks() {
        return Map.of(
                Activity.WORK, new BrainActivityGroup<T>(Activity.WORK).behaviours(
                        //WorkSelector
                        //Use Poi
                )
        );
    }

    @Override
    default List<Activity> getActivityPriorities() {
        return ObjectArrayList.of(Activity.WORK, Activity.FIGHT, Activity.IDLE);
    }

    private boolean canTargetEntity(LivingEntity target, T self) {
        if (self.isAbleToRide() && self.canMountEntity(target)) {
            return true;
        }

        return false;
    }

    private boolean canSitCondition(T self, boolean start) {
        if (self.getLastHurtByMobTimestamp() + DELAY_BEFORE_LAST_ATTACK > self.tickCount) {
            return false;
        }

        if (!self.isOrderedToSit()) {
            return false;
        }

        if (!start) {
            return true;
        }

        LivingEntity owner = self.getClayTeamOwner();
        if (owner == null) {
            return false;
        } else if (owner.getLastHurtByMob() != null && self.distanceToSqr(owner) < 144.0) {
            return false;
        }

        Entity vehicle = self.getVehicle();
        if (vehicle != null) {
            return !vehicle.isInWaterOrBubble();
        } else {
            if (self.isInWaterOrBubble()) {
                return false;
            } else {
                return self.onGround();
            }
        }
    }
}
