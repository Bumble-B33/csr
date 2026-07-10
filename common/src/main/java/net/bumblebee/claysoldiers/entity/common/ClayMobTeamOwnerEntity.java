package net.bumblebee.claysoldiers.entity.common;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModEntitySerializers;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class ClayMobTeamOwnerEntity extends ClayMobEntity {
    private static final EntityDataAccessor<Holder.Reference<ClayMobTeam>> CLAY_MOB_TEAM = SynchedEntityData.defineId(ClayMobTeamOwnerEntity.class, ModEntitySerializers.CLAY_TEAM);

    @Nullable
    private Holder.Reference<ClayMobTeam> teamBeforeChange = null;

    protected ClayMobTeamOwnerEntity(EntityType<? extends ClayMobEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLAY_MOB_TEAM, ClayMobTeamManger.getDefault(level().registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        ClayMobTeam.read(input, level().registryAccess()).ifPresentOrElse(
                this::setClayTeamType,
                () -> ClaySoldiersCommon.ERROR_HANDLER.debug("%s was saved with a Team that does not exist anymore".formatted(this.getClass().getSimpleName())));
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        ClayMobTeam.store(getClayTeamHolder(), output);
    }

    @Override
    public @NonNull ResourceKey<ClayMobTeam> getClayTeamKey() {
        return getClayTeamHolder().key();
    }

    @Override
    public @NotNull Holder.Reference<ClayMobTeam> getClayTeamHolder() {
        return this.entityData.get(CLAY_MOB_TEAM);
    }

    @Override
    public void setClayTeamType(ResourceKey<ClayMobTeam> type) {
        level().registryAccess().get(type).ifPresent(this::setClayTeamType);
    }

    @Override
    public void setClayTeamType(Holder.Reference<ClayMobTeam> team) {
        if (team.is(getClayTeamHolder())) {
            return;
        }
        this.entityData.set(CLAY_MOB_TEAM, team);
        level().broadcastEntityEvent(this, TEAM_CHANGE_EVENT);
        handleTeamChange(team);

    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == TEAM_CHANGE_EVENT) {
            teamBeforeChange = getClayTeamHolder();
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();
        if (teamBeforeChange != null && level().isClientSide() && tickCount % 5 == 0) {
            if (!getClayTeamHolder().is(teamBeforeChange)) {
                handleTeamChange(teamBeforeChange);
                teamBeforeChange = null;
            }
        }
    }

    /**
     * Called when the team of this {@code ClayMobTeamOwner} changes.
     *
     * @param teamId the new team
     */
    protected abstract void handleTeamChange(Holder.Reference<ClayMobTeam> teamId);

    protected abstract boolean targetPredicate(LivingEntity other, ServerLevel serverLevel);
}
