package net.bumblebee.claysoldiers.datamap;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum DropRateProperty implements StringRepresentable, Supplier<Float> {
    ALWAYS("always", 1f),
    NEVER("never", 0f),
    NORMAL("normal", 0.5f);

    public static final Codec<DropRateProperty> SPECIAL_CODEC = StringRepresentable.fromEnum(DropRateProperty::values);
    public static final Codec<Float> CODEC = CodecUtils.withReplacementValues(Codec.floatRange(0f, 1f), SPECIAL_CODEC, DropRateProperty.values());

    private final float chance;
    private final String serializedName;

    DropRateProperty(String serializedName, float chance) {
        this.chance = chance;
        this.serializedName = serializedName;
    }

    @Override
    public Float get() {
        return chance;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return serializedName;
    }
}
