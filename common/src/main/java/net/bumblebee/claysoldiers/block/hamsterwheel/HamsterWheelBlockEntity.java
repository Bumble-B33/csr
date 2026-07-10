package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSoldier;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
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
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HamsterWheelBlockEntity extends BlockEntityWithSoldier implements StatInfoDisplay {
    public static final Identifier WORKSITE_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel");
    private final AssignableWorksiteCapability poiCap = new AssignableWorksiteCapability() {
        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            return clayMob instanceof AbstractClaySoldierEntity soldier && soldier.getSoldierSize() <= 1.45f;
        }

        @Override
        public int onUse(ClayMobEntity clayMob) {
            if (clayMob instanceof AbstractClaySoldierEntity soldier) {
                addSoldier(soldier);
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
    private final HamsterWheelEnergyStorage energyStorage;

    private long lastEnergySend = 0;
    private int rotationTick = 0;

    public HamsterWheelBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), pPos, pBlockState, ModPoiTypes.SINGLE_SOLDIER_CONTAINER_POI_KEY, ModPoiTypes.SINGLE_SOLDIER_CONTAINER.get());
        energyStorage = ClaySoldiersCommon.CAPABILITY_MANGER.createEnergyStorage(this);
    }


    public void clientTick() {
        if (hasSoldier()) {
            rotationTick += (int) Math.clamp(soldierData.getSpeed(), 1, 3);

            soldierData.getClientSoldier().increaseTickCount();

            walkAnimation.update(0.75f, 0.4F, 1f);
        }
    }

    public float getRotationTick(float partialTick) {
        return rotationTick + (hasSoldier() ? partialTick : 0);
    }

    public AssignableWorksiteCapability getPoiCap() {
        return poiCap;
    }

    public @Nullable HamsterWheelEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (hasEnergyStorage()) {
            if (direction == null) {
                return energyStorage.asViewOnly();
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

    public boolean hasEnergyStorage() {
        return HamsterWheelBlock.hasPowerConnection(getBlockState());
    }

    public boolean hasSecondBattery() {
        return getBlockState().getValue(HamsterWheelBlock.BATTERY_PROPERTY) == BatteryProperty.DUAL;
    }

    public int getEnergyCapacityMultiplier() {
        return getBlockState().getValue(HamsterWheelBlock.BATTERY_PROPERTY).getCapacityMultiplier();
    }


    @Override
    protected Vec3 getExitPosition() {
        BlockPos pos = worldPosition;
        Direction direction = getBlockState().getValue(HamsterWheelBlock.FACING);
        return new Vec3(pos.getX() + 0.5 + (direction.getStepX() * 0.3f), pos.getY(), pos.getZ() + 0.5 + (direction.getStepZ() * 0.3f));
    }


    @Override
    protected void saveAdditional(@NotNull ValueOutput pTag) {
        super.saveAdditional(pTag);
        energyStorage.save(pTag);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput pTag) {
        super.loadAdditional(pTag);
        energyStorage.load(pTag);
    }


    public void serverTick() {
        if (hasEnergyStorage() && hasSoldier()) {
            energyStorage.generate(soldierData.getAdjustedSpeed());
        }
        if (hasEnergyStorage() && Math.abs(lastEnergySend - energyStorage.energyStored()) > 2) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new HamsterWheelEnergyPayload(energyStorage.energyStored(), getBlockPos()));
            lastEnergySend = energyStorage.energyStored();
            setChanged();
        }
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
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (HamsterWheelBlock.hasPowerConnection(state)) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), Items.REDSTONE.getDefaultInstance());
        }
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        String energyUnit = ClaySoldiersCommon.PLATFORM.getEnergyUnitName();
        list.add(getBlockState().getBlock().getName());
        addSoldierData(list, viewer);

        if (hasEnergyStorage()) {
            list.add(CommonComponents.space().append(
                    Component.translatable(StatInfoDisplay.ENERGY_LANG, energyStorage.energyStored() + energyUnit, energyStorage.maxEnergyStored() + energyUnit).withStyle(ChatFormatting.GRAY)
            ));
            list.add(CommonComponents.space().append(
                    Component.translatable(StatInfoDisplay.GENERATION_LANG, HamsterWheelEnergyStorage.energyGeneratedPerTick(soldierData == null ? 0 : soldierData.getAdjustedSpeed()) + energyUnit).withStyle(ChatFormatting.GRAY)
            ));
        }
    }
}
