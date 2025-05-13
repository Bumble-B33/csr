package net.bumblebee.claysoldiers.claypoifunction;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.AdditionalSoldierData;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierLike;
import net.bumblebee.claysoldiers.util.EffectHolder;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ClayPoiFunctions {
    private static final Supplier<ClayPoiFunctionSerializer<EffectFunction>> EFFECT_FUNCTION_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayFunctionSerializer("apply_effect", () -> new ClayPoiFunctionSerializer<>(EffectFunction.CODEC.fieldOf("effect"), EffectFunction.STREAM_CODEC));
    private static final Supplier<ClayPoiFunctionSerializer<SetItem>> SET_ITEM_FUNCTION_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayFunctionSerializer("set_item", () -> new ClayPoiFunctionSerializer<>(SetItem.CODEC.fieldOf("set_item"), SetItem.STREAM_CODEC));
    private static final Supplier<ClayPoiFunctionSerializer<ConvertTo>> CONVERSION_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayFunctionSerializer("conversion", () -> new ClayPoiFunctionSerializer<>(ConvertTo.CODEC.fieldOf("conversion"), ConvertTo.STREAM_CODEC));
    private static final Supplier<ClayPoiFunctionSerializer<DyeSoldierFunction>> DYE_FUNCTION_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayFunctionSerializer("dye_soldier", () -> new ClayPoiFunctionSerializer<>(DyeSoldierFunction.CODEC.fieldOf("dye"), DyeSoldierFunction.STREAM_CODEC));
    private static final Supplier<ClayPoiFunctionSerializer<SelectRandom>> SELECT_RANDOM_FUNCTION_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerClayFunctionSerializer("select_random", () -> new ClayPoiFunctionSerializer<>(SelectRandom.CODEC.fieldOf("selection"), SelectRandom.STREAM_CODEC));

    private static final String COMPONENT_PREFIX = "clay_poi_function." + ClaySoldiersCommon.MOD_ID + ".";
    public static final String EFFECT_FUNCTION_ADD = COMPONENT_PREFIX + "effect_function_add";
    public static final String EFFECT_FUNCTION_REMOVE = COMPONENT_PREFIX + "effect_function_remove";
    public static final String EFFECT_FUNCTION_INCREASE = COMPONENT_PREFIX + "effect_function_increase";
    public static final String DYE_FUNCTION = COMPONENT_PREFIX + "dye_function";
    public static final String SET_ITEM_FUNCTION = COMPONENT_PREFIX + "set_item_function";
    public static final String SET_ITEM_FIND_FUNCTION = COMPONENT_PREFIX + "set_item_function_find";
    public static final String CONVERSION_FUNCTION = COMPONENT_PREFIX + "conversion";
    public static final String SELECT_RANDOM_FUNCTION = COMPONENT_PREFIX + "select_random";


    private ClayPoiFunctions() {
    }

    public static class EffectFunction extends ClayPoiFunction<EffectFunction> implements EffectHolder {
        public static final Codec<EffectFunction> CODEC = RecordCodecBuilder.create(in -> in.group(
                EffectOperation.CODEC.fieldOf("operation").forGetter(ef -> ef.operation)
        ).and(CodecUtils.addEffectAnd()).apply(in, EffectFunction::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EffectFunction> STREAM_CODEC = StreamCodec.composite(
                EffectOperation.STREAM_CODEC, t -> t.operation,
                ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), t -> t.effect,
                ByteBufCodecs.VAR_INT, t -> t.duration,
                ByteBufCodecs.VAR_INT, t -> t.amplifier,
                EffectFunction::new
        );

        private final EffectOperation operation;
        private final Holder<MobEffect> effect;
        private final int duration;
        private final int amplifier;

        private EffectFunction(EffectOperation operation, Holder<MobEffect> effect, int duration, int amplifier) {
            super(EFFECT_FUNCTION_SERIALIZER);
            this.operation = operation;
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public static EffectFunction addEffect(Holder<MobEffect> effect, int duration, int amplifier) {
            return new EffectFunction(EffectOperation.ADD, effect, duration, amplifier);
        }

        public static EffectFunction removeEffect(Holder<MobEffect> effect) {
            return new EffectFunction(EffectOperation.REMOVE, effect, 0, 0);
        }

        public static EffectFunction increaseEffect(Holder<MobEffect> effect, int duration, int amplifier) {
            return new EffectFunction(EffectOperation.INCREASE, effect, duration, amplifier);
        }

        @Override
        public void accept(ClaySoldierInventorySetter soldier, ClayPoiSource ignored) {
            operation.accept(soldier, new MobEffectInstance(effect, duration, amplifier, false, false));
        }

        @Override
        public int duration() {
            return duration;
        }

        @Override
        public int amplifier() {
            return amplifier;
        }

        @Override
        public Holder<MobEffect> effectHolder() {
            return effect;
        }

        @Override
        public @Nullable Component getDisplayName() {
            return switch (operation) {
                case ADD ->
                        Component.translatable(EFFECT_FUNCTION_ADD, effect.value().getDisplayName(), amplifier + 1, durationToString());
                case REMOVE -> Component.translatable(EFFECT_FUNCTION_REMOVE, effect.value().getDisplayName());
                case INCREASE ->
                        Component.translatable(EFFECT_FUNCTION_INCREASE, effect.value().getDisplayName(), amplifier, durationToString());
            };
        }

        private Component durationToString() {
            if (duration == MobEffectInstance.INFINITE_DURATION || duration > 9999) {
                return Component.literal("forever");
            }
            return Component.literal((duration / 20) + "s");
        }
    }

    private enum EffectOperation implements StringRepresentable {
        ADD("add", (soldier, effect) -> soldier.addMobEffect(effect, null)),
        INCREASE("increase", (soldier, effect) -> soldier.increaseEffect(effect, null)),
        REMOVE("remove", (soldier, effect) -> soldier.removeMobEffect(effect.getEffect()));

        public static final Codec<EffectOperation> CODEC = StringRepresentable.fromEnum(EffectOperation::values);
        public static final StreamCodec<FriendlyByteBuf, EffectOperation> STREAM_CODEC = CodecUtils.createEnumStreamCodec(EffectOperation.class);

        private final String serializedNamed;
        private final BiConsumer<ClaySoldierInventorySetter, MobEffectInstance> consumer;

        EffectOperation(String serializedNamed, BiConsumer<ClaySoldierInventorySetter, MobEffectInstance> consumer) {
            this.serializedNamed = serializedNamed;
            this.consumer = consumer;
        }

        public void accept(ClaySoldierInventorySetter soldier, MobEffectInstance effect) {
            consumer.accept(soldier, effect);
        }


        @Override
        public String getSerializedName() {
            return serializedNamed;
        }
    }

    public static class DyeSoldierFunction extends ClayPoiFunction<DyeSoldierFunction> {
        public static final Codec<DyeSoldierFunction> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.either(
                        ColorHelper.CODEC,
                        ColorGetterFunction.CODEC
                ).fieldOf("color").forGetter(DyeSoldierFunction::getEither),
                Codec.BOOL.optionalFieldOf("overwrite", false).forGetter(d -> d.overwrite)
        ).apply(in, DyeSoldierFunction::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DyeSoldierFunction> STREAM_CODEC = StreamCodec.composite(
                ColorHelper.STREAM_CODEC, s -> s.color,
                ColorGetterFunction.STREAM_CODEC, s -> s.colorGetter,
                ByteBufCodecs.BOOL, s -> s.overwrite,
                DyeSoldierFunction::new
        );

        private final ColorHelper color;
        private final ColorGetterFunction colorGetter;
        private final boolean overwrite;

        private DyeSoldierFunction(ColorHelper color, ColorGetterFunction colorGetter, boolean overwrite) {
            super(DYE_FUNCTION_SERIALIZER);
            this.color = color;
            this.colorGetter = colorGetter;
            this.overwrite = overwrite;
        }

        private DyeSoldierFunction(Either<ColorHelper, ColorGetterFunction> either, boolean overwrite) {
            super(DYE_FUNCTION_SERIALIZER);
            this.overwrite = overwrite;
            if (either.left().isPresent()) {
                this.color = either.left().get();
                this.colorGetter = ColorGetterFunction.NONE;
            } else {
                this.colorGetter = either.right().orElseThrow();
                this.color = ColorHelper.EMPTY;
            }

        }

        public DyeSoldierFunction(ColorGetterFunction colorGetter, boolean overwrite) {
            this(ColorHelper.EMPTY, colorGetter, overwrite);
        }

        public DyeSoldierFunction(int color, boolean overwrite) {
            this(ColorHelper.color(color), ColorGetterFunction.NONE, overwrite);
        }

        public DyeSoldierFunction(ColorHelper color, boolean overwrite) {
            this(color, ColorGetterFunction.NONE, overwrite);
        }


        @Override
        public void accept(ClaySoldierInventorySetter soldier, ClayPoiSource source) {
            ColorHelper currentColor = ColorHelper.color(colorGetter.getColor(source));
            if (currentColor.isEmpty()) {
                currentColor = this.color;
            }
            if (overwrite) {
                soldier.setOffsetColor(currentColor);
            } else {
                soldier.addOffsetColor(currentColor);
            }
        }

        private Either<ColorHelper, ColorGetterFunction> getEither() {
            if (colorGetter != ColorGetterFunction.NONE) {
                return Either.right(colorGetter);
            }
            return Either.left(color);
        }

        @Override
        public @Nullable List<Component> getDisplayNameDynamic(Player player) {
            if (colorGetter != ColorGetterFunction.NONE) {
                return List.of(Component.translatable(DYE_FUNCTION, colorGetter.getAnimatedDisplayName(player)));
            }
            return List.of(Component.translatable(DYE_FUNCTION, color.formatDynamic(player)));
        }
    }

    public static class SetItem extends ClayPoiFunction<SetItem> {
        public static final Codec<SetItem> CODEC = RecordCodecBuilder.create(in -> in.group(
                ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(s -> s.item),
                SoldierEquipmentSlot.CODEC.optionalFieldOf("slot").forGetter(SetItem::getSlot),
                SetItemOperation.CODEC.optionalFieldOf("operation", SetItemOperation.DROP).forGetter(s -> s.operation)
        ).apply(in, (item, slot, setItemOperation) -> new SetItem(item, slot.orElse(null), setItemOperation)));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetItem> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, s -> s.item,
                SoldierEquipmentSlot.OPTIONAL_STREAM_CODEC, s -> Optional.ofNullable(s.slot),
                SetItemOperation.STREAM_CODEC, s -> s.operation,
                (stack, slot, op) -> new SetItem(stack, slot.orElse(null), op)
        );

        private final ItemStack item;
        @Nullable
        private final SoldierEquipmentSlot slot;
        private final SetItemOperation operation;

        private SetItem(ItemStack item, @Nullable SoldierEquipmentSlot slot, SetItemOperation operation) {
            super(SET_ITEM_FUNCTION_SERIALIZER);
            this.item = item;
            this.slot = slot;
            this.operation = operation;
        }

        private Optional<SoldierEquipmentSlot> getSlot() {
            return Optional.ofNullable(slot);
        }

        public static SetItem replace(ItemStack item, @Nullable SoldierEquipmentSlot slot) {
            return new SetItem(item, slot, SetItemOperation.REPLACE);
        }

        public static SetItem drop(ItemStack item, @Nullable SoldierEquipmentSlot slot) {
            return new SetItem(item, slot, SetItemOperation.DROP);
        }

        @Override
        public @Nullable Component getDisplayName() {
            if (slot == null) {
                return Component.translatable(SET_ITEM_FIND_FUNCTION, item.getDisplayName());
            }
            return Component.translatable(SET_ITEM_FUNCTION, item.getDisplayName(), slot.getDisplayName());
        }

        @Override
        public void accept(ClaySoldierInventorySetter soldier, ClayPoiSource ignored) {
            if (slot == null) {
                var effect = ClaySoldiersCommon.DATA_MAP.getEffect(item);
                if (effect == null) {
                    ClaySoldiersCommon.LOGGER.error("Set Item in suitable Slot is only supported for items that can be held by soldiers. {} cannot be held.", item.getItem());
                    return;
                }
                for (var possibleSlot : effect.slots()) {
                    if (soldier.setSlotIfEmpty(possibleSlot, item)) {
                        if (operation == SetItemOperation.DROP) {
                            soldier.dropItemSlotWithChance(possibleSlot);

                        }
                        return;
                    }
                }
                return;
            }

            if (operation == SetItemOperation.DROP) {
                soldier.dropItemSlotWithChance(slot);
            }

            soldier.setItemSlot(slot, item);
        }
    }

    private enum SetItemOperation implements StringRepresentable {
        REPLACE("replace"),
        DROP("drop");

        private static final Codec<SetItemOperation> CODEC = StringRepresentable.fromEnum(SetItemOperation::values);
        private static final StreamCodec<FriendlyByteBuf, SetItemOperation> STREAM_CODEC = CodecUtils.createEnumStreamCodec(SetItemOperation.class);

        private final String serializedName;

        SetItemOperation(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }


    }

    public static class ConvertTo extends ClayPoiFunction<ConvertTo> {
        private static final Codec<ConvertTo> CODEC = AdditionalSoldierData.CODEC.xmap(d -> new ConvertTo(d.soldierType(), d.tag()), c -> c.data);
        private static final StreamCodec<RegistryFriendlyByteBuf, ConvertTo> STREAM_CODEC = AdditionalSoldierData.STREAM_CODEC.map(d -> new ConvertTo(d.soldierType(), d.tag()), c -> c.data);
        private final AdditionalSoldierData data;

        public <T extends ClayMobEntity & ClaySoldierLike> ConvertTo(EntityType<T> soldier, CompoundTag tag) {
            super(CONVERSION_SERIALIZER);
            this.data = new AdditionalSoldierData(soldier, tag);
        }

        @Override
        public void accept(ClaySoldierInventorySetter soldier, ClayPoiSource source) {
            if (soldier instanceof AbstractClaySoldierEntity entity) {
                data.convert(entity);
            }
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(CONVERSION_FUNCTION, data.soldierType().getDescription());
        }

        @Override
        public String toString() {
            return "%s(%s)".formatted(super.toString(), data);
        }
    }

    public static class SelectRandom extends ClayPoiFunction<SelectRandom> {
        private static final StreamCodec<RegistryFriendlyByteBuf, Map<ClayPoiFunction<?>, Float>> MAP_STREAM_CODEC =
                ByteBufCodecs.map(HashMap::new, ClayPoiFunction.STREAM_CODEC, ByteBufCodecs.FLOAT);
        private static final Codec<SelectRandom> CODEC = ClayPoiFunction.CODEC.listOf().xmap(SelectRandom::new, s -> s.chanceList);

        private static final StreamCodec<RegistryFriendlyByteBuf, SelectRandom> STREAM_CODEC = StreamCodec.composite(
                ClayPoiFunction.LIST_STREAM_CODEC, s -> s.chanceList, SelectRandom::new
        );

        private final List<ClayPoiFunction<?>> chanceList;

        private SelectRandom(List<ClayPoiFunction<?>> functions) {
            super(SELECT_RANDOM_FUNCTION_SERIALIZER);
            this.chanceList = functions;
            if (chanceList.isEmpty()) {
                throw new IllegalStateException("Chance List cannot be empty");
            }
        }


        private void forEach(Consumer<ClayPoiFunction<?>> apply) {
            chanceList.forEach(apply);
        }

        public static SelectRandom allEqual(ClayPoiFunction<?>... functions) {
            return new SelectRandom(List.of(functions));
        }

        @Override
        public void accept(ClaySoldierInventorySetter soldier, ClayPoiSource source) {
            Util.getRandom(chanceList, soldier.getClaySoldierRandom()).accept(soldier, source);
        }

        @Override
        public @Nullable List<Component> getDisplayNameDynamic(Player player) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable(SELECT_RANDOM_FUNCTION));
            forEach(func -> {
                func.getDisplayNameDynamic(player).forEach(component -> {
                    list.add(CommonComponents.space().append(component));
                });
            });
            return list;
        }
    }

    public static void init() {
    }
}
