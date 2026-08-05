package net.bumblebee.claysoldiers.datamap;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.function.Predicate;

public enum HoldingPose implements StringRepresentable {
    NONE("none", List.of()),
    TWO_HANDED_BLOCK("two_handed", SoldierEquipmentSlot.HANDS);

    public static final Codec<HoldingPose> CODEC = StringRepresentable.fromEnum(HoldingPose::values);
    public static final StreamCodec<FriendlyByteBuf, HoldingPose> STREAM_CODEC = CodecUtils.createEnumStreamCodec(HoldingPose.class);

    private final String serializedName;
    private final List<SoldierEquipmentSlot> requiredSlots;

    HoldingPose(String serializedName, List<SoldierEquipmentSlot> requiredSlots) {
        this.serializedName = serializedName;
        this.requiredSlots = requiredSlots;
    }

    public boolean test(Predicate<SoldierEquipmentSlot> isSlotEmpty) {
        return requiredSlots.stream().allMatch(isSlotEmpty);
    }

    public List<SoldierEquipmentSlot> getRequiredSlots() {
        return requiredSlots;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
