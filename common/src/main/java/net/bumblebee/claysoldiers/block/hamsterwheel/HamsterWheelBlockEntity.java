package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.networking.HamsterWheelEnergyPayload;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HamsterWheelBlockEntity extends BlockEntity implements ClayMobContainer, StatInfoDisplay {
    public static final Identifier WORKSITE_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel");
    private final WalkAnimationState walkAnimation = new WalkAnimationState();
    private final HamsterWheelEnergyStorage energyStorage;
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
    @Nullable
    private HamsterWheelSoldierData soldierData = null;
    private long lastEnergySend = 0;
    private int rotationTick = 0;

    public HamsterWheelBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), pPos, pBlockState);
        energyStorage = ClaySoldiersCommon.CAPABILITY_MANGER.createEnergyStorage(this);
    }


    public void clientTick() {
        if (hasSoldier()) {
            rotationTick += (int) Math.clamp(soldierData.getSpeed(), 1, 3);

            soldierData.getClientSoldier().tickCount++;

            walkAnimation.update(0.75f, 0.4F, 1f);
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
        soldier.enteredHamsterWheel();

        setSoldierData(HamsterWheelSoldierData.of(soldier), 7);

        soldier.discard();
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

    public void spawnSoldier(int flags) {
        if (hasSoldier()) {
            if ((getLevel() instanceof ServerLevel serverLevel)) {
                AbstractClaySoldierEntity soldier = soldierData.createSoldier(serverLevel);
                soldier.snapTo(getExitPosition());
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
    public void killSoldier(ServerLevel level, ServerPlayer player) {
        if (soldierData == null) {
            return;
        }
        var owner = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(soldierData.getTeamKey());
        if (owner == null || owner.is(player)) {
            Vec3 pos = getExitPosition();
            soldierData.dropItems(level, pos.x, pos.y, pos.z);
            setSoldierData(null, 7);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public boolean canKillClayMob(ServerLevel level, ServerPlayer player) {
        if (soldierData == null) {
            return false;
        }
        var owner = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(soldierData.getTeamKey());
        return owner == null || owner.is(player);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput pTag) {
        super.saveAdditional(pTag);
        if (soldierData != null) {
            soldierData.save(pTag, hasLevel() && getLevel().isClientSide());
        }
        energyStorage.save(pTag);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput pTag) {
        super.loadAdditional(pTag);
        setSoldierData(HamsterWheelSoldierData.load(pTag, getBlockPos(), walkAnimation, level == null ? null : level.registryAccess()), 0);
        energyStorage.load(pTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, pRegistries);
        HamsterWheelSoldierData.markTagAsClient(output);
        this.saveWithoutMetadata(output);
        return output.buildResult();
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity hamsterWheelBlockEntity) {
            hamsterWheelBlockEntity.spawnSoldier(0);
        }
        if (HamsterWheelBlock.hasPowerConnection(state)) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), Items.REDSTONE.getDefaultInstance());
        }
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        String energyUnit = ClaySoldiersCommon.PLATFORM.getEnergyUnitName();
        list.add(getBlockState().getBlock().getName());
        if (hasSoldier()) {
            var team = ClayMobTeamManger.get(soldierData.getTeamKey(), level.registryAccess()).orElse(null);
            list.add(CommonComponents.space().append(
                    team.value().getDisplayNameWithColor(c -> c.getColor(0, viewer.tickCount, 0))
            ).append(CommonComponents.space())
                            .append(ModEntityTypes.CLAY_SOLDIER_ENTITY.get().getDescription()).withStyle(ChatFormatting.GRAY)
            );
            list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.SPEED_LANG, soldierData.getSpeed()).withStyle(ChatFormatting.GRAY)));
        }

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
