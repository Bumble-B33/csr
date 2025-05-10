package net.bumblebee.claysoldiers.soldierproperties;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.UnitProperty;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SoldierProperty<T> {
    private final SoldierPropertyType<T> type;
    private final T value;

    public SoldierProperty(@NotNull SoldierPropertyType<T> type, @NotNull T value) {
        this.type = type;
        this.value = value;
    }

    public static SoldierProperty<UnitProperty> unit(SoldierPropertyType<UnitProperty> unit) {
        return new SoldierProperty<>(unit, UnitProperty.INSTANCE);
    }

    @NotNull
    public T value() {
        return value;
    }

    @NotNull
    public SoldierPropertyType<T> type() {
        return type;
    }

    public SoldierProperty<T> createWithCombinedValues(@NotNull T other) {
        T result;
        try {
            result = type.combine(value, other);
        } catch (IllegalArgumentException e) {
            result = value;
            ClaySoldiersCommon.LOGGER.error("Tried combining to incompatible values {}, {}, using the first", value, other);
        }

        return new SoldierProperty<>(type, result);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        SoldierProperty<?> that = (SoldierProperty<?>) other;
        return type.equals(that.type);
    }

    public void streamEncode(RegistryFriendlyByteBuf byteBuf) {
        type.streamEncode(byteBuf, value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    public boolean is(TagKey<SoldierPropertyType<?>> tagKey) {
        return type.is(tagKey);
    }

    @Override
    public String toString() {
        return "SoldierProperty[" + type + ", " + value + ']';
    }
}
