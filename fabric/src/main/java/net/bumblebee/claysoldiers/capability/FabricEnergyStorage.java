package net.bumblebee.claysoldiers.capability;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.IHamsterWheelEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class FabricEnergyStorage extends SimpleEnergyStorage implements IHamsterWheelEnergyStorage {
    private final HamsterWheelBlockEntity blockEntity;
    private final FabricViewOnly viewOnly;
    @Nullable
    private BlockApiCache<EnergyStorage, Direction> cached;

    public FabricEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity) {
        super(0, 0, ClaySoldierFabric.hamsterWheelCapacity);
        this.blockEntity = hamsterWheelBlockEntity;
        this.viewOnly = new FabricViewOnly(this);
    }

    @Override
    public long getCapacity() {
        return ClaySoldierFabric.hamsterWheelCapacity * blockEntity.getEnergyCapacityMultiplier();
    }

    @Override
    public void setEnergy(long energy) {
        amount = energy;
    }

    @Override
    public boolean supportsExtraction() {
        return super.supportsExtraction() && blockEntity.hasEnergyStorage();
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return blockEntity.hasEnergyStorage() ? super.extract(maxAmount, transaction) : 0;
    }

    @Override
    public long energyStored() {
        return getAmount();
    }

    @Override
    public long maxEnergyStored() {
        return getCapacity();
    }

    @Override
    public void generate(float speed) {
        long generated = amount + (int) Math.max(1, ClaySoldierFabric.hamsterWheelSpeed * speed);
        if (generated < 0) {
            generated = Long.MAX_VALUE;
        }
        amount = Math.min(generated, getCapacity());
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING);
        if (cached == null) {
            cached = BlockApiCache.create(EnergyStorage.SIDED, serverLevel, blockEntity.getBlockPos().relative(direction));
        }
        var storage = cached.find(direction.getOpposite());
        if (storage != null && storage.supportsInsertion()) {
            try (Transaction transaction = Transaction.openOuter()) {
                long insertable;
                try (Transaction simulateTransaction = transaction.openNested()) {
                    insertable = storage.insert(Long.MAX_VALUE, simulateTransaction);
                }

                long extracted = this.extract(insertable, transaction);
                long inserted = storage.insert(extracted, transaction);
                if (extracted == inserted)
                    transaction.commit();
            }
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong(TAG_KEY, amount);
    }

    @Override
    public void load(CompoundTag tag) {
        amount = Math.min(ClaySoldierFabric.hamsterWheelCapacity, tag.getLong(TAG_KEY));
    }

    @Override
    public IHamsterWheelEnergyStorage asViewOnly() {
        return viewOnly;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        blockEntity.setChanged();
    }

    private record FabricViewOnly(FabricEnergyStorage storage) implements IHamsterWheelEnergyStorage, EnergyStorage {
        @Override
        public boolean supportsExtraction() {
            return false;
        }


        @Override
        public long energyStored() {
            return storage.energyStored();
        }

        @Override
        public long maxEnergyStored() {
            return storage.maxEnergyStored();
        }

        @Override
        public void generate(float speed) {
        }

        @Override
        public void save(CompoundTag tag) {
        }

        @Override
        public void load(CompoundTag tag) {
        }

        @Override
        public void setEnergy(long energy) {
            storage.setEnergy(energy);
        }

        @Override
        public IHamsterWheelEnergyStorage asViewOnly() {
            return this;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            return storage.getAmount();
        }

        @Override
        public long getCapacity() {
            return storage.getCapacity();
        }
    }
}
