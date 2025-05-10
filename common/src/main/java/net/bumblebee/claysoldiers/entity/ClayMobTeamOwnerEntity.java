package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayMobSpawnPayload;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class ClayMobTeamOwnerEntity extends ClayMobEntity {
    private static final EntityDataAccessor<String> VARIANT_SYNC = SynchedEntityData.defineId(ClayMobTeamOwnerEntity.class, EntityDataSerializers.STRING);
    @Nullable
    private ResourceLocation teamBeforeChange = null;

    protected ClayMobTeamOwnerEntity(EntityType<? extends ClayMobEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT_SYNC, ClayMobTeamManger.DEFAULT_TYPE.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        final var team = ClayMobTeam.read(pCompound);
        if (!ClayMobTeamManger.isValidTeam(team, registryAccess())) {
            ClayMobTeamManger.LOGGER.error("{} was saved with a Team ({}) that does not exist anymore", this.getClass().getSimpleName(), team);
            setClayTeamType(ClayMobTeamManger.DEFAULT_TYPE);
        } else {
            setClayTeamType(team);
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        ClayMobTeam.save(getClayTeamType(), pCompound);
    }

    @Override
    public ResourceLocation getClayTeamType() {
        return ResourceLocation.parse(this.entityData.get(VARIANT_SYNC));
    }

    @Override
    public void setClayTeamType(ResourceLocation type) {
        if (getClayTeamType().equals(type)) {
            return;
        }
        ClayMobTeamManger.getOptional(type, registryAccess()).ifPresent(team -> {
            this.entityData.set(VARIANT_SYNC, type.toString());
            level().broadcastEntityEvent(this, TEAM_CHANGE_EVENT);
            handleTeamChange(type);
        });
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == TEAM_CHANGE_EVENT) {
            teamBeforeChange = getClayTeamType();
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();
        if (teamBeforeChange != null && level().isClientSide() && tickCount % 5 == 0) {
            var teamId = getClayTeamType();
            if (!teamId.equals(teamBeforeChange)) {
                teamBeforeChange = null;
                handleTeamChange(teamId);
            }
        }
    }

    /**
     * Called when the team of this {@code ClayMobTeamOwner} changes.
     *
     * @param teamId the new team
     */
    protected abstract void handleTeamChange(ResourceLocation teamId);

    @Override
    public void sendSpawnPayload(ServerPlayer tracking) {
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new ClayMobSpawnPayload(this));
    }

    protected abstract boolean targetPredicate(LivingEntity other);
}
