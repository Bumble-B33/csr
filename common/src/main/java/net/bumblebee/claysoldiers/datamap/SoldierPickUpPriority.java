package net.bumblebee.claysoldiers.datamap;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum SoldierPickUpPriority implements StringRepresentable, Supplier<Integer> {
    LOW("low", 1),
    NORMAL("normal", 3),
    HIGH("high", 5),
    VERY_HIGH("very_high", 10);

    public static final int MIN = -3;
    public static final int MAX = 10;

    private static final Codec<SoldierPickUpPriority> CODEC_DEFAULT = StringRepresentable.fromEnum(SoldierPickUpPriority::values);
    public static final Codec<Integer> CODEC = CodecUtils.withReplacementValues(Codec.intRange(MIN, MAX), CODEC_DEFAULT, SoldierPickUpPriority.values());

    private final String serializedName;
    private final int weight;

    SoldierPickUpPriority(String serializedName, int weight) {
        this.serializedName = serializedName;
        this.weight = weight;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    @Override
    public Integer get() {
        return weight;
    }
}
