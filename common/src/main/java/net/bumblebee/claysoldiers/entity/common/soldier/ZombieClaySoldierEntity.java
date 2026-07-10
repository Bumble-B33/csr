package net.bumblebee.claysoldiers.entity.common.soldier;

import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModEntitySerializers;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ZombieClaySoldierEntity extends UndeadClaySoldier {
    private static final EntityDataAccessor<Holder.Reference<ClayMobTeam>> PREVIOUS_TEAM_SYNC = SynchedEntityData.defineId(ZombieClaySoldierEntity.class, ModEntitySerializers.CLAY_TEAM);
    public static final String CURABLE_TAG = "Curable";
    public static final String PICK_ITEMS_TAG = "PickUpItems";
    public static final String MATCH_TEAMS = "match_teams";
    private boolean curable = true;
    private boolean canPickItems = false;

    public ZombieClaySoldierEntity(EntityType<? extends ZombieClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.ZOMBIE);
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        ClayMobTeam.read(input, "zombie", level().registryAccess()).ifPresent(this::setPreviousTeam);

        setCurable(input.getBooleanOr(CURABLE_TAG, false));
        setCanPickItems(input.getBooleanOr(PICK_ITEMS_TAG, false));
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        ClayMobTeam.store(getClayTeamHolder(), output, "zombie");
        output.putBoolean(CURABLE_TAG, isCurable());
        output.putBoolean(PICK_ITEMS_TAG, canPickItems());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PREVIOUS_TEAM_SYNC, ClayMobTeamManger.getDefault(level().registryAccess()));
    }

    @NotNull
    public ClayMobTeam getPreviousTeam() {
        return getPreviousHolder().value();
    }

    @NotNull
    public Holder.Reference<ClayMobTeam> getPreviousHolder() {
        return entityData.get(PREVIOUS_TEAM_SYNC);
    }

    public void setPreviousTeam(Holder.Reference<ClayMobTeam> variant) {
        this.entityData.set(PREVIOUS_TEAM_SYNC, variant);
    }

    @Override
    public boolean isAbleToRide() {
        return false;
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack pStack) {
        return (canPickItems && super.wantsToPickUp(level, pStack)) || isItemStackHelm(pStack);
    }

    /**
     * Returns whether the previous team of this zombie-soldier is the same as the given soldier.
     *
     * @param claySoldier the other soldier
     */
    public boolean previousTeamSameAs(ClayMobEntity claySoldier) {
        if (!getPreviousTeam().isCooperative() || claySoldier.hasNoTeam()) {
            return false;
        }
        return getPreviousHolder().is(claySoldier.getClayTeamHolder());
    }

    /**
     * Returns whether this soldier can be cured.
     */
    public boolean isCurable() {
        return curable;
    }

    /**
     * Sets whether this soldier can be cured.
     */
    public void setCurable(boolean curable) {
        this.curable = curable;
    }

    /**
     * Cures this soldier if it is curable.
     * The spawning soldiers team will be the original team from this zombie
     */
    public void cureZombieSoldier() {
        if (!curable) {
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            ModEntityTypes.CLAY_SOLDIER_ENTITY.get().spawn(serverLevel,
                    curedSoldier -> {
                        copyInventory(curedSoldier);
                        curedSoldier.setClayTeamType(getClayTeamHolder());
                    },
                    this.blockPosition(), EntitySpawnReason.CONVERSION, false, false);
            this.discard();
        }
    }

    @Override
    public void readItemPersistentData(ValueInput tag) {
        setCurable(tag.getBooleanOr(CURABLE_TAG, false));
        setCanPickItems(tag.getBooleanOr(PICK_ITEMS_TAG, false));
    }

    public boolean canPickItems() {
        return canPickItems;
    }

    public void setCanPickItems(boolean canPickItems) {
        this.canPickItems = canPickItems;
    }

    @Override
    public void onConversion(ClayMobEntity oldSoldier, ValueInput tag, @Nullable Player player) {
        if (tag.getBooleanOr(MATCH_TEAMS, false)) {
            setPreviousTeam(oldSoldier.getClayTeamHolder());
        }
    }

    @Override
    public boolean isZombie() {
        return true;
    }
}
