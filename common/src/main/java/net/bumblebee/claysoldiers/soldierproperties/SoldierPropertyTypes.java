package net.bumblebee.claysoldiers.soldierproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.*;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.types.AttributePropertyType;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.types.UnitPropertyType;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class SoldierPropertyTypes {
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
    public static final ToIntFunction<Float> FLOAT_AS_INT = value -> (int) Math.signum(value);
    public static final ToIntFunction<Integer> INT_AS_INT = value -> value;
    public static final ValueCombiner<Float> FLOAT_COMBINER = Float::sum;
    public static final ValueCombiner<Float> SIZE_COMBINER = (f1, f2) -> f1 + f2 - 1;
    public static final ValueCombiner<Integer> INT_COMBINER = Integer::sum;

    public static final Supplier<SoldierPropertyType<Float>> DAMAGE = createFloat("damage", Codec.FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<SoldierPropertyType<Float>> PROTECTION = createFloat("protection", Codec.FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<SoldierPropertyType<Integer>> SET_ON_FIRE = create("set_on_fire", CodecUtils.TIME_CODEC, ByteBufCodecs.VAR_INT.cast(), 0, INT_AS_INT, INT_COMBINER, defaultedDisplaysNameValue(v -> Component.literal(v + "s")));
    public static final Supplier<SoldierPropertyType<RangedAttackType>> THROWABLE = create("throwable", RangedAttackType.CODEC, RangedAttackType.STREAM_CODEC.cast(), RangedAttackType.NONE, RangedAttackType.TO_INT, RangedAttackType.COMBINER, defaultedDisplaysName());
    public static final Supplier<UnitPropertyType> SEE_INVISIBILITY = create("see_invisibility", new UnitPropertyType());
    public static final Supplier<UnitPropertyType> CAN_SWIM = create("can_swim", new UnitPropertyType());
    public static final Supplier<BreathHoldPropertyType> BREATH_HOLD = create("breath_hold", new BreathHoldPropertyType());
    public static final Supplier<AttributePropertyType> ATTRIBUTES = create("attributes", new AttributePropertyType());
    public static final Supplier<SoldierPropertyType<List<DeathCloudProperty>>> DEATH_CLOUD = create("death_cloud", DeathCloudProperty.LIST_CODEC, DeathCloudProperty.LIST_STREAM_CODEC, DeathCloudProperty.EMPTY, DeathCloudProperty.TO_INT, DeathCloudProperty.COMBINER);
    public static final Supplier<SoldierPropertyType<Float>> DEATH_EXPLOSION = createFloat("death_exploder", ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<SoldierPropertyType<Float>> SIZE = createFloat("size", ExtraCodecs.POSITIVE_FLOAT, ByteBufCodecs.FLOAT.cast(), 1f, FLOAT_AS_INT, SIZE_COMBINER);
    public static final Supplier<UnitPropertyType> INVISIBLE = create("invisible", new UnitPropertyType());
    public static final Supplier<UnitPropertyType> GLOW_OUTLINE = create("glow_outline", new UnitPropertyType());
    public static final Supplier<UnitPropertyType> GLOW_IN_THE_DARK = create("glowing", new UnitPropertyType());
    public static final Supplier<SoldierPropertyType<AttackTypeProperty>> ATTACK_TYPE = create("attack_type", AttackTypeProperty.CODEC, AttackTypeProperty.STREAM_CODEC.cast(), AttackTypeProperty.NORMAL, AttackTypeProperty.TO_INT, AttackTypeProperty.COMBINER);
    public static final Supplier<SoldierPropertyType<Float>> HEAVY = createFloat("heavy", Codec.FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<SoldierPropertyType<List<SpecialAttack<?>>>> SPECIAL_ATTACK = create("special_attack", SpecialAttack.LIST_CODEC, SpecialAttack.STREAM_CODEC.apply(ByteBufCodecs.list()), List.of(), SpecialAttack.TO_INT, SpecialAttack.COMBINER);
    public static final Supplier<SoldierPropertyType<List<SpecialAttack<?>>>> COUNTER_ATTACK = create("counter_attack", SpecialAttack.LIST_CODEC, SpecialAttack.STREAM_CODEC.apply(ByteBufCodecs.list()), List.of(), SpecialAttack.TO_INT, SpecialAttack.COMBINER);
    public static final Supplier<SoldierPropertyType<DamageBlock>> DAMAGE_BLOCK = create("damage_block", DamageBlock.CODEC, DamageBlock.STREAM_CODEC.cast(), DamageBlock.EMPTY, DamageBlock.TO_INT, DamageBlock.COMBINER, DamageBlock.DISPLAY_NAME_CREATOR);
    public static final Supplier<SoldierPropertyType<ReviveProperty>> REVIVE_PROPERTY = create("revive_other", ReviveProperty.CODEC, ReviveProperty.STREAM_CODEC, ReviveProperty.EMPTY, ReviveProperty.TO_INT, ReviveProperty.COMBINER);
    public static final Supplier<SoldierPropertyType<EffectImmunityMap>> IMMUNITY = create("immunity", EffectImmunityMap.CODEC, EffectImmunityMap.STREAM_CODEC, EffectImmunityMap.EMPTY, EffectImmunityMap.TO_INT, EffectImmunityMap.COMBINER);
    public static final Supplier<SoldierPropertyType<WraithProperty>> WRAITH = create("wraith", WraithProperty.CODEC, WraithProperty.STREAM_CODEC, WraithProperty.EMPTY, WraithProperty.TO_INT, WraithProperty.COMBINER);
    public static final Supplier<SoldierPropertyType<Float>> ATTACK_RANGE = createFloat("attack_range", Codec.FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<UnitPropertyType> CAN_GLIDE = create("glide", new UnitPropertyType());
    public static final Supplier<UnitPropertyType> TELEPORTATION = create("teleportation", new UnitPropertyType());
    public static final Supplier<SoldierPropertyType<Float>> EXPLOSION_RESISTANCE = createFloat("explosion_resistance",  Codec.FLOAT, ByteBufCodecs.FLOAT.cast(), 0f, FLOAT_AS_INT, FLOAT_COMBINER);
    public static final Supplier<UnitPropertyType> TELEPORT_TO_OWNER = create("teleport_to_owner", new UnitPropertyType());
    public static final Supplier<SoldierPropertyType<IEvacuationProperty>> EVACUATION = create("evacuation", IEvacuationProperty.CODEC, IEvacuationProperty.STREAM_CODEC.cast(), IEvacuationProperty.NONE, IEvacuationProperty::toInt, IEvacuationProperty.COMBINER, IEvacuationProperty.DISPLAY_NAME_GETTER);
    public static final Supplier<UnitPropertyType> BOUNCE = create("bounce", new UnitPropertyType());


    public static final Codec<SoldierPropertyType<?>> CODEC = CodecUtils.byNameCodecWithDefaultModId(ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyType<?>> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.SOLDIER_PROPERTY_TYPES);

    private static Supplier<SoldierPropertyType<Float>> createFloat(String name, Codec<Float> valueCodec, StreamCodec<RegistryFriendlyByteBuf, Float> streamCodec, float defaultValue, ToIntFunction<Float> toIntFunction, ValueCombiner<Float> combiner){
        return create(name, valueCodec, streamCodec, defaultValue, toIntFunction, combiner, floatRoundedDisplaysNameValue());
    }
    private static <V> Supplier<SoldierPropertyType<V>> create(String name, Codec<V> valueCodec, StreamCodec<RegistryFriendlyByteBuf, V> streamCodec, V defaultValue, ToIntFunction<V> toIntFunction, ValueCombiner<V> combiner){
        return create(name, valueCodec, streamCodec, defaultValue, toIntFunction, combiner, null);
    }
    private static <V> Supplier<SoldierPropertyType<V>> create(String name, Codec<V> valueCodec, StreamCodec<RegistryFriendlyByteBuf, V> streamCodec, V defaultValue, ToIntFunction<V> toIntFunction, ValueCombiner<V> combiner, BiFunction<String, V, List<Component>> customDisplayName){
        return create(name, new SoldierPropertyType<>(valueCodec, streamCodec, defaultValue, toIntFunction, combiner, customDisplayName));
    }
    private static <T extends SoldierPropertyType<?>> Supplier<T> create(String name, T type){
        return ClaySoldiersCommon.PLATFORM.registerSoldierProperty(name, () -> type);
    }

    private static <V> BiFunction<String, V, List<Component>> defaultedDisplaysNameValue(Function<V, Component> valueDisplayName) {
        return (key, value) -> List.of(Component.translatable(key).append(": ").append(valueDisplayName.apply(value)));
    }
    private static <V> BiFunction<String, V, List<Component>> defaultedDisplaysName() {
        return (key, value) -> List.of(Component.translatable(key));
    }
    private static BiFunction<String, Float, List<Component>> floatRoundedDisplaysNameValue() {
        return defaultedDisplaysNameValue(f -> Component.literal(FLOAT_FORMAT.format(f)));
    }


    public static void init() {
    }

    private SoldierPropertyTypes() {
    }
}
