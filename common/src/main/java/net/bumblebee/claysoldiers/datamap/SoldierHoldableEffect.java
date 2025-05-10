package net.bumblebee.claysoldiers.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.CustomEquipCapability;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunction;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.claypoifunction.ClaySoldierInventorySetter;
import net.bumblebee.claysoldiers.clayremovalcondition.RemovalCondition;
import net.bumblebee.claysoldiers.clayremovalcondition.RemovalConditionType;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.RangedAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SoldierHoldableEffect {
    public static final Codec<SoldierHoldableEffect> CODEC = RecordCodecBuilder.create(in -> in.group(
            SoldierPropertyMap.CODEC.optionalFieldOf("properties", SoldierPropertyMap.EMPTY_MAP).forGetter(SoldierHoldableEffect::properties),
            SoldierEquipmentSlot.CODEC_MAPPED.forGetter(SoldierHoldableEffect::slots),
            SoldierPickUpPriority.CODEC.optionalFieldOf("pick_priority", SoldierPickUpPriority.NORMAL.get()).forGetter(SoldierHoldableEffect::pickUpPriority),
            DropRateProperty.CODEC.optionalFieldOf("drop_rate", DropRateProperty.NORMAL.get()).forGetter(SoldierHoldableEffect::dropRate),
            Codec.intRange(1, Item.DEFAULT_MAX_STACK_SIZE).optionalFieldOf("max_stack_size", 1).forGetter(s -> s.maxStackSize),
            ClayPredicate.CODEC.optionalFieldOf("predicate", ClayPredicates.ConstantPredicate.getAlwaysTruePredicate()).forGetter(SoldierHoldableEffect::predicate),
            ClayPoiFunction.CODEC.listOf().optionalFieldOf("on_pick", List.of()).forGetter(s -> s.onPickUpFunction),
            RemovalConditionType.PAIR_CODEC.optionalFieldOf("removal_condition", Map.of()).forGetter(s -> s.removalConditionType)
    ).apply(in, (SoldierHoldableEffect::new)));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierHoldableEffect> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SoldierHoldableEffect decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return new SoldierHoldableEffect(
                    SoldierPropertyMap.STREAM_CODEC.decode(registryFriendlyByteBuf),
                    SoldierEquipmentSlot.LIST_STREAM_CODEC.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.VAR_INT.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.FLOAT.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.VAR_INT.decode(registryFriendlyByteBuf),
                    ClayPredicate.STREAM_CODEC.decode(registryFriendlyByteBuf),
                    ClayPoiFunction.LIST_STREAM_CODEC.decode(registryFriendlyByteBuf),
                    RemovalConditionType.PAIR_STREAM_CODEC.decode(registryFriendlyByteBuf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf byteBuf, SoldierHoldableEffect soldierHoldableEffect) {
            SoldierPropertyMap.STREAM_CODEC.encode(byteBuf, soldierHoldableEffect.properties);
            SoldierEquipmentSlot.LIST_STREAM_CODEC.encode(byteBuf, soldierHoldableEffect.slots);
            ByteBufCodecs.VAR_INT.encode(byteBuf, soldierHoldableEffect.pickUpPriority);
            ByteBufCodecs.FLOAT.encode(byteBuf, soldierHoldableEffect.dropRate);
            ByteBufCodecs.VAR_INT.encode(byteBuf, soldierHoldableEffect.maxStackSize);
            ClayPredicate.STREAM_CODEC.encode(byteBuf, soldierHoldableEffect.predicate);
            ClayPoiFunction.LIST_STREAM_CODEC.encode(byteBuf, soldierHoldableEffect.onPickUpFunction);
            RemovalConditionType.PAIR_STREAM_CODEC.encode(byteBuf, soldierHoldableEffect.removalConditionType);
        }
    };

    private final SoldierPropertyMap properties;
    private final List<SoldierEquipmentSlot> slots;
    private final int pickUpPriority;
    private final float dropRate;
    private final int maxStackSize;
    private final ClayPredicate<?> predicate;
    private final List<ClayPoiFunction<?>> onPickUpFunction;
    private final Map<RemovalConditionType<?>, RemovalCondition> removalConditionType;

    private SoldierHoldableEffect(SoldierPropertyMap properties, List<SoldierEquipmentSlot> slots, int pickUpPriority, float dropRate, int maxStackSize, ClayPredicate<?> predicate, List<ClayPoiFunction<?>> onPickUpFunction, Map<RemovalConditionType<?>, RemovalCondition> removalConditionType) {
        this.properties = properties;
        this.slots = slots;
        this.pickUpPriority = pickUpPriority;
        this.dropRate = dropRate;
        this.predicate = predicate;
        this.onPickUpFunction = onPickUpFunction;
        this.removalConditionType = removalConditionType;
        this.maxStackSize = maxStackSize;
    }

    private SoldierHoldableEffect(SoldierPropertyMap.Builder properties, List<SoldierEquipmentSlot> slots, SoldierPickUpPriority pickUpPriority, DropRateProperty dropRate,
                                 ClayPredicate<?> predicate, List<ClayPoiFunction<?>> onPickUpFunction) {
        this(properties.build(), slots, pickUpPriority.get(), dropRate.get(), 1, predicate, onPickUpFunction, Map.of());
    }
    public SoldierHoldableEffect(SoldierPropertyMap.Builder properties, SoldierEquipmentSlot slot) {
        this(properties, slot, SoldierPickUpPriority.NORMAL);
    }
    public SoldierHoldableEffect(SoldierPropertyMap.Builder properties, SoldierEquipmentSlot slot, SoldierPickUpPriority pickUpPriority) {
        this(properties, List.of(slot), pickUpPriority, DropRateProperty.NORMAL, ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(), List.of());
    }

    public float damage() {
        return properties.damage();
    }

    public boolean throwable() {
        return properties.throwable().canPerformRangedAttack();
    }
    public boolean throwableType(RangedAttackType type) {
        return properties.throwable() ==  type;
    }

    public List<SpecialAttack<?>> getSpecialRangedAttacks() {
        return properties.specialAttacks(SpecialAttackType.RANGED, SpecialEffectCategory.BOTH);
    }

    public SoldierPropertyMap properties() {
        return properties;
    }

    public List<SoldierEquipmentSlot> slots() {
        return slots;
    }

    public int pickUpPriority() {
        return pickUpPriority;
    }

    public float dropRate() {
        return dropRate;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public ClayPredicate<?> predicate() {
        return predicate;
    }

    public List<ClayPoiFunction<?>> getOnPickUpFunctions() {
        return onPickUpFunction;
    }

    public void executePickUpFunctions(ClaySoldierInventorySetter soldier, ItemStack source) {
        for (ClayPoiFunction<?> pickUpFunction : onPickUpFunction) {
            pickUpFunction.accept(soldier, ClayPoiSource.createSource(source));
        }
    }

    public Collection<RemovalCondition> getRemovalConditions() {
        return removalConditionType.values();
    }

    public @Nullable CustomEquipCapability getCustomEquipCapability(ItemStack stack) {
        var factory = ClaySoldiersCommon.CAPABILITY_MANGER.getCustomEquip(stack);
        if (factory != null) {
            return factory.apply(stack, this);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SoldierHoldableEffect) obj;
        return Objects.equals(this.properties, that.properties) &&
                Objects.equals(this.slots, that.slots) &&
                this.pickUpPriority == that.pickUpPriority &&
                Float.floatToIntBits(this.dropRate) == Float.floatToIntBits(that.dropRate) &&
                Objects.equals(this.predicate, that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties, slots, pickUpPriority, dropRate, predicate);
    }

    @Override
    public String toString() {
        return "SoldierHoldableEffect[" +
                "properties=" + properties + ", " +
                "slots=" + slots + ", " +
                "pickUpPriority=" + pickUpPriority + ", " +
                "dropRate=" + dropRate + ", " +
                "predicate=" + predicate + ']';
    }

    public static Builder of(SoldierPropertyMap.Builder properties) {
        return new Builder(properties.build());
    }

    public static class Builder {
        private final SoldierPropertyMap properties;
        private final List<ClayPoiFunction<?>> onPickUpFunction;

        private List<SoldierEquipmentSlot> slots = List.of();
        private int pickUpPriority = SoldierPickUpPriority.NORMAL.get();
        private float dropRate = DropRateProperty.NORMAL.get();
        private ClayPredicate<?> predicate = ClayPredicates.ConstantPredicate.getAlwaysTruePredicate();
        private final Map<RemovalConditionType<?>, RemovalCondition> removalConditionType = new HashMap<>();
        private int maxStackSize = 1;


        public Builder(SoldierPropertyMap properties) {
            this.properties = properties;
            this.onPickUpFunction = new ArrayList<>();
        }

        public Builder setSlots(List<SoldierEquipmentSlot> slots) {
            this.slots = slots;
            return this;
        }

        public Builder setSlot(SoldierEquipmentSlot slots) {
            this.slots = List.of(slots);
            return this;
        }

        public Builder setPickUpPriority(SoldierPickUpPriority pickUpPriority) {
            this.pickUpPriority = pickUpPriority.get();
            return this;
        }

        public Builder setPickUpPriority(int weight) {
            this.pickUpPriority = weight;
            return this;
        }
        public Builder setMaxStackSize(int size) {
            this.maxStackSize = size;
            return this;
        }
        public Builder setDropRate(float dropRate) {
            this.dropRate = dropRate;
            return this;
        }

        public Builder setDropRate(DropRateProperty dropRate) {
            this.dropRate = dropRate.get();
            return this;
        }

        public Builder setPredicate(ClayPredicate<?> predicate) {
            this.predicate = predicate;
            return this;
        }
        public Builder addPickUpEffect(ClayPoiFunction<?> effect) {
            this.onPickUpFunction.add(effect);
            return this;
        }

        public <T extends RemovalCondition> Builder removalCondition(RemovalConditionType<T> type, T context) {
            this.removalConditionType.put(type, context);
            return this;
        }

        public SoldierHoldableEffect build() {
            return new SoldierHoldableEffect(properties, slots, pickUpPriority, dropRate, maxStackSize, predicate, onPickUpFunction, removalConditionType);
        }
    }
}
