package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.IClayMobTeamReference;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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

public class ZombieClaySoldierEntity extends UndeadClaySoldier {
    private static final EntityDataAccessor<String> PREVIOUS_TEAM_SYNC = SynchedEntityData.defineId(ZombieClaySoldierEntity.class, EntityDataSerializers.STRING);
    public static final String CURABLE_TAG = "Curable";
    public static final String PICK_ITEMS_TAG = "PickUpItems";
    public static final String MATCH_TEAMS = "match_teams";
    private boolean curable = true;
    private boolean canPickItems = false;

    private IClayMobTeamReference cachedPrevTeam = null;


    public ZombieClaySoldierEntity(EntityType<? extends ZombieClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.ZOMBIE);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        final ResourceLocation prevTeamId = ClayMobTeam.read(input, "zombie");


        if (ClayMobTeamManger.isValidTeam(prevTeamId, registryAccess())) {
            setPreviousTeam(prevTeamId);
        } else {
            setClayTeamType(ClayMobTeamManger.DEFAULT_TYPE);
        }


        setCurable(input.getBooleanOr(CURABLE_TAG, false));
        setCanPickItems(input.getBooleanOr(PICK_ITEMS_TAG, false));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ClayMobTeam.save(getPreviousTeamId(), output, "zombie");
        output.putBoolean(CURABLE_TAG, isCurable());
        output.putBoolean(PICK_ITEMS_TAG, canPickItems());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PREVIOUS_TEAM_SYNC, ClayMobTeamManger.DEFAULT_TYPE.toString());
    }

    @NotNull
    public ClayMobTeam getPreviousTeam() {
        var prevKey = getPreviousTeamId();
        if (cachedPrevTeam == null || !cachedPrevTeam.isValidForKey(prevKey)) {
            cachedPrevTeam = ClayMobTeamManger.getReferenceOrDefault(prevKey, registryAccess(),() -> {
                setPreviousTeam(ClayMobTeamManger.NO_TEAM_TYPE);
                ClayMobTeamManger.LOGGER.error("{} has a Previous Team ({}) that does not exist anymore", this.getClass().getSimpleName(), prevKey);
            });
        }
        return cachedPrevTeam == null ? ClayMobTeamManger.ERROR : cachedPrevTeam.value();
    }

    public ResourceLocation getPreviousTeamId() {
        return ResourceLocation.parse(entityData.get(PREVIOUS_TEAM_SYNC));
    }

    public void setPreviousTeam(ResourceLocation variant) {
        this.entityData.set(PREVIOUS_TEAM_SYNC, variant.toString());
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
        return getPreviousTeamId().equals(claySoldier.getClayTeamType());
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
                        curedSoldier.setClayTeamType(getPreviousTeamId());
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
            setPreviousTeam(oldSoldier.getClayTeamType());
        }
    }

    @Override
    public boolean isZombie() {
        return true;
    }
}
