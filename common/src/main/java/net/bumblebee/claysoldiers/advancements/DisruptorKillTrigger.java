package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class DisruptorKillTrigger extends SimpleCriterionTrigger<DisruptorKillTrigger.Condition> {
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static Criterion<Condition> createInstance(int amountRequired) {
        return ModCriterions.DISRUPTOR_KILL_TRIGGER.get().createCriterion(new Condition(Optional.empty(), amountRequired));
    }

    public void trigger(ServerPlayer serverPlayer, int amountKilled) {
        this.trigger(serverPlayer, instance -> instance.matches(amountKilled));
    }

    public record Condition(Optional<ContextAwarePredicate> player, int amountRequired) implements SimpleCriterionTrigger.SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(Condition::amountRequired)
                ).apply(in, Condition::new));

        private boolean matches(int amountKilled) {
            return amountKilled >= amountRequired;
        }
    }
}
