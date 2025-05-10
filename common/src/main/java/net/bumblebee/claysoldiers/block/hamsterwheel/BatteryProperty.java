package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum BatteryProperty implements StringRepresentable {
    NONE("none", 0),
    SINGLE("single", 1),
    DUAL("dual", 2);

    private final String serializedName;
    private final int capacityMultiplier;

    BatteryProperty(String serializedName, int capacityMultiplier) {
        this.serializedName = serializedName;
        this.capacityMultiplier = capacityMultiplier;
    }


    public int getCapacityMultiplier() {
        return capacityMultiplier;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static <T> T getMaxSupportedEnergy(IntFunction<T> maxMultiplierToStorage) {
        int maxMultiplier = 0;
        for (BatteryProperty batteryProperty : values()) {
            maxMultiplier = Math.max(batteryProperty.getCapacityMultiplier(), maxMultiplier);
        }
        return maxMultiplierToStorage.apply(maxMultiplier);
    }
}
