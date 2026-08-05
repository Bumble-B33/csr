package net.bumblebee.claysoldiers.block.chipassembler;

public interface UsingEnergyStorage {
    int insert(int amount);

    default void remove(int amount) {
        setEnergy(Math.max(0, energyStored() - amount));
    }

    void setEnergy(int amount);

    int maxEnergyStored();

    int energyStored();
}
