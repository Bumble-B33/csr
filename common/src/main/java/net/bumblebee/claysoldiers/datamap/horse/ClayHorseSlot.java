package net.bumblebee.claysoldiers.datamap.horse;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public enum ClayHorseSlot implements StringRepresentable {
    ARMOR("armor", EquipmentSlot.BODY),
    HORN("horn", EquipmentSlot.HEAD);

    public static final Codec<ClayHorseSlot> CODEC = StringRepresentable.fromEnum(ClayHorseSlot::values);

    private final String serializedName;
    private final EquipmentSlot slot;

    ClayHorseSlot(String serializedName, EquipmentSlot slot) {
        this.serializedName = serializedName;
        this.slot = slot;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public EquipmentSlot asEquipmentSlot() {
        return slot;
    }
}
