package net.bumblebee.claysoldiers.block.soldiercontainer;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class BlockEntityWithSoldiers extends BlockEntity implements ClayMobContainer {
    private static final String SOLDIER_DATA_TAG = "SoldierData";
    private static final Codec<Stream<OccupantSoldierData.Compressed>> SOLDIER_CODEC = OccupantSoldierData.CODEC.listOf().xmap(List::stream, Stream::toList);
    protected final Queue<OccupantSoldierData> soldierData;
    protected final WalkAnimationState walkAnimation;
    private final ResourceKey<PoiType> poiKey;
    private final int maxSpace;


    protected BlockEntityWithSoldiers(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, ResourceKey<PoiType> poiKey, PoiType poiType) {
        super(type, worldPosition, blockState);
        this.poiKey = poiKey;
        this.maxSpace = poiType.maxTickets();
        this.soldierData = new ArrayDeque<>(maxSpace);
        if (maxSpace == 0) {
            throw new IllegalStateException("Cannot have a Container with not Space");
        }
        walkAnimation = new WalkAnimationState();
    }

    public boolean hasSoldier() {
        return !soldierData.isEmpty();
    }

    public boolean hasSpace() {
        return soldierData.size() < maxSpace;
    }

    protected boolean addSoldier(AbstractClaySoldierEntity soldier) {
        if (!hasSpace()) {
            return false;
        }
        soldier.stopRiding();
        soldier.ejectPassengers();
        soldier.enteredClayContainer();

        addSoldierData(OccupantSoldierData.of(soldier), 7);

        soldier.discard();
        return true;
    }

    public void spawnSoldier(int flags) {
        if (getLevel() instanceof ServerLevel serverLevel && hasSoldier()) {
            OccupantSoldierData s = soldierData.remove();

            AbstractClaySoldierEntity soldier = s.createSoldier(serverLevel);
            soldier.snapTo(getExitPosition());
            soldier.setHealth(soldier.getMaxHealth());

            serverLevel.addFreshEntity(soldier);
            onUpdate(getBlockState(), getBlockState(), UpdateOperation.REMOVE, flags, soldierData.size());
        }
    }

    protected abstract Vec3 getExitPosition();

    /**
     * Set the SoldierData. Does not sync data to the Client
     *
     * @param flags can be OR-ed
     *              <p>1 will write changes to disk</p>
     *              <p>2 will notify client</p>
     *              <p>4 will update POI Occupants</p>
     */
    private void addSoldierData(@NotNull OccupantSoldierData data, int flags) {
        if (!hasSpace()) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Block already contains max amount of Soldiers");
        }
        soldierData.add(data);
        onUpdate(getBlockState(), getBlockState(), UpdateOperation.ADD, flags, soldierData.size());
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        try {
            updateOccupants(level, UpdateOperation.UPDATE);
        } catch (RuntimeException ignored) {
        }
    }

    protected void onUpdate(BlockState oldState, BlockState newState, UpdateOperation operation, int flags, int currentSoldiers) {
        if ((flags & 1) != 0) {
            setChanged();
        }
        if ((flags & 2) != 0) {
            getLevel().sendBlockUpdated(worldPosition, oldState, newState, 2);
        }
        if ((flags & 4) != 0) {
            updateOccupants(getLevel(), operation);
        }
    }

    private void updateOccupants(@Nullable Level level, UpdateOperation operation) {

    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (getLevel() instanceof ServerLevel serverLevel && hasSoldier()) {
            soldierData.forEach(s -> {
                AbstractClaySoldierEntity soldier = s.createSoldier(serverLevel);
                soldier.snapTo(getExitPosition());
                soldier.setHealth(soldier.getMaxHealth());

                serverLevel.addFreshEntity(soldier);
            });
            soldierData.clear();
            updateOccupants(serverLevel, UpdateOperation.UPDATE);
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput tag) {
        super.saveAdditional(tag);
        tag.store(SOLDIER_DATA_TAG, SOLDIER_CODEC, soldierData.stream().map(s -> s.compress(false)));

    }

    @Override
    protected void loadAdditional(@NotNull ValueInput tag) {
        super.loadAdditional(tag);
        soldierData.clear();
        tag.read(SOLDIER_DATA_TAG, SOLDIER_CODEC).orElse(Stream.of()).map(s -> s.build(worldPosition, walkAnimation, tag.lookup())).forEach(s -> s.ifPresent(soldierData::add));
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, pRegistries);
        output.store(SOLDIER_DATA_TAG, SOLDIER_CODEC, soldierData.stream().map(s -> s.compress(true)));

        return output.buildResult();
    }

    @Override
    public int killSoldier(ServerLevel level, ServerPlayer player) {
        int sizeBefore = soldierData.size();
        var it = soldierData.iterator();
        while (it.hasNext()) {
            OccupantSoldierData soldier = it.next();

            if (soldier.canBeKilledBy(level, player)) {
                Vec3 pos = getExitPosition();
                soldier.dropItems(level, pos.x, pos.y, pos.z);
                it.remove();

            }
        }
        onUpdate(getBlockState(), getBlockState(), UpdateOperation.UPDATE, 7, soldierData.size());
        return sizeBefore - soldierData.size();
    }


    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ClayMobContainer getClayMobContainer() {
        return this;
    }

    protected void addSoldierData(List<Component> list, LivingEntity viewer) {
        soldierData.forEach(s -> BlockEntityWithSoldier.addSoldierData(s, list, viewer, false));
    }

    protected enum UpdateOperation {
        ADD,
        REMOVE,
        UPDATE;
    }
}
