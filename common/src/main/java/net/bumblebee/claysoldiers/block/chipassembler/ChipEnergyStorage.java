package net.bumblebee.claysoldiers.block.chipassembler;

public interface ChipEnergyStorage {
    int MAX_CAPACITY = 9000;

    long getEnergyStored();

    int insert(int amount);

    void remove(int amount);

    void set(int amount);

    long getMaxCapacity();
}
