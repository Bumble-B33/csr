package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoiWithSource;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SoldierPoiUseTrigger extends SimpleCriterionTrigger<SoldierPoiUseTrigger.Condition> {
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, SoldierPoiWithSource.Type type) {
        this.trigger(serverPlayer, instance -> instance.matches(type));
    }

    public static Criterion<Condition> of(SoldierPoiWithSource.Type type) {
        return ModCriterions.SOLDIER_POI_USE_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.of(type)));
    }

    public static Criterion<Condition> any() {
        return ModCriterions.SOLDIER_POI_USE_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.empty()));
    }



    public record Condition(Optional<ContextAwarePredicate> player, Optional<SoldierPoiWithSource.Type> type) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                SoldierPoiWithSource.Type.CODEC.optionalFieldOf("type").forGetter(Condition::type)
                ).apply(in, Condition::new));

        private boolean matches(SoldierPoiWithSource.Type type) {
            return this.type.map(t -> t == type).orElse(true);
        }
    }
}
