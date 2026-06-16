package net.bumblebee.claysoldiers.cap;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class HamsterWheelNeoForgeEnergy implements EnergyHandler, HamsterWheelEnergyStorage {
    private final HamsterWheelBlockEntity blockEntity;
    private final NeoForgeViewOnly viewOnly;
    private final EnergyJournal journal;
    private long energy;
    @Nullable
    private BlockCapabilityCache<EnergyHandler,Direction> cached;

    public HamsterWheelNeoForgeEnergy(HamsterWheelBlockEntity entity) {
        this.blockEntity = entity;
        this.journal = new EnergyJournal();
        this.viewOnly = new NeoForgeViewOnly(this);
    }

    @Override
    public void setEnergy(long energy) {
        this.energy = Math.min(energy, maxEnergyStored());
    }

    @Override
    public long energyStored() {
        return getAmountAsLong();
    }

    @Override
    public long maxEnergyStored() {
        return ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelEnergyCapacity() * blockEntity.getEnergyCapacityMultiplier();
    }

    @Override
    public void generate(float speed) {
        long generate = energy + HamsterWheelEnergyStorage.energyGeneratedPerTick(speed);
        if (generate < 0) {
            generate = 0;
        }
        energy = Math.min(generate, maxEnergyStored());
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING);
        if (cached == null) {
            cached = BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, blockEntity.getBlockPos().relative(direction.getOpposite()), direction);
        }
        EnergyHandler storage = cached.getCapability();
        if (storage != null) {
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = extract(getMaxExtract(), tx);
                tx.close();
                try (Transaction tx2 = Transaction.openRoot()) {
                    int inserted = storage.insert(extracted, tx2);
                    tx2.commit();
                    try (Transaction tx3 = Transaction.openRoot()) {
                        extract(inserted, tx3);
                        tx3.commit();
                    }
                }
            }
        }
    }

    @Override
    public void save(ValueOutput tag) {
        tag.putLong(TAG_KEY, energy);
    }

    @Override
    public void load(ValueInput tag) {
        energy = Math.min(tag.getLongOr(TAG_KEY, 0), maxEnergyStored());
    }

    @Override
    public HamsterWheelEnergyStorage asViewOnly() {
        return viewOnly;
    }

    private int getMaxExtract() {
        return Math.min(Ints.saturatedCast(ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelEnergyCapacity()), Integer.MAX_VALUE);
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return maxEnergyStored();
    }

    @Override
    public int insert(int amount, TransactionContext transactionContext) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transactionContext) {
        TransferPreconditions.checkNonNegative(amount);
        int extracted = Math.min(getAmountAsInt(), Math.min(amount, getMaxExtract()));
        if (extracted > 0) {
            this.journal.updateSnapshots(transactionContext);
            this.energy -= extracted;
            return extracted;
        }
        return 0;
    }

    private class EnergyJournal extends SnapshotJournal<Long> {
        @Override
        protected Long createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            energy = snapshot;
        }
    }

    private record NeoForgeViewOnly(HamsterWheelNeoForgeEnergy energyStorage) implements EnergyHandler, HamsterWheelEnergyStorage {
        @Override
        public long getAmountAsLong() {
            return energyStorage.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong() {
            return energyStorage.getCapacityAsLong();
        }

        @Override
        public int insert(int i, TransactionContext transactionContext) {
            return 0;
        }

        @Override
        public int extract(int i, TransactionContext transactionContext) {
            return 0;
        }

        @Override
        public long energyStored() {
            return getAmountAsLong();
        }

        @Override
        public long maxEnergyStored() {
            return getCapacityAsLong();
        }

        @Override
        public void generate(float speed) {}

        @Override
        public void save(ValueOutput tag) {}

        @Override
        public void load(ValueInput tag) {}

        @Override
        public void setEnergy(long energy) {
            energyStorage.setEnergy(energy);
        }

        @Override
        public HamsterWheelEnergyStorage asViewOnly() {
            return this;
        }
    }
}
