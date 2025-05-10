package net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum EffectImmunityType implements StringRepresentable, KeyableTranslatableProperty {
    IMMUNE("immune"),
    PERSISTENT("persistent");

    public static final Codec<EffectImmunityType> CODEC = StringRepresentable.fromEnum(EffectImmunityType::values);
    public static final StreamCodec<FriendlyByteBuf, EffectImmunityType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(EffectImmunityType.class);

    private final String serializedName;

    EffectImmunityType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    @Override
    public String translatableKey() {
        return ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG  + "immunity_type."+ serializedName;
    }

    @Override
    public ChatFormatting getFormat() {
        return ChatFormatting.DARK_GRAY;
    }
}
