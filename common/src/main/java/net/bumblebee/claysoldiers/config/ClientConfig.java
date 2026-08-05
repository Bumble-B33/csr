package net.bumblebee.claysoldiers.config;

import net.bumblebee.claysoldiers.energy.EnergyColor;

public interface ClientConfig {
    default boolean statItemShowStats() {
        return true;
    }

    default boolean statItemShowCount() {
        return true;
    }

    default int getEnergyColor() {
        return EnergyColor.GREEN.getColor();
    }
}
