package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class NeoForgeHamsterWheelEnergy extends SimpleEnergyHandler implements HamsterWheelEnergyStorage {
    private final HamsterWheelBlockEntity blockEntity;
    @Nullable
    private BlockCapabilityCache<EnergyHandler, Direction> cached;

    public NeoForgeHamsterWheelEnergy(HamsterWheelBlockEntity blockEntity, int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
        this.blockEntity = blockEntity;
    }

    @Override
    public int energyStored() {
        return energy;
    }

    @Override
    public int maxEnergyStored() {
        return capacity;
    }

    @Override
    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
    }

    @Override
    public void distribute() {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = blockEntity.getFacing();

        if (cached == null || cached.context() != direction) {
            cached = BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, blockEntity.getBlockPos().relative(direction.getOpposite()), direction);
        }
        EnergyHandler storage = cached.getCapability();
        EnergyHandlerUtil.move(this, storage, maxExtract, null);
    }

    @Override
    public String toString() {
        return "HamsterWheelNeoForgeEnergy{" + energy + '}';
    }
}
