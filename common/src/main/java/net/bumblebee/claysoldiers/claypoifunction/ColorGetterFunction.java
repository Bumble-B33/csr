package net.bumblebee.claysoldiers.claypoifunction;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.function.Function;

public enum ColorGetterFunction implements StringRepresentable, KeyableTranslatableProperty {
    NONE("none", (_) -> ColorHelper.EMPTY),
    FROM_DYE("from_item", ClayPoiSource::getDyeItemColor),
    FROM_BLOCK_MAP_COLOR("from_block", ClayPoiSource::getBlockItemMapColor);

    public static final Codec<ColorGetterFunction> CODEC = StringRepresentable.fromEnum(ColorGetterFunction::values);
    public static final StreamCodec<FriendlyByteBuf, ColorGetterFunction> STREAM_CODEC = CodecUtils.createEnumStreamCodec(ColorGetterFunction.class);

    private final String serializedName;
    private final Function<ClayPoiSource, ColorHelper> colorGetter;

    ColorGetterFunction(String serializedName, Function<ClayPoiSource, ColorHelper> colorGetter) {
        this.serializedName = serializedName;
        this.colorGetter = colorGetter;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
    public ColorHelper getColor(ClayPoiSource source) {
        return colorGetter.apply(source);
    }

    @Override
    public String translatableKey() {
        return ClayPoiFunctions.DYE_FUNCTION + ".color_getter." + serializedName;
    }
}

