package net.bumblebee.claysoldiers.block;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.networking.BlockEntityEnergyPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class BlockEntityWithEnergy extends BlockEntity {
    private static final String ENERGY_TAG = HamsterWheelEnergyStorage.TAG_KEY;

    protected final @NonNull UsingEnergyStorage energyStorage;
    protected final BatteryProperties batteryProperties;
    protected int lastEnergyStored;


    public BlockEntityWithEnergy(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, BatteryProperties batteryProperties) {
        super(type, worldPosition, blockState);
        this.energyStorage = ClaySoldiersCommon.ENERGY_HELPER.createEnergyChipStorage(this::onEnergyChange, batteryProperties.capacity(), batteryProperties.maxInsert(), batteryProperties.maxExtract());
        this.lastEnergyStored = energyStorage.energyStored();
        this.batteryProperties = batteryProperties;
    }

    protected void onEnergyChange(int previous) {
        setChanged();
        if (energyRequiresUpdate()) {
            sendEnergyUpdatePayLoad();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setEnergy(input.getIntOr(ENERGY_TAG, 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(ENERGY_TAG, energyStorage.energyStored());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, registries);
        output.putInt(ENERGY_TAG, energyStorage.energyStored());
        return output.buildResult();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Contract("null -> !null")
    public @Nullable UsingEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == null) {
            return energyStorage;
        }
        return isValidDirectionForEnergy(direction) ? energyStorage : null;
    }

    public abstract boolean isValidDirectionForEnergy(Direction direction);

    protected boolean energyRequiresUpdate() {
        int energy = energyStorage.energyStored();
        if (energy == lastEnergyStored) {
            return false;
        }
        if (energy == 0) {
            return true;
        }
        if (Math.abs(energy - lastEnergyStored) > 3) {
            return true;
        }
        return energy >= energyStorage.maxEnergyStored();
    }

    protected void sendEnergyUpdatePayLoad() {
        if (level instanceof ServerLevel) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new BlockEntityEnergyPayload(worldPosition, energyStorage.energyStored()));
            lastEnergyStored = energyStorage.energyStored();
        }
    }

    public void setEnergy(int energy) {
        this.energyStorage.setEnergy(energy);
        if (level instanceof ServerLevel) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Setting Energy on the Server");
        }
    }

    protected boolean fillFromBattery(ItemStack battery) {
        int wanted = Math.min(batteryProperties.maxInsert(), energyStorage.maxEnergyStored() - energyStorage.energyStored());
        int got = ClaySoldiersCommon.ENERGY_HELPER.extract(battery, wanted);
        if (got <= 0) {
            return false;
        }

        energyStorage.insert(got);
        return true;
    }

    public static InteractionResult fillFromBattery(ItemStack itemStack, Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityWithEnergy blockEntityWithEnergy) {
            return blockEntityWithEnergy.fillFromBattery(itemStack) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }
}
