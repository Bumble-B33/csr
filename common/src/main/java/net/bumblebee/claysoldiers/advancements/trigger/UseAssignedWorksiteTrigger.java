package net.bumblebee.claysoldiers.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class UseAssignedWorksiteTrigger extends SimpleCriterionTrigger<UseAssignedWorksiteTrigger.Condition> {
    public void trigger(ServerPlayer serverPlayer, Entity user, AssignableWorksiteCapability poiCapability, int timesUsed) {
        trigger(serverPlayer, c -> c.matches(serverPlayer, user, poiCapability.descriptionId(), timesUsed));
    }

    public static Builder of() {
        return new Builder();
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static class Builder {
        private Identifier type;
        private EntityPredicate predicate;
        private ResourceKey<ClockTimeMarker> time;
        private MinMaxBounds.Ints timesUsed = MinMaxBounds.Ints.atLeast(1);


        private Builder() {
        }

        public Builder setType(Identifier type) {
            this.type = type;
            return this;
        }

        public Builder setPredicate(EntityPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder setTime(ResourceKey<ClockTimeMarker> time) {
            this.time = time;
            return this;
        }

        public Builder setTimesUsed(MinMaxBounds.Ints timesUsed) {
            this.timesUsed = timesUsed;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCritirions.USE_ASSIGNED_POI_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(predicate), Optional.ofNullable(type), Optional.ofNullable(time), timesUsed));
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> entity, Optional<Identifier> type, Optional<ResourceKey<ClockTimeMarker>> time, MinMaxBounds.Ints timesUsed) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                EntityPredicate.CODEC.optionalFieldOf("clay_mob").forGetter(Condition::entity),
                Identifier.CODEC.optionalFieldOf("type").forGetter(Condition::type),
                ClockTimeMarker.KEY_CODEC.optionalFieldOf("time").forGetter(Condition::time),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("times_used", MinMaxBounds.Ints.atLeast(1)).forGetter(Condition::timesUsed)
        ).apply(in, Condition::new));

        private boolean matches(ServerPlayer serverPlayer, Entity entity, Identifier resourceLocation, int timesUsed) {
            if (!this.timesUsed.matches(timesUsed)) {
                return false;
            }
            if (this.time.isPresent()) {
                if (!serverPlayer.level().dimensionType().defaultClock().map(
                        c -> !serverPlayer.level().clockManager().isAtTimeMarker(c, time.orElseThrow())).orElse(true)) {
                    return false;
                }

            }
            if (this.entity.isPresent() && !this.entity.get().matches(serverPlayer, entity)) {
                return false;
            }
            return type.map(resourceLocation::equals).orElse(true);
        }
    }
}
