package net.bumblebee.claysoldiers.cap;

import net.bumblebee.claysoldiers.ConfigNeoForge;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.IHamsterWheelEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class NeoForgeEnergy extends EnergyStorage implements IHamsterWheelEnergyStorage  {
    private final HamsterWheelBlockEntity blockEntity;
    private final NeoForgeViewOnly viewOnly;
    @Nullable
    private BlockCapabilityCache<IEnergyStorage ,Direction> cached;

    public NeoForgeEnergy(HamsterWheelBlockEntity entity) {
        super(ConfigNeoForge.HAMSTER_WHEEL_CAPACITY.getAsInt(), 0, ConfigNeoForge.HAMSTER_WHEEL_CAPACITY.getAsInt(), 0);
        this.blockEntity = entity;
        this.viewOnly = new NeoForgeViewOnly(this);
    }

    @Override
    public void setEnergy(long energy) {
        this.energy =  Math.min((int) energy, getMaxEnergyStored());
    }

    @Override
    public long energyStored() {
        return getEnergyStored();
    }

    @Override
    public long maxEnergyStored() {
        return getMaxEnergyStored();
    }

    @Override
    public void generate(float speed) {
        int generate = energy + (int) Math.max(1, ConfigNeoForge.HAMSTER_WHEEL_SPEED.get() * speed);
        if (generate < 0) {
            generate = Integer.MAX_VALUE;
        }
        energy = Math.min(generate, getMaxEnergyStored());
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction direction = blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING);
        if (cached == null) {
            cached = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, serverLevel, blockEntity.getBlockPos().relative(direction), direction.getOpposite());
        }
        IEnergyStorage storage = cached.getCapability();
        if (storage != null && storage.canReceive()) {
            int amount = extractEnergy(maxExtract, true);
            extractEnergy(storage.receiveEnergy(amount, false), false);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt(TAG_KEY, energy);
    }

    @Override
    public void load(CompoundTag tag) {
        energy = Math.min(tag.getInt(TAG_KEY), getMaxEnergyStored());
    }

    @Override
    public boolean canExtract() {
        return super.canExtract() && blockEntity.hasEnergyStorage();
    }

    @Override
    public IHamsterWheelEnergyStorage asViewOnly() {
        return viewOnly;
    }

    @Override
    public int getMaxEnergyStored() {
        return ConfigNeoForge.HAMSTER_WHEEL_CAPACITY.getAsInt() * blockEntity.getEnergyCapacityMultiplier();
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        int energyExtracted = Math.min(this.energy, Math.min(this.getMaxExtract(), toExtract));
        if (!simulate)
            this.energy -= energyExtracted;
        return energyExtracted;
    }

    private int getMaxExtract() {
        return ConfigNeoForge.HAMSTER_WHEEL_CAPACITY.getAsInt();
    }

    private record NeoForgeViewOnly(NeoForgeEnergy energyStorage) implements IEnergyStorage, IHamsterWheelEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energyStorage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyStorage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public long energyStored() {
            return getEnergyStored();
        }

        @Override
        public long maxEnergyStored() {
            return getMaxEnergyStored();
        }

        @Override
        public void generate(float speed) {}

        @Override
        public void save(CompoundTag tag) {}

        @Override
        public void load(CompoundTag tag) {}

        @Override
        public void setEnergy(long energy) {
            energyStorage.setEnergy(energy);
        }

        @Override
        public IHamsterWheelEnergyStorage asViewOnly() {
            return this;
        }
    }
}
