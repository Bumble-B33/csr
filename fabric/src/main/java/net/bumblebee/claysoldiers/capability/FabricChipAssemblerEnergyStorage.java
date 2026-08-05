package net.bumblebee.claysoldiers.capability;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.function.IntConsumer;

public class FabricChipAssemblerEnergyStorage extends SimpleEnergyStorage implements UsingEnergyStorage {
    private final IntConsumer onChange;
    private int lastEnergy;

    public FabricChipAssemblerEnergyStorage(IntConsumer onChange, int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
        this.onChange = onChange;
        this.lastEnergy = 0;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        onChange.accept(lastEnergy);
        lastEnergy = energyStored();
    }

    @Override
    public int insert(int amount) {
        int inserted;
        Transaction tx = Transaction.openOuter();
        inserted = Ints.saturatedCast(insert(amount, tx));
        tx.commit();

        return inserted;
    }

    @Override
    public int energyStored() {
        return Ints.saturatedCast(getAmount());
    }

    @Override
    public void setEnergy(int amount) {
        this.amount = Math.min(amount, capacity);
        onFinalCommit();
    }

    @Override
    public int maxEnergyStored() {
        return Ints.saturatedCast(getCapacity());
    }

    @Override
    public String toString() {
        return "ChipAssemblerEnergyStorage{" + amount + '}';
    }
}
