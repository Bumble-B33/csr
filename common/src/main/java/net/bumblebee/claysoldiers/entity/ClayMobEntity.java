package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.entity.goal.UseAssignedPoiGoal;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusManager;
import net.bumblebee.claysoldiers.init.ModEffects;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoiWithItem;
import net.bumblebee.claysoldiers.team.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class ClayMobEntity extends PathfinderMob implements TeamHolder {
    public static final float DEFAULT_SCALE = 0.25f;
    protected static final double DEFAULT_ATTACK_REACH = 2.8f * DEFAULT_SCALE;
    private static final EntityDataAccessor<Boolean> SLIME_ROOT_SYNC = SynchedEntityData.defineId(ClayMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(ClayMobEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> HAS_POI_POS = SynchedEntityData.defineId(ClayMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> USING_POI = SynchedEntityData.defineId(ClayMobEntity.class, EntityDataSerializers.BOOLEAN);

    public static final String WORK_POI_LANG_KEY = "clay_mob_work.%s.poi.%s";
    public static final String WORK_POI_CLEARED_LANG = WORK_POI_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "cleared");
    public static final String WORK_POI_INVALID_LANG = WORK_POI_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "invalid");


    protected static final byte SITTING_FLAG = 1;
    private static final byte WAXED_FLAG = 2;

    public static final String SITTING_TAG = "Sitting";
    public static final String WAXED_TAG = "Waxed";
    public static final String POI_POS_TAG = "PoiPos";
    public static final String SPAWNED_FROM_TAG = "SpawnedFrom";
    public static final String DROP_SPAWNED_FROM_TAG = "DropSpawnedFrom";

    protected static final byte TEAM_CHANGE_EVENT = 77;
    protected static final byte SPAWN_HEARTS_EVENT = 78;
    protected static final byte SPAWN_ANGRY_EVENT = 79;
    protected static final byte SPAWN_HAPPY_EVENT = 80;

    protected DamageCalculator inWallDamage = (w, e) -> w ? 0.5f : 1;
    protected DamageCalculator ownerDamage = (w, e) -> 100;
    protected DamageCalculator otherPlayerDamage = (w, e) -> w ? 8 : 100;
    protected DamageCalculator explosionDamage = (w, e) -> w ? 0.4f : 0.5f;
    protected DamageCalculator clayDamage = (w, e) -> (float) getVisibilityPercent(e);
    protected DamageCalculator defaultDamage = (w, e) -> w ? 75 : 100;

    private final DamageSources clayDamageSources;
    private ItemStack spawnedFrom = ItemStack.EMPTY;
    private boolean dropSpawnedFrom = false;

    @Nullable
    private BlockPos poiPos = null;
    @Nullable
    private IBlockCache<AssignablePoiCapability> poiPosCapability;

    private boolean orderedToSit = false;

    @Nullable
    private IClayMobTeamReference cachedTeam = null;

    @Nullable
    public TeamPlayerData teamPlayerData = null;
    @Nullable
    private TeamPlayerData.PlayerData cachedTeamOwner = null;
    private long lastOwnerChange = -1;

    protected ClayMobEntity(EntityType<? extends ClayMobEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.clayDamageSources = ClaySoldiersCommon.PLATFORM.createClayDamageSources(pLevel.registryAccess());
        getPlayerTeamData(pLevel);
        setPersistenceRequired();
    }

    private void getPlayerTeamData(Level level) {
        if (teamPlayerData != null) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            teamPlayerData = TeamLoyaltyManger.getTeamPlayerData(serverLevel);
        } else {
            teamPlayerData = TeamLoyaltyManger.getClientTeamPlayerData();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLIME_ROOT_SYNC, false);
        builder.define(DATA_FLAGS_ID, (byte) 0);
        builder.define(HAS_POI_POS, false);
        builder.define(USING_POI, false);
    }

    /**
     * Returns whether this entity is currently slime rooted
     *
     * @return whether this entity is currently slime rooted
     */
    public boolean isSlimeRooted() {
        return entityData.get(SLIME_ROOT_SYNC);
    }

    /**
     * Set whether this entity is slime rooted
     */
    public void setSlimeRooted(boolean slimeRooted) {
        entityData.set(SLIME_ROOT_SYNC, slimeRooted);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
        super.onEffectAdded(pEffectInstance, pEntity);
        if (pEffectInstance.getEffect().is(ModEffects.SLIME_ROOT)) {
            if (this.getVehicle() instanceof LivingEntity livingVehicle) {
                livingVehicle.addEffect(pEffectInstance);
            }
            setSlimeRooted(true);
        }
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance pEffectInstance) {
        super.onEffectRemoved(pEffectInstance);
        if (pEffectInstance.getEffect().is(ModEffects.SLIME_ROOT)) {
            setSlimeRooted(false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (!spawnedFrom.isEmpty()) {
            pCompound.put(SPAWNED_FROM_TAG, spawnedFrom.save(this.registryAccess()));
            pCompound.putBoolean(DROP_SPAWNED_FROM_TAG, dropSpawnedFrom);
        }
        pCompound.putBoolean(SITTING_TAG, this.orderedToSit);
        pCompound.putBoolean(WAXED_TAG, this.isWaxed());
        if (getPoiPos() != null) {
            pCompound.put(POI_POS_TAG, NbtUtils.writeBlockPos(getPoiPos()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains(SPAWNED_FROM_TAG, Tag.TAG_COMPOUND)) {
            spawnedFrom = getSpawnedFromFromTag(pCompound, this.registryAccess());
            if (pCompound.contains(DROP_SPAWNED_FROM_TAG)) {
                dropSpawnedFrom = pCompound.getBoolean(DROP_SPAWNED_FROM_TAG);
            }
        }
        if (hasEffect(ModEffects.SLIME_ROOT)) {
            setSlimeRooted(true);
        }
        this.orderedToSit = pCompound.getBoolean(SITTING_TAG);
        this.setInSittingPose(this.orderedToSit);
        if (orderedToSit) {
            setPose(Pose.SITTING);
        }
        this.setWaxed(pCompound.getBoolean(WAXED_TAG));

        setPoiPos(NbtUtils.readBlockPos(pCompound, POI_POS_TAG).orElse(null));
    }

    public static ItemStack getSpawnedFromFromTag(CompoundTag tag, RegistryAccess registries) {
        return ItemStack.parseOptional(registries, tag.getCompound(SPAWNED_FROM_TAG));
    }

    @Override
    public DamageSources damageSources() {
        return this.clayDamageSources;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (amount == Float.MAX_VALUE || source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount);
        }
        if (source.is(DamageTypes.CRAMMING)) {
            return false;
        }
        if (sameTeamAs(source.getEntity()) && !getClayTeam().isFriendlyFireAllowed()) {
            return false;
        }
        float newDamage = amount;

        boolean waxed = isWaxed();
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            newDamage *= explosionDamage.calculate(waxed, source.getEntity());
        }

        if (source.is(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE) || (source.getEntity() instanceof ClayMobEntity)) {
            if (getVehicle() instanceof ClayMobRideableEntity rideableEntity) {
                rideableEntity.hurt(source, newDamage * rideableEntity.shareDamagePercent());
                newDamage *= (1 - rideableEntity.shareDamagePercent());
            }
            newDamage *= clayDamage.calculate(waxed, source.getEntity());
        } else {
            if (source.getEntity() instanceof Player player) {
                newDamage *= isOwnedBy(player) ? ownerDamage.calculate(waxed, player) : otherPlayerDamage.calculate(waxed, player);
            } else if (source.is(DamageTypes.IN_WALL)) {
                newDamage *= inWallDamage.calculate(waxed, source.getEntity());
            } else {
                newDamage *= defaultDamage.calculate(waxed, source.getEntity());
            }
        }
        return super.hurt(source, newDamage);
    }

    /**
     * Returns the custom armor value.
     *
     * @return the custom armor value
     */
    public float getCustomArmorValue() {
        return 0;
    }

    /**
     * Set the {@code ItemStack} this ClayMob was spawned from.
     *
     * @param spawnedFrom the {@code ItemStack} the clay-mab was spawned from
     * @param allowDropping whether {@code spawnedFrom} should be dropped on death
     */
    public void setSpawnedFrom(ItemStack spawnedFrom, boolean allowDropping) {
        this.spawnedFrom = spawnedFrom;
        this.dropSpawnedFrom = allowDropping;
    }

    public boolean dropSpawnedFrom() {
        return dropSpawnedFrom;
    }

    /**
     * Returns the {@code ItemStack} this ClayMob was spawned from.
     *
     * @return the {@code ItemStack} this ClayMob was spawned from
     */
    public ItemStack getSpawnedFrom() {
        return spawnedFrom;
    }

    @Override
    protected AABB getAttackBoundingBox() {
        Entity vehicle = this.getVehicle();
        AABB aabb;
        double attackReach = isSlimeRooted() ? getDefaultAttackReach() * 0.7D : getDefaultAttackReach();

        if (vehicle != null) {
            AABB aabb1 = vehicle.getBoundingBox();
            AABB aabb2 = this.getBoundingBox();
            aabb = new AABB(
                    Math.min(aabb2.minX, aabb1.minX),
                    aabb2.minY,
                    Math.min(aabb2.minZ, aabb1.minZ),
                    Math.max(aabb2.maxX, aabb1.maxX),
                    aabb2.maxY,
                    Math.max(aabb2.maxZ, aabb1.maxZ)
            );
        } else {
            aabb = this.getBoundingBox();
        }

        return aabb.inflate(attackReach, 0.0, attackReach);
    }

    /**
     * Returns the default attack reach.
     *
     * @return the default attack reach.
     */
    protected abstract double getDefaultAttackReach();

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource pSource, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(serverLevel, pSource, pRecentlyHit);
        if (!dropSpawnedFrom) {
            return;
        }
        dropSpawnedFrom(serverLevel, spawnedFrom, this::spawnAtLocation, pSource.getEntity() instanceof Player, isOnFire());
    }

    public static void dropSpawnedFrom(ServerLevel level, ItemStack spawnedFrom, Consumer<ItemStack> spawnInWorld, boolean alwaysDrop, boolean onFire) {
        float chance = alwaysDrop ? 1f : level.getLevelData().getGameRules().getInt(ClaySoldiersCommon.CLAY_SOLDIER_DROP_RULE) / 100f;

        if (chance >= level.random.nextFloat()) {
            if (onFire && spawnedFrom.getItem() instanceof BrickedItemHolder brickedItemHolder) {
                spawnInWorld.accept(brickedItemHolder.getBrickedItem(spawnedFrom));
            } else {
                spawnInWorld.accept(spawnedFrom);
            }
        }
    }

    /**
     * Returns whether this {@code ClayMob} can ride other {@code Entities}.
     *
     * @return whether this {@code ClayMob} can ride other {@code Entities}
     */
    public boolean isAbleToRide() {
        return false;
    }

    public boolean canMountEntity(LivingEntity livingEntity) {
        return false;
    }

    /**
     * Returns whether this ClayMob can be killed by a Clay Mob Kill Item.
     *
     * @return whether this ClayMob can be killed by a Clay Mob Kill Item
     */
    public boolean canBeKilledByItem() {
        return true;
    }

    /**
     * Spawns {@code ItemBreakParticles} of the given {@code ItemStack}.
     * Only used for spawning particles whe using an {@link SoldierPoiWithItem Poi}.
     *
     * @param pStack  the {@code ItemStack} to spawn particles of.
     * @param pAmount the amount of particles
     */
    public void spawnItemBreakParticles(ItemStack pStack, int pAmount) {
        if (pStack.isEmpty()) {
            return;
        }

        for (int i = 0; i < pAmount; ++i) {
            Vec3 vec3 = new Vec3(((double) this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
            vec3 = vec3.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
            double d0 = (double) (-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3 vec31 = new Vec3(((double) this.random.nextFloat() - 0.5) * 0.3, d0, 0.6);
            vec31 = vec31.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.add(this.getX(), this.getEyeY(), this.getZ());
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, pStack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05, vec3.z);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            // reduced death poof particles
            case 60 -> {
                return;
            }
            case SPAWN_HEARTS_EVENT -> {
                spawnParticleAround(ModParticles.SMALL_HEART_PARTICLE.get());
                return;
            }
            case SPAWN_ANGRY_EVENT -> {
                spawnParticleAround(ModParticles.SMALL_ANGRY_PARTICLE.get());
                return;
            }
            case SPAWN_HAPPY_EVENT -> {
                spawnParticleAround(ModParticles.SMALL_HAPPY_PARTICLE.get());
                return;
            }
        }
        super.handleEntityEvent(id);
    }

    @Override
    public boolean isInvisibleTo(Player pPlayer) {
        return false;
    }

    /**
     * Opens the menu associated with this {@code ClayMob} if there is one.
     *
     * @param player the team who opened the menu
     * @return whether the menu could be opened and with which id
     */
    protected OptionalInt openMenuScreen(Player player) {
        return OptionalInt.empty();
    }

    /**
     * Returns the inventory title for this associated inventory menu.
     */
    protected Component getInventoryName() {
        return getDisplayName();
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (ModItems.TEST_ITEM.is(pPlayer.getItemInHand(pHand))) {
            if (pHand == InteractionHand.MAIN_HAND) {
                TestItem.log(this, getInfoState());
            }

            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (pPlayer.isShiftKeyDown() && pPlayer.isCreative()) {
            if (!level().isClientSide) {
                if (openMenuScreen(pPlayer).isPresent()) {
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());

        }
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        var mode = ClayBrushItem.getMode(itemInHand);
        if (mode != null && isOwnedBy(pPlayer)) {
            clayBrushEffect(mode, itemInHand, pPlayer);
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isClayFood(itemInHand)) {
            this.heal(getMaxHealth());
            itemInHand.consume(1, pPlayer);
            if (level().isClientSide()) {
                this.playSound(SoundEvents.GENERIC_EAT, 1f ,1f);
                spawnParticleAround(ModParticles.SMALL_HEART_PARTICLE.get());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (itemInHand.is(ModTags.Items.CLAY_WAX)) {
            itemInHand.consume(1, pPlayer);
            setWaxed(true);
            if (level().isClientSide()) {
                this.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1f ,1f);
                spawnParticleAround(ModParticles.SMALL_WAXED_PARTICLE.get());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(pPlayer, pHand);
    }

    protected boolean isClayFood(ItemStack stack) {
        return stack.is(ModTags.Items.CLAY_FOOD);
    }

    /**
     * Called when this {@code ClayMob} is interacted with a {@code ClayBrush}.
     *
     * @return whether a command was successfully applied.
     */
    protected boolean clayBrushEffect(ClayBrushItem.Mode mode, ItemStack itemInHand, Player player) {
        if (mode == ClayBrushItem.Mode.COMMAND) {
            if (!level().isClientSide()) {
                tryToSit(player, !this.isOrderedToSit());
            }
            return true;
        }
        if (mode == ClayBrushItem.Mode.POI) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                setPoiPos(ClayBrushItem.getPoiPos(itemInHand));
                serverPlayer.sendSystemMessage(getPoiSetDisplayName(), true);
            }
            return true;
        }


        return false;
    }

    public Component getPoiSetDisplayName() {
        if (getPoiPos() == null) {
            return Component.translatable(WORK_POI_CLEARED_LANG);
        }
        BlockState blockState = level().getBlockState(getPoiPos());
        if (blockState.isAir()) {
            setPoiPos(null);
            return Component.translatable(WORK_POI_INVALID_LANG, Component.translatable(Blocks.AIR.getDescriptionId()));
        }
        return Component.translatable(blockState.getBlock().getDescriptionId()).append(" (" + getPoiPos().toShortString() + ")");
    }

    /**
     * Orders this {@code ClayMob} to sit.
     *
     * @param player the player giving the order or {@code null} if this order was not given from a player.
     * @param sitting whether to sit or get up
     */
    protected void tryToSit(@Nullable Player player, boolean sitting) {
        this.setOrderedToSit(sitting);
        this.jumping = false;
        this.navigation.stop();
        this.setTarget(null);
        if (getControllingPassenger() instanceof ClayMobEntity clayMob) {
            clayMob.tryToSit(null, sitting);
        }
    }

    /**
     * Spawns a few particles around this {@code ClayMob}.
     *
     * @param particleOptions the {@code Particle} to spawn.
     */
    protected void spawnParticleAround(ParticleOptions particleOptions) {
        for (int i = 0; i < 2; i++) {
            double xSpeed = this.random.nextGaussian() * 0.02;
            double ySpeed = this.random.nextGaussian() * 0.02;
            double zSpeed = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleOptions, this.getRandomX(0.5), this.getRandomY() + 0.25, this.getRandomZ(0.5), xSpeed, ySpeed, zSpeed);
        }
    }


    @Override
    public @NotNull ClayMobTeam getClayTeam() {
        ResourceLocation team = getClayTeamType();
        if (cachedTeam == null || !cachedTeam.isValidForKey(team)) {
            cachedTeam = ClayMobTeamManger.getReferenceOrDefault(team, registryAccess(), () -> {
                ClayMobTeamManger.LOGGER.error("{} has a Team ({}) that does not exist anymore", this.getClass().getSimpleName(), team);
                setClayTeamType(ClayMobTeamManger.DEFAULT_TYPE);
            });
        }

        return cachedTeam == null ? ClayMobTeamManger.ERROR : cachedTeam.value();
    }


    @Override
    public @Nullable UUID getClayTeamOwnerUUID() {
        return getCachedTeamOwner().map(TeamPlayerData.PlayerData::getUUID).orElse(null);
    }

    @Override
    public @Nullable Player getClayTeamOwner() {
        if (getClayTeamOwnerUUID() == null) {
            return null;
        }
        return level().getPlayerByUUID(getClayTeamOwnerUUID());
    }

    public Component getOwnerDisplayName() {
        return getCachedTeamOwner().map(TeamPlayerData.PlayerData::getLastDisplayName).orElse(null);
    }

    private Optional<TeamPlayerData.PlayerData> getCachedTeamOwner() {
        if (teamPlayerData == null) {
            getPlayerTeamData(level());
        }
        if (teamPlayerData != null) {
            if (lastOwnerChange < 0 || lastOwnerChange <= teamPlayerData.lastChangeTime()) {
                cachedTeamOwner = teamPlayerData.getPlayerForTeam(getClayTeamType());
                lastOwnerChange = teamPlayerData.lastChangeTime();
            }
        }
        return Optional.ofNullable(cachedTeamOwner);

    }

    @Override
    public boolean tryClaimingTeam(Player player) {
        if (!getClayTeam().canBeUsed(player)) {
            return false;
        }

        return teamPlayerData != null && teamPlayerData.putPlayerIfAbsent(getClayTeamType(), player);
    }

    @Override
    public boolean unableToMoveToOwner() {
        return TeamHolder.super.unableToMoveToOwner();
    }

    protected void setDataFlag(int pMask, boolean pValue) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        if (pValue) {
            i |= pMask;
        } else {
            i &= ~pMask;
        }

        this.entityData.set(DATA_FLAGS_ID, (byte) (i & 0xFF));
    }

    protected boolean getDataFlag(int pMask) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        return (i & pMask) != 0;
    }

    @Override
    public boolean isOrderedToSit() {
        if (getControllingPassenger() instanceof ClayMobEntity clayMob) {
            return clayMob.orderedToSit;
        }
        return orderedToSit;
    }

    @Override
    public void setOrderedToSit(boolean sit) {
        orderedToSit = sit;
    }

    @Override
    public boolean isInSittingPose() {
        return getDataFlag(SITTING_FLAG);
    }

    @Override
    public void setInSittingPose(boolean sitting) {
        setDataFlag(SITTING_FLAG, sitting);
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        if (spawnedFrom == null || spawnedFrom.isEmpty()) {
            return null;
        }
        var copy = spawnedFrom.copy();
        modifyPickResult(copy);
        return copy;
    }

    protected void modifyPickResult(ItemStack stack) {
    }

    /**
     * @return the current status of the ClayMob, returns {@code null} to indicate the ClayMob does not do anything special
     */
    @Nullable
    public Component getWorkStatus() {
        if (isInSittingPose()) {
            return Component.translatable(SoldierStatusManager.SITTING_LANG);
        }
        return usingPoi() ? Component.literal(SoldierStatusManager.USING_POI_LANG) : null;
    }

    /**
     * Tries to teleport this {@code ClayMob} to its owner.
     * @return whether the teleport was successful.
     */
    public boolean tryToTeleportToOwner() {
        LivingEntity livingentity = this.getClayTeamOwner();
        if (livingentity != null) {
            return this.teleportToAroundBlockPos(livingentity.blockPosition());
        }
        return false;
    }
    private boolean teleportToAroundBlockPos(BlockPos p_350657_) {
        for (int i = 0; i < 10; i++) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(p_350657_.getX() + j, p_350657_.getY() + l, p_350657_.getZ() + k)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        Mob moving = getControlledVehicle() instanceof Mob mob ? mob : this;
        if (!this.canTeleportTo(new BlockPos(x, y, z), moving)) {
            return false;
        } else {
            moving.teleportTo((double)x + 0.5, y, (double)z + 0.5);
            moving.getNavigation().stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos, Mob moving) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(moving, pos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = moving.level().getBlockState(pos.below());
            if (!this.canFlyToOwner() && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(moving.blockPosition());
                return moving.level().noCollision(moving, moving.getBoundingBox().move(blockpos));
            }
        }
    }

    /**
     * @return whether this ClayMob can fly to its owner
     */
    protected boolean canFlyToOwner() {
        if (getControlledVehicle() instanceof ClayMobEntity clayMob) {
            return clayMob.canFlyToOwner();
        }
        return false;
    }

    /**
     * @return whether this ClayMob should attempt to teleport to its owner
     */
    public boolean shouldTryTeleportToOwner() {
        return false;
    }

    /**
     * Called when this ClayMob spawns to send custom data to the client
     */
    public void sendSpawnPayload(ServerPlayer tracking) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        sendSpawnPayload(serverPlayer);
    }

    public void setWaxed(boolean waxed) {
        setDataFlag(WAXED_FLAG, waxed);
    }

    /**
     * @return whether this {@code ClayMob} is waxed.
     */
    public boolean isWaxed() {
        return getDataFlag(WAXED_FLAG);
    }

    /**
     * Returns the {@code BlockPos} of currently save point of interest of this {@code ClayMob}.
     * May be {@code null} to indicate there is no saved poi.
     */
    public @Nullable BlockPos getPoiPos() {
        return poiPos;
    }

    /**
     * Sets a new PoiPos.
     * @param pos the new PoiPos
     */
    public void setPoiPos(@Nullable BlockPos pos) {
        poiPos = pos;
        boolean hasPos = pos != null;
        entityData.set(HAS_POI_POS, hasPos);
        if (level() instanceof ServerLevel serverLevel) {
            poiPosCapability = hasPos ? UseAssignedPoiGoal.createCache(this, serverLevel) : null;
        }
    }

    public @Nullable AssignablePoiCapability getPoiCapability() {
        return poiPosCapability == null ? null : poiPosCapability.getCapability();
    }

    public void setUsingPoi(boolean usingPoi) {
        entityData.set(USING_POI, usingPoi);
    }

    /**
     * @return whether this {@code ClayMob} is currently using an {@code AssignablePoiCapability}.
     */
    public boolean usingPoi() {
        return entityData.get(USING_POI);
    }

    /**
     * @return whether this {@code ClayMob} has a PoiPos.
     */
    protected boolean hasPoiPos() {
        return entityData.get(HAS_POI_POS);
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof Player) {
            return;
        }
        super.doPush(entity);
    }


    @Override
    protected void playAttackSound() {
        this.playSound(SoundEvents.MUD_BRICKS_HIT, 0.3F, 1.0F);
    }

    public List<String> getInfoState() {
        List<String> info = new ArrayList<>(4);

        info.add("TeamPlayerData: " + teamPlayerData);
        info.add("CachedTeam: " + cachedTeam);
        info.add("Owner: " + (cachedTeamOwner == null ? "Null" : cachedTeamOwner));
        info.add("PoiPos: " + poiPos + " PoiCap: " + poiPosCapability);
        info.add("Active Goals:");
        goalSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).map(g -> g.getGoal().getClass().getSimpleName()).forEach(c -> info.add(" - " + c));

        return info;
    }

    @FunctionalInterface
    protected interface DamageCalculator {
        float calculate(boolean waxed, @Nullable Entity attacker);
    }
}


