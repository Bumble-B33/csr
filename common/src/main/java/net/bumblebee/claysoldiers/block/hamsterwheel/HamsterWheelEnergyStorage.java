package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;

public interface HamsterWheelEnergyStorage {
    String TAG_KEY = "energy";

    /**
     * @return the energy stored in this storage
     */
    int energyStored();

    /**
     * @return the capacity of this storage
     */
    int maxEnergyStored();

    /**
     * Sets the energy of this storage to the given energy
     */
    void setEnergy(int energy);

    /**
     * Generates energy if possible
     */
    default void generate(int amount) {
        if (amount <= 0) {
            return;
        }
        long generate = energyStored() + (long) amount;
        setEnergy(Ints.saturatedCast(generate));
    }

    /**
     * Pushes Energy to a nearby Energy Storage
     */
    void distribute();

    static int energyGeneratedPerTick(float speed) {
        if (speed <= 0) {
            return 0;
        }
        return (int) Math.max(1d, (double) ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelSpeed() * speed);
    }
}
