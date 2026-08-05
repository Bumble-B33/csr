package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.IntConsumer;

public class NeoForgeChipAssemblerEnergy extends SimpleEnergyHandler implements UsingEnergyStorage {
    private final IntConsumer onChange;

    public NeoForgeChipAssemblerEnergy(IntConsumer onChange, int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
        this.onChange = onChange;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChange.accept(previousAmount);
    }

    @Override
    public int energyStored() {
        return getAmountAsInt();
    }

    @Override
    public int insert(int amount) {
        int i;
        Transaction tx = Transaction.openRoot();
        i = insert(amount, tx);
        tx.commit();

        return i;
    }

    @Override
    public void setEnergy(int amount) {
        set(Math.min(capacity, amount));
    }

    @Override
    public int maxEnergyStored() {
        return getCapacityAsInt();
    }

    @Override
    public String toString() {
        return "NeoForgeChipAssemblerEnergy{" + energy + '}';
    }
}
