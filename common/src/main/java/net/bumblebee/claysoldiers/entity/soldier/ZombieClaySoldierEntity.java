package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.IClayMobTeamReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        final ResourceLocation prevTeamId = ClayMobTeam.read(pCompound, "zombie");


        if (ClayMobTeamManger.isValidTeam(prevTeamId, registryAccess())) {
            setPreviousTeam(prevTeamId);
        } else {
            setClayTeamType(ClayMobTeamManger.DEFAULT_TYPE);
        }


        setCurable(pCompound.getBoolean(CURABLE_TAG));
        setCanPickItems(pCompound.getBoolean(PICK_ITEMS_TAG));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        ClayMobTeam.save(getPreviousTeamId(), pCompound, "zombie");
        pCompound.putBoolean(CURABLE_TAG, isCurable());
        pCompound.putBoolean(PICK_ITEMS_TAG, canPickItems());
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
    public boolean wantsToPickUp(ItemStack pStack) {
        return (canPickItems && super.wantsToPickUp(pStack)) || isItemStackHelm(pStack);
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
                    this.blockPosition(), MobSpawnType.CONVERSION, false, false);
            this.discard();
        }
    }

    @Override
    public void readItemPersistentData(CompoundTag tag) {
        if (tag.contains(CURABLE_TAG)) {
            setCurable(tag.getBoolean(CURABLE_TAG));
        }
        if (tag.contains(PICK_ITEMS_TAG)) {
            setCanPickItems(tag.getBoolean(PICK_ITEMS_TAG));
        }
    }

    public boolean canPickItems() {
        return canPickItems;
    }

    public void setCanPickItems(boolean canPickItems) {
        this.canPickItems = canPickItems;
    }

    @Override
    public void onConversion(ClayMobEntity oldSoldier, CompoundTag tag) {
        if (tag.contains(MATCH_TEAMS) && tag.getBoolean(MATCH_TEAMS)) {
            setPreviousTeam(oldSoldier.getClayTeamType());
        }
    }

    @Override
    public boolean isZombie() {
        return true;
    }
}
