package net.bumblebee.claysoldiers.capability;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class FabricHamsterWheelEnergyStorage extends SimpleEnergyStorage implements HamsterWheelEnergyStorage {
    private final HamsterWheelBlockEntity blockEntity;
    @Nullable
    private BlockApiCache<EnergyStorage, Direction> cached;

    public FabricHamsterWheelEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity, int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
        this.blockEntity = hamsterWheelBlockEntity;
    }

    @Override
    public int energyStored() {
        return Ints.saturatedCast(amount);
    }

    @Override
    public int maxEnergyStored() {
        return Ints.saturatedCast(maxExtract);
    }

    @Override
    public void setEnergy(int energy) {
        amount = Math.min(energy, capacity);
    }

    @Override
    public void distribute() {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = blockEntity.getFacing();

        if (cached == null) {
            cached = BlockApiCache.create(EnergyStorage.SIDED, serverLevel, blockEntity.getBlockPos().relative(direction.getOpposite()));
        }
        EnergyStorage storage = cached.find(direction);

        EnergyStorageUtil.move(this, storage, maxExtract, null);
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        blockEntity.setChanged();
    }
}
