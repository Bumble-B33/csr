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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HitWithClayBlockTrigger extends SimpleCriterionTrigger<HitWithClayBlockTrigger.Condition> {
    public void trigger(ServerPlayer serverPlayer, Entity target, boolean block, @Nullable ItemStack weapon) {
        trigger(serverPlayer, c -> c.matches(serverPlayer, target, block, weapon));
    }

    public static Builder of() {
        return new Builder();
    }

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public static class Builder {
        private EntityPredicate target;
        private Boolean isBlock;
        private ItemPredicate weapon;

        private Builder() {
        }

        public Builder setTarget(EntityPredicate.Builder target) {
            return setTarget(target.build());
        }

        public Builder setTarget(EntityPredicate target) {
            this.target = target;
            return this;
        }

        public Builder needsToBeBlock() {
            isBlock = true;
            return this;
        }

        public Builder needsToBeSoldier() {
            isBlock = false;
            return this;
        }

        public Builder setWeapon(ItemPredicate.Builder weapon) {
            return setWeapon(weapon.build());
        }

        public Builder setWeapon(ItemPredicate weapon) {
            this.weapon = weapon;
            return this;
        }

        public Criterion<Condition> build() {
            return ModCritirions.HIT_WITH_CLAY_BLOCK_TRIGGER.get().createCriterion(new Condition(Optional.empty(), Optional.ofNullable(target), Optional.ofNullable(isBlock), Optional.ofNullable(weapon)));
        }
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> target, Optional<Boolean> isBlock, Optional<ItemPredicate> weapon) implements SimpleInstance {
        private static final Codec<Condition> CODEC = RecordCodecBuilder.create(in -> in.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                EntityPredicate.CODEC.optionalFieldOf("target").forGetter(Condition::target),
                Codec.BOOL.optionalFieldOf("isBlock").forGetter(Condition::isBlock),
                ItemPredicate.CODEC.optionalFieldOf("weapon").forGetter(Condition::weapon)
                ).apply(in, Condition::new));

        private boolean matches(ServerPlayer player, @Nullable Entity target, boolean block, @Nullable ItemStack weapon) {
            if (this.target.isPresent() && !this.target.get().matches(player, target)) {
                return false;
            }
            if (this.isBlock.isPresent() && this.isBlock.get() != block) {
                return false;
            }
            if (weapon == null && this.weapon.isPresent()) {
                return false;
            }
            return weapon == null || this.weapon.map(p -> p.test(weapon)).orElse(true);
        }
    }
}
