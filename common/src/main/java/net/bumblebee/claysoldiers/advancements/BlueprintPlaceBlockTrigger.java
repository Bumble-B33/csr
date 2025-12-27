package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class BlueprintPlaceBlockTrigger extends SimpleCriterionTrigger<BlueprintPlaceBlockTrigger.Condition> {
    public void trigger(ServerPlayer serverPlayer, ResourceKey<BlueprintData> key, boolean completion) {
        trigger(serverPlayer, c -> c.matches(key, completion));
    }

    public static Builder of() {
        return new Builder();
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static class Builder {
        private ResourceKey<BlueprintData> key;
        private boolean completionRequired = false;

        private Builder() {
        }

        public Builder setKey(ResourceKey<BlueprintData> key) {
            this.key = key;
            return this;
        }

        public Builder setCompletionRequired() {
            this.completionRequired = true;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCriterions.BLUEPRINT_COMPLETION_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(key), completionRequired));
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<ResourceKey<BlueprintData>> type, boolean completionRequired) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                ResourceKey.codec(ModRegistries.BLUEPRINTS).optionalFieldOf("type").forGetter(Condition::type),
                Codec.BOOL.optionalFieldOf("completion", false).forGetter(Condition::completionRequired)
        ).apply(in, Condition::new));

        private boolean matches(ResourceKey<BlueprintData> data, boolean completion) {
            if (this.completionRequired && !completion) {
                return false;
            }
            return type.map(k -> k.equals(data)).orElse(true);
        }
    }
}
