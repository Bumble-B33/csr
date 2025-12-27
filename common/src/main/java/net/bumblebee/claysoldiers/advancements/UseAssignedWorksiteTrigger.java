package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        private ResourceLocation type;
        private EntityPredicate predicate;
        private MinMaxBounds.Ints time;
        private MinMaxBounds.Ints timesUsed = MinMaxBounds.Ints.atLeast(1);


        private Builder() {
        }

        public Builder setType(ResourceLocation type) {
            this.type = type;
            return this;
        }

        public Builder setPredicate(EntityPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder setTime(MinMaxBounds.Ints time) {
            this.time = time;
            return this;
        }

        public Builder setTimesUsed(MinMaxBounds.Ints timesUsed) {
            this.timesUsed = timesUsed;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCriterions.USE_ASSIGNED_POI_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(predicate), Optional.ofNullable(type), Optional.ofNullable(time), timesUsed));
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> entity, Optional<ResourceLocation> type, Optional<MinMaxBounds.Ints> time, MinMaxBounds.Ints timesUsed) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                EntityPredicate.CODEC.optionalFieldOf("clay_mob").forGetter(Condition::entity),
                ResourceLocation.CODEC.optionalFieldOf("type").forGetter(Condition::type),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("time").forGetter(Condition::time),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("times_used", MinMaxBounds.Ints.atLeast(1)).forGetter(Condition::timesUsed)
        ).apply(in, Condition::new));

        private boolean matches(ServerPlayer serverPlayer, Entity entity, ResourceLocation resourceLocation, int timesUsed) {
            if (!this.timesUsed.matches(timesUsed)) {
                return false;
            }
            if (this.time.isPresent()) {
                if (!this.time.orElseThrow().matches((int) serverPlayer.level().getDayTime() % 24000)) {
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
