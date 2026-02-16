package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TextureAccessoryData(ResourceLocation textureLocation) implements SoldierAccessoryData {
    public static final Codec<TextureAccessoryData> CODEC = ResourceLocation.CODEC.xmap(TextureAccessoryData::new, s -> s.textureLocation);
    public static final StreamCodec<ByteBuf, TextureAccessoryData> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(TextureAccessoryData::new, s -> s.textureLocation);

    public static final ResourceLocation BAMBOO_STICK_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/bamboo_stick.png");
    public static final ResourceLocation STUDDED_SHIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/studded_clay_shield.png");
    public static final ResourceLocation SHIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/clay_shield.png");
    public static final ResourceLocation STRING_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/wrapped.png");

    public static TextureAccessoryData ofString() {
        return new TextureAccessoryData(STRING_TEXTURE);
    }
    public static TextureAccessoryData ofShield() {
        return new TextureAccessoryData(SHIELD_TEXTURE);
    }
    public static TextureAccessoryData ofStuddedShield() {
        return new TextureAccessoryData(STUDDED_SHIELD_TEXTURE);
    }
    public static TextureAccessoryData ofSnorkel() {
        return new TextureAccessoryData(BAMBOO_STICK_TEXTURE);
    }
}
