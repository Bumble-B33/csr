package net.bumblebee.claysoldiers.entity.common.programmable.chips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.goal.ClaySoldierMeleeAttackGoal;
import net.bumblebee.claysoldiers.entity.goal.ClaySoldierRangedAttackGoal;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;

import java.util.function.BiConsumer;

public class CombatChip extends ClaySoldierChip<CombatChip.Data> {
    private static final ResourceLocation ASSET_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "combat");
    public static final Codec<Data> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.BOOL.optionalFieldOf("monster", false).forGetter(d -> d.monster),
            Codec.BOOL.optionalFieldOf("animal", false).forGetter(d -> d.animal),
            Codec.BOOL.optionalFieldOf("ignoreBabies", false).forGetter(d -> d.ignoreBaby)
    ).apply(in, Data::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, d -> d.monster,
            ByteBufCodecs.BOOL, d -> d.animal,
            ByteBufCodecs.BOOL, d -> d.ignoreBaby,
            Data::new
    );

    public CombatChip(ProgrammableClaySoldierEntity soldier, Data data) {
        super(soldier, data);
    }

    @Override
    public void addGoals(ServerLevel level, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
        goalAdder.accept(1, new ClaySoldierRangedAttackGoal(soldier, 1, 10f));
        goalAdder.accept(0, new ClaySoldierMeleeAttackGoal(soldier, 1, false));

        targetAdder.accept(0, new CombatNearestAttackableTargetGoal<>(soldier, data));
    }

    @Override
    public Type<Data> getType() {
        return ClaySoldierChips.COMBAT_TYPE.get();
    }

    @Override
    public ResourceLocation assetId() {
        return ASSET_ID;
    }

    @Override
    public Component info() {
        var mut = super.info().copy();
        if (data.monster) {
            return mut.append(" Monster");
        }
        if (data.animal) {
            return mut.append(" Animal");
        }

        return mut.append(" Any");
    }

    public static class Data {
        private final boolean monster;
        private final boolean animal;
        private final boolean ignoreBaby;

        private Data(boolean monster, boolean animal, boolean ignoreBaby) {
            this.monster = monster;
            this.animal = animal;
            this.ignoreBaby = ignoreBaby;
        }

        private Class<? extends LivingEntity> getTargetType() {
            if (monster) {
                return Monster.class;
            }
            if (animal) {
                return Animal.class;
            }
            return LivingEntity.class;
        }

        public static Data monster() {
            return new Data(true, false, false);
        }

        public static Data animal(boolean ignoreBabies) {
            return new Data(false, true, ignoreBabies);
        }
    }

    public static class CombatNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final Data data;

        @SuppressWarnings("unchecked")
        private CombatNearestAttackableTargetGoal(Mob mob, Data data) {
            super(mob, (Class<T>) (data.getTargetType()), true);
            this.data = data;
        }

        private boolean targetPredicate(T target) {
            return !target.isBaby() || (!data.ignoreBaby);
        }

        @Override
        protected void findTarget() {
            ServerLevel serverlevel = getServerLevel(this.mob);
            this.target = serverlevel.getNearestEntity(
                    this.mob.level().getEntitiesOfClass(
                            targetType,
                            this.getTargetSearchArea(this.getFollowDistance()), this::targetPredicate),
                    targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());


        }
    }
}
