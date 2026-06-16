package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface HamsterWheelEnergyStorage {
    String TAG_KEY = "wheel_energy";

    /**
     * @return the energy stored in this storage
     */
    long energyStored();

    /**
     * @return the capacity of this storage
     */
    long maxEnergyStored();

    /**
     * Generates energy if possible
     */
    void generate(float speed);

    void save(ValueOutput tag);

    void load(ValueInput tag);

    /**
     * Sets the energy of this storage to the given energy
     */
    void setEnergy(long energy);

    /**
     * Returns this energy storage as view only, no energy can be extracted.
     * Energy can still be set with {@link #setEnergy}.
     */
    HamsterWheelEnergyStorage asViewOnly();

    static long energyGeneratedPerTick(float speed) {
        if (speed <= 0) {
            return 0;
        }
        return (long) Math.max(1, ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelSpeed() * speed);
    }
}
