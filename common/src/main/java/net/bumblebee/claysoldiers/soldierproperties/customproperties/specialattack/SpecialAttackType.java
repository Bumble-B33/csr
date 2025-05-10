package net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum SpecialAttackType implements StringRepresentable {
    MELEE("melee"),
    RANGED("ranged"),
    MELEE_AND_RANGED("melee_and_ranged");

    public static final Codec<SpecialAttackType> CODEC = StringRepresentable.fromEnum(SpecialAttackType::values);
    public static final StreamCodec<FriendlyByteBuf, SpecialAttackType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(SpecialAttackType.class);

    private final String serialiedName;

    SpecialAttackType(String serialiedName) {
        this.serialiedName = serialiedName;
    }

    public boolean is(SpecialAttackType type) {
        return type == this || this == MELEE_AND_RANGED;
    }

    @Override
    public String getSerializedName() {
        return serialiedName;
    }
}
