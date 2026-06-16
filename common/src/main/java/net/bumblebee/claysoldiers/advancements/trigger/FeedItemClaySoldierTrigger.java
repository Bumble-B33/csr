package net.bumblebee.claysoldiers.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FeedItemClaySoldierTrigger extends SimpleCriterionTrigger<FeedItemClaySoldierTrigger.Condition> {
    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static Builder wax() {
        return new Builder(Type.WAX);
    }
    public static Builder ofCookie() {
        return new Builder(Type.COOKIE);
    }
    public static Builder ofLoyalty() {
        return new Builder(Type.LOYALTY);
    }
    public static Builder ofChip() {
        return new Builder(Type.CHIP);
    }


    public void triggerWax(ServerPlayer serverPlayer, ItemStack stack) {
        this.trigger(serverPlayer,
                c -> c.type == Type.WAX
                && c.itemPredicate.map(i -> i.test(stack)).orElse(true)
        );
    }

    public void triggerFood(ServerPlayer serverPlayer, ItemStack stack) {
        this.trigger(serverPlayer,
                c -> c.type == Type.COOKIE
                        && c.itemPredicate.map(i -> i.test(stack)).orElse(true)
        );
    }

    public void triggerLoyalty(ServerPlayer serverPlayer, ItemStack stack) {
        this.trigger(serverPlayer,
                c -> (c.type == Type.COOKIE || c.type == Type.LOYALTY)
                        && c.itemPredicate.map(i -> i.test(stack)).orElse(true)
        );
    }

    public void triggerChip(ServerPlayer serverPlayer, ItemStack stack) {
        this.trigger(serverPlayer,
                c -> (c.type == Type.CHIP)
                        && c.itemPredicate.map(i -> i.test(stack)).orElse(true)
        );
    }


    public static class Builder {
        private ItemPredicate itemPredicate;
        private final Type type;

        private Builder(Type type) {
            this.type = type;
        }

        public Builder setItemPredicate(ItemPredicate itemPredicate) {
            this.itemPredicate = itemPredicate;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCritirions.FEED_CLAY_SOLDIER_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(itemPredicate), type));

        }
    }

    private enum Type implements StringRepresentable {
        WAX("wax"),
        COOKIE("food"),
        CHIP("chip"),
        LOYALTY("loyalty");

        private static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> itemPredicate, Type type) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(Condition::itemPredicate),
                Type.CODEC.fieldOf("type").forGetter(Condition::type)
                ).apply(in, Condition::new));
    }
}
