package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public class MultiSpawnItemUseTrigger extends SimpleCriterionTrigger<MultiSpawnItemUseTrigger.Condition> {
    public void trigger(ServerPlayer serverPlayer, EntityType<?> type, int amountSpawned) {
        trigger(serverPlayer, c -> c.matches(type, amountSpawned));
    }

    public static Builder of() {
        return new Builder();
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static class Builder {
        private EntityTypePredicate entityTypePredicate;
        private MinMaxBounds.Ints minMaxBounds;

        private Builder() {
        }

        public Builder setEntityTypePredicate(EntityTypePredicate entityTypePredicate) {
            this.entityTypePredicate = entityTypePredicate;
            return this;
        }

        public Builder setMinMaxBounds(MinMaxBounds.Ints minMaxBounds) {
            this.minMaxBounds = minMaxBounds;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCriterions.MULTI_SPAWN_ITEM_USE_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(entityTypePredicate), Optional.of(minMaxBounds)));
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<EntityTypePredicate> type, Optional<MinMaxBounds.Ints> spawnCount) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(Condition::type),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("spawn_amount").forGetter(Condition::spawnCount)
            ).apply(in, Condition::new));

        private boolean matches(EntityType<?> type, int amountSpawned) {
            if (this.type.isPresent() && !this.type.orElseThrow().matches(type)) {
                return false;
            }
            return this.spawnCount.map(b -> b.matches(amountSpawned)).orElse(true);
        }
    }
}
