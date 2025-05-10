package net.bumblebee.claysoldiers.soldierproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class SoldierPropertyType<T> implements ToIntFunction<T>, ValueCombiner<T> {
    private final Codec<T> valueCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
    private final T defaultValue;
    private final ToIntFunction<T> toIntFunction;
    private final ValueCombiner<T> combiner;
    private Holder.Reference<SoldierPropertyType<?>> holder;
    @Nullable
    private String descriptionId;
    @Nullable
    private final BiFunction<String, T, List<Component>> customDisplayName;

    protected SoldierPropertyType(Codec<T> valueCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, T defaultValue, ToIntFunction<T> toIntFunction, ValueCombiner<T> combiner, @Nullable BiFunction<String, T, List<Component>> customDisplayName) {
        this.valueCodec = valueCodec;
        this.streamCodec = streamCodec;
        this.defaultValue = defaultValue;
        this.toIntFunction = toIntFunction;
        this.combiner = combiner;
        this.customDisplayName = customDisplayName;
    }

    /**
     * Returns the {@code Codec} of the value.
     */
    public Codec<T> getValueCodec() {
        return valueCodec;
    }

    /**
     * Encodes this {@code SoldierPropertyType} with the given Value to the given {@code ByteBuff}.
     */
    public void streamEncode(RegistryFriendlyByteBuf byteBuff, T value) {
        streamCodec.encode(byteBuff, value);
    }

    /**
     * Decodes the Value associated {@code SoldierPropertyType} on the given {@code ByteBuff}.
     */
    public T streamDecode(RegistryFriendlyByteBuf byteBuff) {
        return streamCodec.decode(byteBuff);
    }

    /**
     * Returns the default value of this type.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Converts the value to an integer.
     * If the property has not been changed from its default value it returns 0.
     * If the property has been increased it returns a positive value.
     * If the property has been decreased it returns a negative value.
     * Collection should return their size.
     */
    @Override
    public int applyAsInt(T value) {
        return toIntFunction.applyAsInt(value);
    }

    @Override
    public T combine(T first, T second) {
        return combiner.combine(first, second);
    }

    public String getDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId(ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY, getOrCreateReference().key().location());
        }
        return descriptionId;
    }

    public SoldierProperty<T> createProperty(T value) {
        return new SoldierProperty<>(this, value);
    }

    public Component getDisplayName() {
        return Component.translatable(getDescriptionId());
    }

    /**
     * Returns the Display Name of this property with the given value.
     * An empty List indicates this property is not active.
     */
    public List<Component> getDisplayNameWithValue(T value, @Nullable ClaySoldierInventoryQuery soldier) {
        if (customDisplayName == null) {
            return List.of(Component.translatable(getDescriptionId()).append(": " + value));
        }
        return customDisplayName.apply(getDescriptionId(), value);
    }

    @Override
    public String toString() {
        return ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY.getKey(this).getPath();
    }

    public boolean is(TagKey<SoldierPropertyType<?>> tag) {
        return getOrCreateReference().is(tag);
    }

    private Holder.Reference<SoldierPropertyType<?>> getOrCreateReference() {
        if (holder == null) {
            holder = ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY.getHolderOrThrow(
                    ResourceKey.create(ModRegistries.SOLDIER_PROPERTY_TYPES, ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY.getKey(this)
                    ));
        }
        return holder;
    }
}
