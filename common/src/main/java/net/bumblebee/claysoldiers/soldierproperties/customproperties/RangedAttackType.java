package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public enum RangedAttackType implements StringRepresentable, KeyableTranslatableProperty {
    NONE("none", null),
    HARM("harmful", ChatFormatting.RED),
    HELPING("helping", ChatFormatting.GREEN);

    public static final Codec<RangedAttackType> CODEC = StringRepresentable.fromEnum(RangedAttackType::values);
    public static final StreamCodec<FriendlyByteBuf, RangedAttackType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(RangedAttackType.class);
    public static final ToIntFunction<RangedAttackType> TO_INT = RangedAttackType::ordinal;
    public static final ValueCombiner<RangedAttackType> COMBINER = (r1, r2) -> r1 != NONE ? r1 : r2;
    public static final String RANGED_ATTACK_TYPE = ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "ranged_attack";

    private final String serializedName;
    @Nullable
    private final ChatFormatting chatFormatting;

    RangedAttackType(String serializedName, @Nullable ChatFormatting chatFormatting) {
        this.serializedName = serializedName;
        this.chatFormatting = chatFormatting;
    }

    public boolean canPerformRangedAttack() {
        return this != NONE;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    @Override
    public String toString() {
        return serializedName;
    }

    @Override
    public @Nullable Component getDisplayName() {
        var displayName = KeyableTranslatableProperty.super.getDisplayName();
        if (displayName == null) {
            return null;
        }
        return Component.translatable(RANGED_ATTACK_TYPE).append(": ").append(displayName);
    }

    @Override
    public ChatFormatting getFormat() {
        return chatFormatting;
    }

    @Override
    public String translatableKey() {
        return ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "ranged_attack_type." + serializedName;
    }
}
