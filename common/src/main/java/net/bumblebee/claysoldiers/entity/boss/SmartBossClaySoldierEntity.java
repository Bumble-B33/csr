package net.bumblebee.claysoldiers.entity.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.AvoidSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.EscapeSun;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;

public class SmartBossClaySoldierEntity extends BossClaySoldierEntity implements SmartBrainOwner<SmartBossClaySoldierEntity> {
    public SmartBossClaySoldierEntity(EntityType<? extends BossClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Brain

    @Override
    protected void registerGoals() {
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends SmartBossClaySoldierEntity>> getSensors() {
        return ObjectArrayList.of(
                new HurtBySensor<>(),
                new NearbyPlayersSensor<>(),
                new NearbyLivingEntitySensor<SmartBossClaySoldierEntity>().setPredicate((target, entity) -> entity.targetPredicate(target))
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartBossClaySoldierEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTargetSink(40, 300),
                new MoveToWalkTarget<>());
    }


    @Override
    public BrainActivityGroup<? extends SmartBossClaySoldierEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new TargetOrRetaliate<>(),
                        new FirstApplicableBehaviour<>(
                                new AvoidSun<BossClaySoldierEntity>(),
                                new EscapeSun<BossClaySoldierEntity>().cooldownFor(entity -> 20)
                        ).startCondition(BossClaySoldierEntity::isUndead),
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()
                ),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<BossClaySoldierEntity>()
                                .walkTargetPredicate(BossClaySoldierEntity::shouldWalkToTarget)
                                .avoidWaterWhen(BossClaySoldierEntity::isVampire),
                        new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60))
                )
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartBossClaySoldierEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<>(),
                new FirstApplicableBehaviour<>(
                        new AnimatableRangedAttack<>(20)
                                .cooldownFor(s -> 60),
                        new AnimatableMeleeAttack<BossClaySoldierEntity>(0)
                                .whenActivating(AbstractClaySoldierEntity::indicateMeleeItemUse)
                                .whenStarting(entity -> setAggressive(true))
                                .whenStopping(entity -> setAggressive(false))
                )
        );
    }
}
