package net.bumblebee.claysoldiers.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ClayBrushCommandTrigger extends SimpleCriterionTrigger<ClayBrushCommandTrigger.Condition> {
    public void trigger(ServerPlayer serverPlayer, ClayBrushItem.Mode command) {
        trigger(serverPlayer, c -> c.matches(command));
    }

    public static Criterion<Condition> create(ClayBrushItem.Mode command) {
        return ModCritirions.CLAY_BRUSH_COMMAND_TRIGGER.get().createCriterion(new Condition(Optional.empty(), command));
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public record Condition(Optional<ContextAwarePredicate> player, ClayBrushItem.Mode command) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                ClayBrushItem.Mode.CODEC.fieldOf("command").forGetter(Condition::command)
        ).apply(in, Condition::new));

        private boolean matches(ClayBrushItem.Mode mode) {
            return mode == command;
        }
    }
}
