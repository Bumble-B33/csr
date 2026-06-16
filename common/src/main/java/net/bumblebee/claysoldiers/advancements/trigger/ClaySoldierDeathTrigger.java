package net.bumblebee.claysoldiers.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClaySoldierDeathTrigger extends SimpleCriterionTrigger<ClaySoldierDeathTrigger.Condition> {
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }


    public static Criterion<Condition> createBattle(@Nullable EntityPredicate builder) {
        return ModCritirions.CLAY_SOLDIER_DEATH.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(builder)));
    }

    public void trigger(ServerPlayer serverPlayer, @Nullable Entity killer) {
        this.trigger(serverPlayer, s -> s.match(serverPlayer, killer));
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> entityPredicate) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                EntityPredicate.CODEC.optionalFieldOf("killer").forGetter(Condition::entityPredicate)
        ).apply(in, Condition::new));

        private boolean match(ServerPlayer serverPlayer, @Nullable Entity entity) {
            return this.entityPredicate.map(p -> p.matches(serverPlayer, entity)).orElse(true);
        }
    }
}
