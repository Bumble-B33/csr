package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.minecraft.nbt.CompoundTag;

public interface IHamsterWheelEnergyStorage {
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

    void save(CompoundTag tag);

    void load(CompoundTag tag);

    /**
     * Sets the energy of this storage to the given energy
     */
    void setEnergy(long energy);

    /**
     * Returns this energy storage as view only, no energy can be extracted.
     * Energy can still be set with {@link #setEnergy}.
     */
    IHamsterWheelEnergyStorage asViewOnly();
}
