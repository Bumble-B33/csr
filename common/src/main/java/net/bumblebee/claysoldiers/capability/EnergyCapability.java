package net.bumblebee.claysoldiers.capability;

public interface EnergyCapability {
    int insert(int amount);

    int extract(int wanted);
}
