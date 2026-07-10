package net.bumblebee.claysoldiers.block.soldiercontainer;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseBlockEntityWithSoldier extends BlockEntity implements ClayMobContainer {
    private final ClayMobHolder queue;
    private final int maxSpace;

    public BaseBlockEntityWithSoldier(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, ClayMobHolder queue, int maxSpace) {
        super(type, worldPosition, blockState);
        this.queue = queue;
        this.maxSpace = maxSpace;
    }

    public boolean hasSoldier() {
        return queue.size() > 0;
    }

    public boolean hasSpace() {
        return queue.size() < maxSpace;
    }

    protected abstract Vec3 getExitPosition();

    protected boolean addSoldier(AbstractClaySoldierEntity soldier, boolean force) {
        if (!hasSpace()) {
            if (force) {
                spawnSoldier(0);
            } else {
                return false;
            }
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
            OccupantSoldierData s = queue.remove();

            AbstractClaySoldierEntity soldier = s.createSoldier(serverLevel);
            soldier.snapTo(getExitPosition());
            soldier.setHealth(soldier.getMaxHealth());

            serverLevel.addFreshEntity(soldier);
            onUpdate(getBlockState(), getBlockState(), UpdateOperation.REMOVE, flags, queue.size());
        }
    }

    /**
     * Set the SoldierData. Does not sync data to the Client
     *
     * @param flags can be OR-ed
     *              <p>1 will write changes to disk</p>
     *              <p>2 will notify client</p>
     *              <p>4 will update POI Occupants</p>
     */
    protected void addSoldierData(OccupantSoldierData soldierData, int flags) {
        if (!hasSpace()) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Block already contains max amount of Soldiers");
        }
        queue.add(soldierData);
        onUpdate(getBlockState(), getBlockState(), UpdateOperation.ADD, flags, queue.size());
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

    protected abstract void updateOccupants(@Nullable Level level, UpdateOperation operation);

    public ClayMobContainer getClayMobContainer() {
        return this;
    }

    @Override
    public int killSoldier(ServerLevel level, ServerPlayer player) {
        int killed = 0;
        int skipped = 0;
        var it = queue.iterator();
        while (it.hasNext()) {
            OccupantSoldierData soldier = it.next();

            if (soldier.canBeKilledBy(level, player)) {
                Vec3 pos = getExitPosition();
                soldier.dropItems(level, pos.x, pos.y, pos.z);
                it.remove();
                killed++;
            } else {
                skipped++;
            }
        }
        onUpdate(getBlockState(), getBlockState(), UpdateOperation.UPDATE, 7, skipped);
        return killed;
    }

    protected void addSoldierDataView(List<Component> list, LivingEntity viewer, boolean withSpeed) {
        queue.forEach(s -> addSoldierDataView(s, list, viewer, true));
    }

    private static void addSoldierDataView(OccupantSoldierData soldier, List<Component> list, LivingEntity viewer, boolean withSpeed) {
        Holder.Reference<ClayMobTeam> team = ClayMobTeamManger.get(soldier.getTeamKey(), viewer.registryAccess()).orElse(null);
        list.add(CommonComponents.space()
                .append(team.value().getDisplayNameWithColor(c -> c.getColor(0, viewer.tickCount, 0)))
                .append(CommonComponents.space())
                .append(soldier.getTypeDescription()).withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (getLevel() instanceof ServerLevel serverLevel && hasSoldier()) {
            queue.forEach(s -> {
                AbstractClaySoldierEntity soldier = s.createSoldier(serverLevel);
                soldier.snapTo(getExitPosition());
                soldier.setHealth(soldier.getMaxHealth());

                serverLevel.addFreshEntity(soldier);
            });
            updateOccupants(serverLevel, UpdateOperation.UPDATE);
        }
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        try {
            updateOccupants(level, UpdateOperation.UPDATE);
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput tag) {
        super.saveAdditional(tag);
        queue.saveAdditional(tag, false);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        queue.clear();
        queue.loadAdditional(input);
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, pRegistries);
        queue.saveAdditional(output, true);

        return output.buildResult();
    }


    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected enum UpdateOperation {
        ADD,
        REMOVE,
        UPDATE;
    }

    protected interface ClayMobHolder extends Iterable<OccupantSoldierData> {
        void add(OccupantSoldierData data);

        OccupantSoldierData remove();

        int size();

        void clear();

        void saveAdditional(@NotNull ValueOutput tag, boolean client);

        void loadAdditional(@NotNull ValueInput tag);
    }
}