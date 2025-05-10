package net.bumblebee.claysoldiers.claysoldierpredicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.EffectHolder;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClayPredicates {
    public static final Supplier<ClayPredicateSerializer<ItemPredicate>> ITEM_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("has_item", () -> new ClayPredicateSerializer<>(ItemPredicate.CODEC, ItemPredicate.STREAM_CODEC));
    public static final Supplier<ClayPredicateSerializer<LogicPredicate>> LOGIC_PREDICATE_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("logic", () -> new ClayPredicateSerializer<>(LogicPredicate.CODEC, LogicPredicate.STREAM_CODEC));
    public static final Supplier<ClayPredicateSerializer<SoldierPropertyPredicate>> SOLDIER_HOLDABLE_PROPERTY_PREDICATE = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("holdable_property", () -> new ClayPredicateSerializer<>(SoldierPropertyPredicate.CODEC, SoldierPropertyPredicate.STREAM_CODEC));
    public static final Supplier<ClayPredicateSerializer<EffectPredicate>> EFFECT_PREDICATE = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("has_effect", () -> new ClayPredicateSerializer<>(EffectPredicate.CODEC, EffectPredicate.STREAM_CODEC));

    private static final String COMPONENT_PREFIX = "clay_soldier_predicate." + ClaySoldiersCommon.MOD_ID + ".";

    public static final String ITEM_PREDICATE_COMPONENT = COMPONENT_PREFIX + "item_predicate";
    public static final String ITEM_PREDICATE_ANY_COMPONENT = COMPONENT_PREFIX + "item_predicate_any";
    public static final String SOLDIER_PROPERTY_PREDICATE_COMPONENT = COMPONENT_PREFIX + "soldier_property_predicate";
    public static final String EFFECT_PREDICATE_COMPONENT = COMPONENT_PREFIX + "effect_predicate";
    public static final String EFFECT_PREDICATE_DURATION_COMPONENT = COMPONENT_PREFIX + "effect_predicate.duration";
    public static final String EFFECT_PREDICATE_AMPLIFIER_COMPONENT = COMPONENT_PREFIX + "effect_predicate.amplifier";
    public static final String EFFECT_PREDICATE_DURATION_AMPLIFIER_COMPONENT = COMPONENT_PREFIX + "effect_predicate.duration_amplifier";
    public static final String HAS_CUSTOM_COLOR_COMPONENT = COMPONENT_PREFIX + "custom_color_predicate";

    public static final Supplier<ClayPredicateSerializer<ConstantPredicate>> ALWAYS_TRUE_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("always_true", () -> new ClayPredicateSerializer<>(ConstantPredicate.ALWAYS_TRUE_CODEC));
    public static final Supplier<ClayPredicateSerializer<ConstantPredicate>> HAS_CUSTOM_COLOR_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayPredicateSerializer("has_custom_color", () -> new ClayPredicateSerializer<>(ConstantPredicate.HAS_CUSTOM_COLOR_CODEC));

    private ClayPredicates() {
    }

    public static class ConstantPredicate extends ClayPredicate<ConstantPredicate> {
        private static final Codec<ConstantPredicate> ALWAYS_TRUE_CODEC = Codec.unit(ConstantPredicate::getAlwaysTruePredicate);
        private static final Codec<ConstantPredicate> HAS_CUSTOM_COLOR_CODEC = Codec.unit(ConstantPredicate::getHasCustomColor);

        private static ConstantPredicate ALWAYS_TRUE_INSTANCE = null;
        private static ConstantPredicate CUSTOM_COLOR_INSTANCE = null;

        private final Predicate<ClaySoldierInventoryQuery> value;
        @Nullable
        private final String translatableKey;

        public static ConstantPredicate getAlwaysTruePredicate() {
            if (ALWAYS_TRUE_INSTANCE == null) {
                ALWAYS_TRUE_INSTANCE = new ConstantPredicate(ALWAYS_TRUE_SERIALIZER, (s) -> true, null);
            }
            return ALWAYS_TRUE_INSTANCE;
        }
        public static ConstantPredicate getHasCustomColor() {
            if (CUSTOM_COLOR_INSTANCE == null) {
                CUSTOM_COLOR_INSTANCE = new ConstantPredicate(HAS_CUSTOM_COLOR_SERIALIZER, ClaySoldierInventoryQuery::hasOffsetColor, HAS_CUSTOM_COLOR_COMPONENT);
            }
            return CUSTOM_COLOR_INSTANCE;
        }

        private ConstantPredicate(Supplier<ClayPredicateSerializer<ConstantPredicate>> serializer, Predicate<ClaySoldierInventoryQuery> value, @Nullable String displayName) {
            super(serializer);
            this.value = value;
            this.translatableKey = displayName;
        }

        @Nullable
        @Override
        public Component getDisplayName() {
            return translatableKey == null ? null : Component.translatable(translatableKey);
        }

        @Override
        public ClayPredicatePriority getPriority() {
            return ClayPredicatePriority.HIGH;
        }

        @Override
        public boolean test(ClaySoldierInventoryQuery soldier) {
            return value.test(soldier);
        }

        @Override
        public String toString() {
            return "ConstantPredicate: " + (translatableKey == null ? "AlwaysTrue" : "Custom Color");
        }
    }

    public static class ItemPredicate extends ClayPredicate<ItemPredicate> {
        private static final Codec<Either<ItemPredicateSlot, SoldierEquipmentSlot>> CODEC_SLOT = Codec.either(
                ItemPredicateSlot.CODEC,
                SoldierEquipmentSlot.CODEC
        );
        public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(in -> in.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemPredicate::getItem),
                CODEC_SLOT.fieldOf("slot").forGetter(ItemPredicate::getSlot)
        ).apply(in, ItemPredicate::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemPredicate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), ItemPredicate::getItem,
                ByteBufCodecs.either(ItemPredicateSlot.STREAM_CODEC, SoldierEquipmentSlot.STREAM_CODEC), ItemPredicate::getSlot,
                ItemPredicate::new
        );

        private final Item item;
        private final SoldierEquipmentSlot slot;
        @Nullable
        private final ItemPredicateSlot specialSlot;

        private ItemPredicate(Item item, Either<ItemPredicateSlot, SoldierEquipmentSlot> either) {
            super(ITEM_SERIALIZER);
            this.item = item;
            if (either.left().isPresent()) {
                specialSlot = either.left().get();
                slot = null;
            } else {
                this.slot = either.right().orElseThrow();
                this.specialSlot = null;
            }
        }

        public ItemPredicate(Item item, SoldierEquipmentSlot slot) {
            this(item, Either.right(slot));
        }
        public static ItemPredicate any(Item item) {
            return new ItemPredicate(item, Either.left(ItemPredicateSlot.ANY_SLOT));
        }
        public static ItemPredicate suitable(Item item) {
            return new ItemPredicate(item, Either.left(ItemPredicateSlot.SUITABLE));
        }

        private Either<ItemPredicateSlot, SoldierEquipmentSlot> getSlot() {
            return slot != null ? Either.right(slot) : Either.left(specialSlot);
        }

        public Item getItem() {
            return this.item;
        }

        @Override
        public ClayPredicatePriority getPriority() {
            return ClayPredicatePriority.HIGH;
        }

        @Override
        public @Nullable Component getDisplayName() {
            if (slot == null) {
                assert specialSlot != null;
                return Component.translatable(ITEM_PREDICATE_ANY_COMPONENT, item.getDescription(), specialSlot.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY);
            }
            return Component.translatable(ITEM_PREDICATE_COMPONENT, item.getDescription(), slot.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY);
        }

        @Override
        public boolean test(ClaySoldierInventoryQuery soldier) {
            if (slot != null) {
                return soldier.getItemBySlot(slot).stack().is(item);
            }

            if (specialSlot == ItemPredicateSlot.ANY_SLOT) {
                for (ItemStack stack : soldier.getAllSlots()) {
                    if (stack.is(item)) {
                        return true;
                    }
                }
                return false;
            }
            var effect = ClaySoldiersCommon.DATA_MAP.getEffect(item);
            if (effect != null) {
                for (var possibleSlot : effect.slots()) {
                    if (soldier.getItemBySlot(possibleSlot).stack().isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    public enum ItemPredicateSlot implements StringRepresentable, KeyableTranslatableProperty {
        ANY_SLOT("any"),
        SUITABLE("suitable");

        private static final String ITEM_PREDICATE_SPECIAL = COMPONENT_PREFIX + "item_predicate_special.any.";

        private static final Codec<ItemPredicateSlot> CODEC = StringRepresentable.fromEnum(ItemPredicateSlot::values);
        private static final StreamCodec<FriendlyByteBuf, ItemPredicateSlot> STREAM_CODEC = CodecUtils.createEnumStreamCodec(ItemPredicateSlot.class);

        private final String serializedName;

        ItemPredicateSlot(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String translatableKey() {
            return ITEM_PREDICATE_SPECIAL + serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    public static class SoldierPropertyPredicate extends ClayPredicate<SoldierPropertyPredicate> {
        public static final Codec<SoldierPropertyPredicate> CODEC = RecordCodecBuilder.create(in -> in.group(
                PropertyTestType.CODEC.optionalFieldOf("test_type", PropertyTestType.EXIST).forGetter(p -> p.testType),
                SoldierPropertyTypes.CODEC.fieldOf("property").forGetter(p -> p.propertyIdentifier),
                Codec.INT.optionalFieldOf("value", -1).forGetter(p -> p.countNeeded)
        ).apply(in, SoldierPropertyPredicate::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyPredicate> STREAM_CODEC = StreamCodec.composite(
                PropertyTestType.STREAM_CODEC, s -> s.testType,
                SoldierPropertyTypes.STREAM_CODEC, s -> s.propertyIdentifier,
                ByteBufCodecs.VAR_INT, s -> s.countNeeded,
                SoldierPropertyPredicate::new
        );

        private final SoldierPropertyType<?> propertyIdentifier;
        private final PropertyTestType testType;
        private final int countNeeded;

        private SoldierPropertyPredicate(PropertyTestType testType, SoldierPropertyType<?> propertyIdentifier, int count) {
            super(SOLDIER_HOLDABLE_PROPERTY_PREDICATE);
            this.propertyIdentifier = propertyIdentifier;
            this.testType = testType;
            this.countNeeded = count;
        }

        public SoldierPropertyPredicate(PropertyTestType testType, SoldierPropertyType<?> propertyIdentifier) {
            this(testType, propertyIdentifier, -1);
        }

        public static SoldierPropertyPredicate getCount(SoldierPropertyType<Collection<?>> propertyIdentifier, int count) {
            return new SoldierPropertyPredicate(PropertyTestType.COUNT, propertyIdentifier, count);
        }

        public static SoldierPropertyPredicate isExactly(SoldierPropertyType<?> propertyIdentifier, int toBe) {
            return new SoldierPropertyPredicate(PropertyTestType.IS_EXACTLY, propertyIdentifier, toBe);
        }

        @Override
        public ClayPredicatePriority getPriority() {
            return ClayPredicatePriority.LOW;
        }

        @Override
        public boolean test(ClaySoldierInventoryQuery soldier) {
            int value = soldier.allProperties().getPropertyValueAsInt(propertyIdentifier);
            if (propertyIdentifier == SoldierPropertyTypes.ATTACK_TYPE.get() && value == AttackTypeProperty.NORMAL.ordinal()) {
                value = soldier.getAttackType().ordinal();
            }
            return testType.test(value, countNeeded);
        }

        @Override
        public Component getDisplayName() {
            return testType.getDisplayName(propertyIdentifier.getDisplayName(), countNeeded);
        }

    }

    public enum PropertyTestType implements StringRepresentable {
        INCREASE("increase", i -> i > 0),
        DECREASE("decrease", i -> i < 0),
        EXIST("exist", i -> i != 0),
        IS_EXACTLY("exactly", (n, c) -> n == c),
        COUNT("count", (n, c) -> n >= c);

        public static final Codec<PropertyTestType> CODEC = StringRepresentable.fromEnum(PropertyTestType::values);
        private static final StreamCodec<FriendlyByteBuf, PropertyTestType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(PropertyTestType.class);

        private final String serializedName;
        private final BiIntPredicate predicate;

        PropertyTestType(String serializedName, BiIntPredicate predicate) {
            this.serializedName = serializedName;
            this.predicate = predicate;
        }

        PropertyTestType(String serializedName, IntPredicate predicate) {
            this(serializedName, (number, ignored) -> predicate.test(number));
        }

        public Component getDisplayName(Component propertyName, int count) {
            return Component.translatable(getDescriptionId(), propertyName, "" + count).withStyle(ChatFormatting.DARK_GRAY);
        }

        public String getDescriptionId() {
            return COMPONENT_PREFIX + ".property_test_type." + serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public boolean test(int number, int shouldBe) {
            return predicate.test(number, shouldBe);
        }

        @FunctionalInterface
        private interface BiIntPredicate {
            boolean test(int number, int shouldBe);
        }
    }

    public static class EffectPredicate extends ClayPredicate<EffectPredicate> implements EffectHolder {
        public static final Codec<EffectPredicate> CODEC = EffectHolder.getCodec(EffectPredicate::fromEffectHolder, "min");
        private static final StreamCodec<RegistryFriendlyByteBuf, EffectPredicate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), EffectPredicate::effectHolder,
                ByteBufCodecs.VAR_INT, EffectPredicate::duration,
                ByteBufCodecs.VAR_INT, EffectPredicate::amplifier,
                EffectPredicate::new
        );
        private final Holder<MobEffect> effect;
        private final int minDuration;
        private final int minAmplifier;

        public EffectPredicate(Holder<MobEffect> effect, int minDuration, int minAmplifier) {
            super(EFFECT_PREDICATE);
            this.effect = effect;
            this.minDuration = minDuration;
            this.minAmplifier = minAmplifier;
        }

        public EffectPredicate(Holder<MobEffect> effect) {
            this(effect, 0, 0);
        }

        @Override
        public ClayPredicatePriority getPriority() {
            return ClayPredicatePriority.HIGH;
        }

        @Override
        public boolean test(ClaySoldierInventoryQuery soldier) {
            var mobEffect = soldier.getEffect(effect);
            return mobEffect != null && mobEffect.getAmplifier() >= minAmplifier && mobEffect.getDuration() >= minDuration;

        }

        @Override
        public MobEffect effect() {
            return effect.value();
        }

        @Override
        public int duration() {
            return minDuration;
        }

        @Override
        public int amplifier() {
            return minAmplifier;
        }

        @Override
        public Holder<MobEffect> effectHolder() {
            return effect;
        }

        private static EffectPredicate fromEffectHolder(EffectHolder effectHolder) {
            return new EffectPredicate(effectHolder.effectHolder(), effectHolder.amplifier(), effectHolder.duration());
        }

        @Override
        public Component getDisplayName() {
            if (minDuration > 0 && minAmplifier > 0) {
                return Component.translatable(EFFECT_PREDICATE_DURATION_AMPLIFIER_COMPONENT, effect().getDisplayName(), minDuration*20, minAmplifier);
            }
            if (minAmplifier > 0) {
                return Component.translatable(EFFECT_PREDICATE_AMPLIFIER_COMPONENT, effect().getDisplayName(), minAmplifier);
            }
            if (minDuration > 0) {
                return Component.translatable(EFFECT_PREDICATE_DURATION_COMPONENT, effect().getDisplayName(), minDuration*20);
            }
            return Component.translatable(EFFECT_PREDICATE_COMPONENT, effect().getDisplayName());
        }
    }

    public static class LogicPredicate extends ClayPredicate<LogicPredicate> {
        public static final Codec<LogicPredicate> CODEC = RecordCodecBuilder.create(in -> in.group(
                LogicComparator.CODEC.fieldOf("operation").forGetter(LogicPredicate::getComparator),
                CodecUtils.singularOrPluralCodecOptional(ClayPredicate.CODEC, "predicate").forGetter(LogicPredicate::getList)
        ).apply(in, LogicPredicate::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, LogicPredicate> STREAM_CODEC = StreamCodec.composite(
                LogicComparator.STREAM_CODEC, LogicPredicate::getComparator,
                ClayPredicate.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), s -> s.list,
                LogicPredicate::new
        );


        private final Set<ClayPredicate<?>> list;
        private final LogicComparator comparator;

        private LogicPredicate(LogicComparator comparator, Set<ClayPredicate<?>> list) {
            super(LOGIC_PREDICATE_SERIALIZER);
            this.list = list;
            this.comparator = comparator;
        }

        private Set<ClayPredicate<?>> getList() {
            return list;
        }

        private LogicComparator getComparator() {
            return comparator;
        }


        public static LogicPredicate any(Set<ClayPredicate<?>> predicates) {
            return new LogicPredicate(LogicComparator.ANY, predicates);
        }

        public static LogicPredicate all(Set<ClayPredicate<?>> predicates) {
            return new LogicPredicate(LogicComparator.ALL, predicates);
        }

        public static LogicPredicate none(Set<ClayPredicate<?>> predicates) {
            return new LogicPredicate(LogicComparator.NONE, predicates);
        }

        public static LogicPredicate not(ClayPredicate<?> predicates) {
            return new LogicPredicate(LogicComparator.NOT, Set.of(predicates));
        }

        @Override
        public ClayPredicatePriority getPriority() {
            return ClayPredicatePriority.MULTI;
        }

        @Override
        public boolean test(ClaySoldierInventoryQuery soldier) {
            return comparator.test(list.stream().map(p -> p.test(soldier)).toList());
        }


        @Override
        public @Nullable Component getDisplayName() {
            var mutableComponent = comparator.getDisplayName().copy().withStyle(ChatFormatting.DARK_GRAY);
            var components = list.stream().map(ClayPredicate::getDisplayName).filter(Objects::nonNull).toList();

            for (int i = 0; i < components.size(); i++) {
                if (i == 0) {
                    mutableComponent.append(" ");
                } else {
                    mutableComponent.append(", ");

                }
                mutableComponent.append(components.get(i));
            }

            return components.isEmpty() ? null : mutableComponent;
        }
        @Override
        public String toString() {
            return "LogicPredicate(%s: %s)".formatted(comparator.serializedName, list);
        }
    }

    public enum LogicComparator implements StringRepresentable {
        ANY("any", list -> list.contains(true)),
        ALL("all", list -> !list.contains(false)),
        NONE("none", list -> !list.contains(true)),
        NOT("not", list -> !list.getFirst());

        public static final StringRepresentableCodec<LogicComparator> CODEC = StringRepresentable.fromEnum(LogicComparator::values);
        private static final StreamCodec<FriendlyByteBuf, LogicComparator> STREAM_CODEC = CodecUtils.createEnumStreamCodec(LogicComparator.class);

        private final String serializedName;
        private final Predicate<List<Boolean>> predicate;

        LogicComparator(String name, Predicate<List<Boolean>> predicate) {
            this.serializedName = name;
            this.predicate = predicate;
        }

        public Component getDisplayName() {
            return Component.translatable(COMPONENT_PREFIX + "logic_comparator." + serializedName);
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return serializedName;
        }

        public boolean test(List<Boolean> list) {
            return predicate.test(list);
        }


    }

    public static void init() {
    }
}