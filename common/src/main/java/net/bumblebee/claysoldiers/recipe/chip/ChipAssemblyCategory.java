package net.bumblebee.claysoldiers.recipe.chip;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum ChipAssemblyCategory implements StringRepresentable {
    CHIP(0, "chip"),
    ADDON(1, "addon"),
    MISC(2, "misc");

    public static final Codec<ChipAssemblyCategory> CODEC = StringRepresentable.fromEnum(ChipAssemblyCategory::values);
    private static final IntFunction<ChipAssemblyCategory> BY_ID = ByIdMap.continuous((e) -> e.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ChipAssemblyCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, (e) -> e.id);

    private final int id;
    private final String serializedName;

    ChipAssemblyCategory(int id, String serializedName) {
        this.id = id;
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
