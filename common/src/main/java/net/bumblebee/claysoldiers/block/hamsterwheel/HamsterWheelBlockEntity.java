package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.networking.HamsterWheelEnergyPayload;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HamsterWheelBlockEntity extends BlockEntity implements ClayMobContainer {
    public static final Map<ResourceKey<Level>, Collection<String>> withSoldiers = new HashMap<>();
    private final WalkAnimationState walkAnimation = new WalkAnimationState();
    private final IHamsterWheelEnergyStorage energyStorage;
    private final AssignablePoiCapability poiCap = new AssignablePoiCapability() {
        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            return clayMob instanceof AbstractClaySoldierEntity soldier && soldier.getSoldierSize() <= 1.45f;
        }

        @Override
        public void use(ClayMobEntity clayMob) {
            if (clayMob instanceof AbstractClaySoldierEntity soldier) {
                addSoldier(soldier);
            } else {
                throw new IllegalArgumentException(clayMob + " cannot use this poi");
            }
        }
    };
    @Nullable
    private HamsterWheelSoldierData soldierData = null;
    private long lastEnergySend = 0;
    private int rotationTick = 0;

    public HamsterWheelBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), pPos, pBlockState);
        energyStorage = ClaySoldiersCommon.CAPABILITY_MANGER.createEnergyStorage(this);
    }


    public void clientTick(float partialTick) {
        if (hasSoldier()) {
            rotationTick += (int) Math.clamp(soldierData.getSpeed(), 1, 3);

            soldierData.getClientSoldier().tickCount++;

            float f = Math.min(partialTick * 4.0F, 1.0F);
            this.walkAnimation.update(f, 0.4F);
        }
    }

    public float getRotationTick(float partialTick) {
        return rotationTick + (hasSoldier() ? partialTick : 0);
    }

    public boolean hasSoldier() {
        return soldierData != null;
    }

    public @Nullable HamsterWheelSoldierData getSoldierData() {
        return soldierData;
    }

    private void addSoldier(AbstractClaySoldierEntity soldier) {
        spawnSoldier(0);

        soldier.stopRiding();
        soldier.ejectPassengers();
        soldier.dropCarried();

        setSoldierData(HamsterWheelSoldierData.of(soldier), 7);

        soldier.discard();
    }

    public AssignablePoiCapability getPoiCap() {
        return poiCap;
    }

    public @Nullable IHamsterWheelEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (hasEnergyStorage()) {
            if (direction == null) {
                return energyStorage.asViewOnly();
            }
            return direction.getOpposite() == getBlockState().getValue(HamsterWheelBlock.FACING) ? energyStorage : null;
        }
        return null;
    }

    public static @Nullable IHamsterWheelEnergyStorage getEnergyStorage(BlockEntity blockEntity, Direction context) {
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

    public void spawnSoldier(int flags) {
        if (hasSoldier()) {
            if (!getLevel().isClientSide) {
                AbstractClaySoldierEntity soldier = soldierData.createSoldier(getLevel());
                soldier.moveTo(getExitPosition());
                soldier.setHealth(soldier.getMaxHealth());


                getLevel().addFreshEntity(soldier);
            }
            setSoldierData(null, flags);
        }
    }

    public Vec3 getExitPosition() {
        BlockPos pos = worldPosition;
        Direction direction = getBlockState().getValue(HamsterWheelBlock.FACING);
        return new Vec3(pos.getX() + 0.5 + (direction.getStepX() * 0.3f), pos.getY(), pos.getZ() + 0.5 + (direction.getStepZ() * 0.3f));
    }

    /**
     * Set the SoldierData. Does not sync data to the Client
     *
     * @param flags can be OR-ed
     *              <p>1 will write changes to disk</p>
     *              <p>2 will notify client</p>
     *              <p>4 will update POI Occupants</p>
     */
    private void setSoldierData(@Nullable HamsterWheelSoldierData data, int flags) {
        soldierData = data;
        if ((flags & 1) != 0) {
            setChanged();
        }
        if ((flags & 2) != 0) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        if ((flags & 4) != 0) {
            setOccupant(getLevel(), data);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        try {
            setOccupant(level, soldierData);
        } catch (RuntimeException ignored) {}
    }

    private void setOccupant(@Nullable Level level, @Nullable HamsterWheelSoldierData data) {
        if (level instanceof ServerLevel serverLevel) {
            if (data == null) {
                serverLevel.getPoiManager().release(worldPosition);
            } else {
                serverLevel.getPoiManager().take(h -> h.is(ModTags.PoiTypes.SOLDIER_CONTAINER), (h, p) -> p.equals(worldPosition), worldPosition, 1);
            }
        }
    }

    @Override
    public void killSoldier(ServerLevel level, Player player) {
        if (soldierData == null) {
            return;
        }
        var owner = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(soldierData.getTeamId());
        if (owner == null || owner.is(player)) {
            Vec3 pos = getExitPosition();
            soldierData.dropItems(level, pos.x, pos.y, pos.z);
            setSoldierData(null, 7);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (soldierData != null) {
            soldierData.save(pTag);
        }
        energyStorage.save(pTag);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        setSoldierData(HamsterWheelSoldierData.load(pTag, getBlockPos(), walkAnimation, pRegistries), 0);
        energyStorage.load(pTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var tag = super.getUpdateTag(pRegistries);
        HamsterWheelSoldierData.markTagAsClient(tag);
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
}
