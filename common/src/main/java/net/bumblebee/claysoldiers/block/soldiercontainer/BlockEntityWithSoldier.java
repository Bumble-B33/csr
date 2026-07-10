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

import java.util.List;

public abstract class BlockEntityWithSoldier extends BlockEntity implements ClayMobContainer {
    private static final String SOLDIER_DATA_TAG = "SoldierData";
    @Nullable
    protected OccupantSoldierData soldierData;
    protected final WalkAnimationState walkAnimation;
    private final ResourceKey<PoiType> poiKey;


    protected BlockEntityWithSoldier(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, ResourceKey<PoiType> poiKey, PoiType poiType) {
        super(type, worldPosition, blockState);
        this.poiKey = poiKey;
        soldierData = null;
        walkAnimation = new WalkAnimationState();
    }

    public boolean hasSoldier() {
        return soldierData != null;
    }

    protected void addSoldier(AbstractClaySoldierEntity soldier) {
        spawnSoldier(0);

        soldier.stopRiding();
        soldier.ejectPassengers();
        soldier.enteredClayContainer();

        setSoldierData(OccupantSoldierData.of(soldier), 7);

        soldier.discard();
    }


    public @Nullable OccupantSoldierData getSoldierData() {
        return soldierData;
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

    protected abstract Vec3 getExitPosition();

    /**
     * Set the SoldierData. Does not sync data to the Client
     *
     * @param flags can be OR-ed
     *              <p>1 will write changes to disk</p>
     *              <p>2 will notify client</p>
     *              <p>4 will update POI Occupants</p>
     */
    protected void setSoldierData(@Nullable OccupantSoldierData data, int flags) {
        soldierData = data;
        onUpdate(getBlockState(), getBlockState(), data, flags);
    }


    protected void updateOccupants(@Nullable Level level, @Nullable OccupantSoldierData soldierData) {
        if (level instanceof ServerLevel serverLevel) {
            if (soldierData != null) {
                serverLevel.getPoiManager().take(s -> s.is(poiKey), (h, p) -> p.equals(worldPosition), worldPosition, 1);
            } else {
                serverLevel.getPoiManager().getType(worldPosition).ifPresent(t -> {
                    serverLevel.getPoiManager().release(worldPosition);
                });
            }
        }
    }

    protected void onUpdate(BlockState oldState, BlockState newState, @Nullable OccupantSoldierData data, int flags) {
        if ((flags & 1) != 0) {
            setChanged();
        }
        if ((flags & 2) != 0) {
            getLevel().sendBlockUpdated(worldPosition, oldState, newState, 2);
        }
        if ((flags & 4) != 0) {
            updateOccupants(getLevel(), data);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (getLevel() instanceof ServerLevel serverLevel && hasSoldier()) {
            AbstractClaySoldierEntity soldier = soldierData.createSoldier(serverLevel);
            soldier.snapTo(getExitPosition());
            soldier.setHealth(soldier.getMaxHealth());

            serverLevel.addFreshEntity(soldier);

            updateOccupants(serverLevel, null);
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput tag) {
        super.saveAdditional(tag);
        if (soldierData != null) {
            tag.store(SOLDIER_DATA_TAG, OccupantSoldierData.CODEC, soldierData.compress(false));
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput tag) {
        super.loadAdditional(tag);
        setSoldierData(tag.read(SOLDIER_DATA_TAG, OccupantSoldierData.CODEC)
                .flatMap(s -> s.build(getBlockPos(), walkAnimation, tag.lookup()))
                .orElse(null), 0);

    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        try {
            updateOccupants(level, soldierData);
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    public final CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, pRegistries);
        if (soldierData != null) {
            output.store(SOLDIER_DATA_TAG, OccupantSoldierData.CODEC, soldierData.compress(true));
        }
        return output.buildResult();
    }

    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int killSoldier(ServerLevel level, ServerPlayer player) {
        if (soldierData == null) {
            return 0;
        }
        if (soldierData.canBeKilledBy(level, player)) {
            Vec3 pos = getExitPosition();
            soldierData.dropItems(level, pos.x, pos.y, pos.z);
            setSoldierData(null, 7);
            return 1;
        }
        return 0;
    }

    public ClayMobContainer getClayMobContainer() {
        return this;
    }

    protected void addSoldierData(List<Component> list, LivingEntity viewer) {
        if (soldierData != null) {
            addSoldierData(soldierData, list, viewer, true);
        }
    }

    public static void addSoldierData(OccupantSoldierData soldier, List<Component> list, LivingEntity viewer, boolean withSpeed) {
        Holder.Reference<ClayMobTeam> team = ClayMobTeamManger.get(soldier.getTeamKey(), viewer.registryAccess()).orElse(null);
        list.add(CommonComponents.space()
                .append(team.value().getDisplayNameWithColor(c -> c.getColor(0, viewer.tickCount, 0)))
                .append(CommonComponents.space())
                .append(soldier.getTypeDescription()).withStyle(ChatFormatting.GRAY)
        );
    }
}
