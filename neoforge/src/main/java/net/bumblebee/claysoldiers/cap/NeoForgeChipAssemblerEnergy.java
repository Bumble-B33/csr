package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.block.chipassembler.ChipEnergyStorage;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class NeoForgeChipAssemblerEnergy extends SimpleEnergyHandler implements ChipEnergyStorage {
    public NeoForgeChipAssemblerEnergy() {
        super(ChipEnergyStorage.MAX_CAPACITY, ChipEnergyStorage.MAX_CAPACITY, 0);
    }

    @Override
    public long getEnergyStored() {
        return getAmountAsLong();
    }

    @Override
    public int insert(int amount) {
        int i;
        try (Transaction tx = Transaction.openRoot()) {
            i = insert(amount, tx);
        }
        return i;
    }

    @Override
    public void set(int amount) {
        this.energy = amount;
    }

    @Override
    public void remove(int amount) {
        this.energy -= amount;
    }

    @Override
    public long getMaxCapacity() {
        return getCapacityAsLong();
    }

    @Override
    public String toString() {
        return "NeoForgeChipAssemblerEnergy{" + energy + '}';
    }
}
