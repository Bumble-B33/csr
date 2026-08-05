package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum HamsterWheelBatteryProperty implements StringRepresentable {
    NONE("none", 0),
    SINGLE("single", 1),
    DUAL("dual", 2);

    private final String serializedName;
    private final @Nullable BatteryProperties batteryProperty;

    HamsterWheelBatteryProperty(String serializedName, int capacityMultiplier) {
        this.serializedName = serializedName;
        this.batteryProperty = capacityMultiplier <= 0 ? null : BatteryProperties.of(capacityMultiplier).allowExtraction().build();
    }

    public @Nullable BatteryProperties getBatteryProperty() {
        return batteryProperty;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
