package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSingleSoldier;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModPoiTypes;
import net.bumblebee.claysoldiers.networking.HamsterWheelEnergyPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class HamsterWheelBlockEntity extends BlockEntityWithSingleSoldier implements StatInfoDisplay {
    public static final Identifier WORKSITE_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel");
    private final AssignableWorksiteCapability poiCap = new AssignableWorksiteCapability() {
        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            return clayMob instanceof AbstractClaySoldierEntity soldier && soldier.getSoldierSize() <= 1.45f;
        }

        @Override
        public int onUse(ClayMobEntity clayMob, int acceleration) {
            if (clayMob instanceof AbstractClaySoldierEntity soldier) {
                addSoldier(soldier, true, acceleration);
                return 1;
            } else {
                throw new IllegalArgumentException(clayMob + " cannot use this poi");
            }
        }

        @Override
        public Identifier descriptionId() {
            return WORKSITE_ID;
        }
    };
    @Nullable
    private HamsterWheelEnergyStorage energyStorage = null;

    private long lastEnergySend = 0;
    private float rotationTick = 0;

    public HamsterWheelBlockEntity(BlockPos pPos, BlockState state) {
        super(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), pPos, state, ModPoiTypes.SINGLE_SOLDIER_CONTAINER_POI_KEY, ModPoiTypes.SINGLE_SOLDIER_CONTAINER.get());
        setEnergyStorage(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBlockState(@NonNull BlockState state) {
        super.setBlockState(state);
        int currentEnergyStored = energyStorage == null ? 0 : energyStorage.energyStored();
        setEnergyStorage(state);
        if (currentEnergyStored > 0) {
            energyStorage.setEnergy(currentEnergyStored);
        }
    }

    private void setEnergyStorage(BlockState current) {
        BatteryProperties multiplier = current.getValue(HamsterWheelBlock.BATTERY_PROPERTY).getBatteryProperty();
        if (multiplier != null) {
            energyStorage = ClaySoldiersCommon.ENERGY_HELPER.createEnergyStorage(this, multiplier.capacity(), multiplier.maxInsert(), multiplier.maxExtract());
        }
    }

    public void clientTick() {
        if (hasSoldier()) {
            //Todo
            float speed = getSoldierSpeedFactor();

            walkAnimation.update(0.8f * Mth.sqrt(speed), 0.4F, 1f);

            rotationTick += speed;

            getSoldierData().getClientSoldier().increaseTickCount();

        }
    }

    public float getSoldierSpeedFactor() {
        float speed = getSoldierData().getSpeed();
        return 1f + (speed * 0.15f);
    }

    public float getRotationTick(float partialTick) {
        return rotationTick + (hasSoldier() ? partialTick * getSoldierSpeedFactor() : 0);
    }

    public AssignableWorksiteCapability getPoiCap() {
        return poiCap;
    }

    public @Nullable HamsterWheelEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (hasEnergyStorage()) {
            if (direction == null) {
                return energyStorage;
            }
            return direction.getOpposite() == getBlockState().getValue(HamsterWheelBlock.FACING) ? energyStorage : null;
        }
        return null;
    }

    public static @Nullable HamsterWheelEnergyStorage getEnergyStorage(BlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof HamsterWheelBlockEntity hamsterWheelBlockEntity) {
            return hamsterWheelBlockEntity.getEnergyStorage(context);
        }
        return null;
    }

    public void setStartingEnergy(int amount) {
        if (energyStorage == null) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Try to set energy for a Hamster Wheel with out an EnergyStorage");
        } else {
            energyStorage.setEnergy(energyStorage.energyStored() + amount);
        }
    }

    public boolean hasEnergyStorage() {
        return HamsterWheelBlock.hasPowerConnection(getBlockState());
    }

    public boolean hasSecondBattery() {
        return getBlockState().getValue(HamsterWheelBlock.BATTERY_PROPERTY) == HamsterWheelBatteryProperty.DUAL;
    }


    @Override
    protected Vec3 getExitPosition() {
        BlockPos pos = worldPosition;
        Direction direction = getBlockState().getValue(HamsterWheelBlock.FACING);
        return new Vec3(pos.getX() + 0.5 + (direction.getStepX() * 0.3f), pos.getY(), pos.getZ() + 0.5 + (direction.getStepZ() * 0.3f));
    }


    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (energyStorage != null) {
            output.putInt(HamsterWheelEnergyStorage.TAG_KEY, energyStorage.energyStored());
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput tag) {
        super.loadAdditional(tag);
        if (energyStorage != null) {
            int energy = Math.min(tag.getIntOr(HamsterWheelEnergyStorage.TAG_KEY, 0), energyStorage.maxEnergyStored());
            energyStorage.setEnergy(energy);
        }
    }

    @Override
    protected void saveToUpdateTag(ValueOutput output) {
        if (energyStorage != null) {
            output.putLong(HamsterWheelEnergyStorage.TAG_KEY, energyStorage.energyStored());
        }
    }

    public void serverTick() {
        if (hasEnergyStorage()) {
            int generated = hasSoldier() ? HamsterWheelEnergyStorage.energyGeneratedPerTick(getSoldierData().getAdjustedSpeed()) : 0;
            energyStorage.generate(generated);
            energyStorage.distribute();
        }
        if (hasEnergyStorage() && Math.abs(lastEnergySend - energyStorage.energyStored()) > 2) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new HamsterWheelEnergyPayload(energyStorage.energyStored(), getBlockPos()));
            lastEnergySend = energyStorage.energyStored();
            setChanged();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel) {
            HamsterWheelBatteryProperty batteryState = state.getValue(HamsterWheelBlock.BATTERY_PROPERTY);
            if (batteryState == HamsterWheelBatteryProperty.SINGLE || batteryState == HamsterWheelBatteryProperty.DUAL) {
                ItemStack battery = HamsterWheelBlock.BATTERY_ITEM.get().getDefaultInstance();
                int energyRemaining = energyStorage.energyStored() - ClaySoldiersCommon.ENERGY_HELPER.insert(battery, energyStorage.energyStored());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), battery);
                if (batteryState == HamsterWheelBatteryProperty.DUAL) {
                    ItemStack secondBattery = HamsterWheelBlock.BATTERY_ITEM.get().getDefaultInstance();
                    ClaySoldiersCommon.ENERGY_HELPER.insert(secondBattery, energyRemaining);
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), secondBattery);
                }
            }
        }

    }

    public Direction getFacing() {
        return getBlockState().getValue(HamsterWheelBlock.FACING);
    }

    @Override
    public String toString() {
        return "HamsterWheelBlockEntity(%s, %s)".formatted(worldPosition.toShortString(), level);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HamsterWheelBlockEntity entity = (HamsterWheelBlockEntity) o;
        return worldPosition.equals(entity.worldPosition);
    }

    @Override
    public int hashCode() {
        return worldPosition.hashCode();
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        String energyUnit = ClaySoldiersCommon.ENERGY_HELPER.getEnergyUnitName();
        list.add(getBlockState().getBlock().getName());
        addSoldierDataView(list, viewer, true);

        if (hasEnergyStorage()) {
            list.add(CommonComponents.space().append(
                    Component.translatable(StatInfoDisplay.ENERGY_LANG, energyStorage.energyStored() + energyUnit, energyStorage.maxEnergyStored() + energyUnit).withStyle(ChatFormatting.GRAY)
            ));
            list.add(CommonComponents.space().append(
                    Component.translatable(StatInfoDisplay.GENERATION_LANG, HamsterWheelEnergyStorage.energyGeneratedPerTick(hasSoldier() ? getSoldierData().getAdjustedSpeed() : 0) + energyUnit).withStyle(ChatFormatting.GRAY)
            ));
        }
    }
}
