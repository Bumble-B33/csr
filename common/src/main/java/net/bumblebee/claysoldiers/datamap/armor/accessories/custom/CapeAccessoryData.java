package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CapeAccessoryData(ResourceLocation textureLocation, ColorHelper color, boolean affectedByOffsetColor) implements SoldierAccessoryData {
    public static final Codec<CapeAccessoryData> CODEC = RecordCodecBuilder.create(in -> in.group(
            ResourceLocation.CODEC.fieldOf("texture_location").forGetter(c -> c.textureLocation),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(c -> c.color),
            Codec.BOOL.optionalFieldOf("affectedByOffsetColor", true).forGetter(c -> c.affectedByOffsetColor)
    ).apply(in, CapeAccessoryData::new));
    public static final StreamCodec<ByteBuf, CapeAccessoryData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, c -> c.textureLocation,
            ColorHelper.STREAM_CODEC, c -> c.color,
            ByteBufCodecs.BOOL, c -> c.affectedByOffsetColor,
            CapeAccessoryData::new
    );
    public CapeAccessoryData(ResourceLocation textureLocation) {
        this(textureLocation, ColorHelper.EMPTY, true);
    }
}
