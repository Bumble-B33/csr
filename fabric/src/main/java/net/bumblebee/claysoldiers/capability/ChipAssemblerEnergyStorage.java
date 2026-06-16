package net.bumblebee.claysoldiers.capability;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.block.chipassembler.ChipEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class ChipAssemblerEnergyStorage extends SimpleEnergyStorage implements ChipEnergyStorage {
    public ChipAssemblerEnergyStorage() {
        super(ChipEnergyStorage.MAX_CAPACITY, ChipEnergyStorage.MAX_CAPACITY, 0);
    }

    @Override
    public long getEnergyStored() {
        return getAmount();
    }

    @Override
    public int insert(int amount) {
        try (Transaction tx = Transaction.openOuter()) {
            return Ints.saturatedCast(insert(amount, tx));
        }
    }

    @Override
    public void remove(int amount) {
        this.amount = this.amount - amount;
    }

    @Override
    public void set(int amount) {
        this.amount = amount;
    }

    @Override
    public long getMaxCapacity() {
        return getCapacity();
    }

    @Override
    public String toString() {
        return "ChipAssemblerEnergyStorage{" + amount + '}';
    }
}
