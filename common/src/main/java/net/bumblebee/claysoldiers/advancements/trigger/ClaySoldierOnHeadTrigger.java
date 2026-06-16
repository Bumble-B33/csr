package net.bumblebee.claysoldiers.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ClaySoldierOnHeadTrigger extends SimpleCriterionTrigger<ClaySoldierOnHeadTrigger.Condition> {
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }


    public static Criterion<Condition> create() {
        return ModCritirions.CLAY_SOLDIER_ON_HEAD_TRIGGER.get().createCriterion(new Condition(Optional.empty()));
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, _ -> true);
    }

    public record Condition(Optional<ContextAwarePredicate> player) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player)
        ).apply(in, Condition::new));
    }
}
