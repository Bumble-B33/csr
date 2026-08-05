package net.bumblebee.claysoldiers.entity.common;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.entity.ClayDamageSources;
import net.bumblebee.claysoldiers.entity.common.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.entity.goal.UseAssignedPoiGoal;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoiWithItem;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.OwnerQuery;
import net.bumblebee.claysoldiers.team.TeamHolder;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamPlayerData;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;

public abstract class ClayMobEntity extends PathfinderMob implements TeamHolder, StatInfoDisplay {
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
    protected static final byte FOLLOW_OWNER_FLAG = 4;


    public static final String SITTING_TAG = "Sitting";
    public static final String WAXED_TAG = "Waxed";
    public static final String POI_POS_TAG = "PoiPos";
    public static final String SPAWNED_FROM_TAG = "SpawnedFrom";
    public static final String DROP_SPAWNED_FROM_TAG = "DropSpawnedFrom";

    protected static final byte TEAM_CHANGE_EVENT = 77;
    protected static final byte SPAWN_HEARTS_EVENT = 78;
    protected static final byte SPAWN_ANGRY_EVENT = 79;
    protected static final byte SPAWN_HAPPY_EVENT = 80;

    protected DamageCalculator inWallDamage = (w, _) -> w ? 0.5f : 1;
    protected DamageCalculator ownerDamage = (_, _) -> 100;
    protected DamageCalculator otherPlayerDamage = (w, _) -> w ? 8 : 100;
    protected DamageCalculator explosionDamage = (w, _) -> w ? 0.4f : 0.5f;
    protected DamageCalculator clayDamage = (_, e) -> (float) getVisibilityPercent(e);
    protected DamageCalculator defaultDamage = (w, _) -> w ? 75 : 100;

    private final DamageSources clayDamageSources;
    private ItemStack spawnedFrom = ItemStack.EMPTY;
    private boolean dropSpawnedFrom = false;

    @NotNull
    private PoiPosInfo poiInfo = PoiPosInfo.EMPTY;
    @Nullable
    private IBlockCache<AssignableWorksiteCapability> poiPosCapability;

    @NotNull
    protected OrderedCommand orderedCommand = OrderedCommand.FOLLOW_OWNER;

    @Nullable
    public TeamPlayerData teamPlayerData = null;
    @Nullable
    private TeamPlayerData.PlayerData cachedTeamOwner = null;
    private long lastOwnerChange = -1;

    protected ClayMobEntity(EntityType<? extends ClayMobEntity> entityType, Level level) {
        super(entityType, level);
        this.clayDamageSources = new ClayDamageSources(level.registryAccess());
        setPlayerTeamData(level);
        setPersistenceRequired();
    }

    private void setPlayerTeamData(Level level) {
        if (teamPlayerData != null) {
            return;
        }
        teamPlayerData = TeamLoyaltyManger.getTeamPlayerData(level);
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
    protected void onEffectsRemoved(Collection<MobEffectInstance> effects) {
        super.onEffectsRemoved(effects);
        if (effects.stream().anyMatch(e -> e.is(ModEffects.SLIME_ROOT))) {
            setSlimeRooted(false);
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (!spawnedFrom.isEmpty()) {
            output.store(SPAWNED_FROM_TAG, ItemStack.CODEC, spawnedFrom);
            output.putBoolean(DROP_SPAWNED_FROM_TAG, dropSpawnedFrom);
        }
        output.store(SITTING_TAG, OrderedCommand.CODEC, this.orderedCommand);
        output.putBoolean(WAXED_TAG, this.isWaxed());
        output.storeNullable(POI_POS_TAG, PoiPosInfo.CODEC, getPoiInfo());
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        getSpawnedFromFromTag(input).ifPresent(stack -> {
            spawnedFrom = stack;
            dropSpawnedFrom = input.getBooleanOr(DROP_SPAWNED_FROM_TAG, false);
        });
        if (hasEffect(ModEffects.SLIME_ROOT)) {
            setSlimeRooted(true);
        }
        this.setOrderedCommand(input.read(SITTING_TAG, OrderedCommand.CODEC).orElse(OrderedCommand.FOLLOW_OWNER));

        this.setInSittingPose(this.orderedCommand == OrderedCommand.SITTING);

        if (this.orderedCommand == OrderedCommand.SITTING) {
            setPose(Pose.SITTING);
        }
        this.setWaxed(input.getBooleanOr(WAXED_TAG, false));
        setPoiInfo(input.read(POI_POS_TAG, PoiPosInfo.CODEC).orElse(PoiPosInfo.EMPTY));
    }

    public static Optional<ItemStack> getSpawnedFromFromTag(ValueInput tag) {
        return tag.read(SPAWNED_FROM_TAG, ItemStack.CODEC);
    }

    @Override
    public DamageSources damageSources() {
        return this.clayDamageSources;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (amount == Float.MAX_VALUE || source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(serverLevel, source, amount);
        }
        if (source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.CACTUS)) {
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
        return super.hurtServer(serverLevel, source, newDamage);
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return true;
        }
        if (source.is(DamageTypes.CRAMMING)) {
            return false;
        }
        if (sameTeamAs(source.getEntity()) && !getClayTeam().isFriendlyFireAllowed()) {
            return false;
        }
        return super.hurtClient(source);
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
     * @param spawnedFrom   the {@code ItemStack} the ClayMob was spawned from
     * @param allowDropping whether {@code spawnedFrom} should be dropped on death
     */
    public void setSpawnedFrom(ItemStack spawnedFrom, boolean allowDropping) {
        this.spawnedFrom = spawnedFrom;
        this.dropSpawnedFrom = allowDropping;
    }

    public boolean shouldDropSpawnedFrom() {
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
    protected AABB getAttackBoundingBox(double horizontalExpansion) {
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
        dropSpawnedFrom(serverLevel, spawnedFrom, stack -> spawnAtLocation(serverLevel, stack), pSource.getEntity() instanceof Player, isOnFire());
    }

    public static void dropSpawnedFrom(ServerLevel level, ItemStack spawnedFrom, Consumer<ItemStack> spawnInWorld, boolean alwaysDrop, boolean onFire) {
        float chance = alwaysDrop ? 1f : ClaySoldiersCommon.CONFIG.getServerConfig().soldierDropSelf();

        if (chance >= level.getRandom().nextFloat()) {
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

    public boolean canMountEntity(LivingEntity mount, ServerLevel serverLevel) {
        return false;
    }

    /**
     * Returns whether this ClayMob can be killed by a Clay Mob Kill Item.
     *
     * @return whether this ClayMob can be killed by a Clay Mob Kill Item
     */
    public final boolean canBeKilledByDisruptor(ServerPlayer player) {
        return player.getUUID().equals(getClayTeamOwnerUUID());
    }

    public OwnerQuery createOwnerQuery() {
        return OwnerQuery.team(getClayTeamHolder());
    }

    /**
     * Spawns {@code ItemBreakParticles} of the given {@code ItemStack}.
     * Only used for spawning particles whe using an {@link SoldierPoiWithItem Poi}.
     *
     * @param pStack  the {@code ItemStack} to spawn particles of.
     * @param pAmount the amountRequired of particles
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
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, pStack.getItem()), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05, vec3.z);
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

            return InteractionResult.SUCCESS;
        }

        if (pPlayer.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                if (openMenuScreen(pPlayer).isPresent()) {
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            return InteractionResult.CONSUME;

        }
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);

        var mode = ClayBrushItem.getMode(itemInHand);
        if (mode != null && isOwnedBy(pPlayer)) {
            clayBrushEffect(mode, itemInHand, pPlayer);
            return InteractionResult.SUCCESS;
        }

        if (isClayFood(itemInHand)) {

            if (level().isClientSide()) {
                this.playSound(SoundEvents.GENERIC_EAT.value(), 1f, 1f);
                spawnParticleAround(ModParticles.SMALL_HEART_PARTICLE.get());
            } else if (pPlayer instanceof ServerPlayer serverPlayer) {
                ModCritirions.FEED_CLAY_SOLDIER_TRIGGER.get().triggerFood(serverPlayer, itemInHand);
            }
            this.heal(getMaxHealth());
            itemInHand.consume(1, pPlayer);

            return InteractionResult.SUCCESS;
        }

        if (itemInHand.is(ModTags.Items.CLAY_WAX)) {
            setWaxed(true);
            if (level().isClientSide()) {
                this.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1f, 1f);
                spawnParticleAround(ModParticles.SMALL_WAXED_PARTICLE.get());
            } else if (pPlayer instanceof ServerPlayer serverPlayer) {
                ModCritirions.FEED_CLAY_SOLDIER_TRIGGER.get().triggerWax(serverPlayer, itemInHand);
            }
            itemInHand.consume(1, pPlayer);

            return InteractionResult.SUCCESS;
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
                var command = cycleOrderedCommand();
                tryToSit(player, cycleOrderedCommand());
                if (player instanceof ServerPlayer serverPlayer) {
                    ModCritirions.CLAY_BRUSH_COMMAND_TRIGGER.get().trigger(serverPlayer, mode);
                    serverPlayer.sendSystemMessage(command.getDisplayName(), true);
                }
            }

            return true;
        }
        if (mode == ClayBrushItem.Mode.POI) {
            if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                setPoiInfo(ClayBrushItem.getPoiPos(itemInHand));
                if (!poiInfo.isEmpty()) {
                    ModCritirions.CLAY_BRUSH_COMMAND_TRIGGER.get().trigger(serverPlayer, mode);
                }
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
            setPoiInfo(PoiPosInfo.EMPTY);
            return Component.translatable(WORK_POI_INVALID_LANG, Component.translatable(Blocks.AIR.getDescriptionId()));
        }
        return Component.translatable(blockState.getBlock().getDescriptionId()).append(" (" + getPoiPos().toShortString() + ")");
    }

    /**
     * Orders this {@code ClayMob} to sit.
     *
     * @param player  the player giving the order or {@code null} if this order was not given from a player.
     * @param sitting whether to sit or get up
     */
    protected void tryToSit(@Nullable Player player, OrderedCommand sitting) {
        this.setOrderedCommand(sitting);
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
    public @Nullable UUID getClayTeamOwnerUUID() {
        return getCachedTeamOwner().map(TeamPlayerData.PlayerData::getUUID).orElse(null);
    }

    @Nullable
    public Component getOwnerDisplayName() {
        return getCachedTeamOwner().map(TeamPlayerData.PlayerData::getLastDisplayName).orElse(null);
    }

    protected Optional<TeamPlayerData.PlayerData> getCachedTeamOwner() {
        if (teamPlayerData == null) {
            setPlayerTeamData(level());
        }
        if (teamPlayerData != null) {
            if (lastOwnerChange < 0 || lastOwnerChange <= teamPlayerData.lastChangeTime()) {
                cachedTeamOwner = teamPlayerData.getPlayerForTeam(getClayTeamKey());
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
        if (!getClayTeam().canBeTamed()) {
            return false;
        }

        return teamPlayerData != null && teamPlayerData.putPlayerIfAbsent(getClayTeamKey(), player);
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
    public boolean getOrderedCommand() {
        if (getControllingPassenger() instanceof ClayMobEntity clayMob) {
            return clayMob.orderedCommand == OrderedCommand.SITTING;
        }
        return orderedCommand == OrderedCommand.SITTING;
    }

    public boolean isOrderedToIgnoreOwner() {
        return orderedCommand == OrderedCommand.IGNORE_OWNER;
    }

    protected OrderedCommand cycleOrderedCommand() {
        return switch (orderedCommand) {
            case IGNORE_OWNER -> OrderedCommand.FOLLOW_OWNER;
            case FOLLOW_OWNER -> OrderedCommand.SITTING;
            case SITTING -> OrderedCommand.IGNORE_OWNER;
        };
    }

    protected void setOrderedCommand(OrderedCommand command) {
        orderedCommand = command;
        setFollowsOwner(command == OrderedCommand.FOLLOW_OWNER);
    }

    @Override
    public boolean isInSittingPose() {
        return getDataFlag(SITTING_FLAG);
    }

    public boolean ignoresOwner() {
        return getDataFlag(FOLLOW_OWNER_FLAG);
    }

    public void setFollowsOwner(boolean followsOwner) {
        setDataFlag(FOLLOW_OWNER_FLAG, followsOwner);
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

    public float getChanceLuck() {
        return 0;
    }

    /**
     * @return the current status of the ClayMob, returns {@code null} to indicate the ClayMob does not do anything special
     */
    @Nullable
    public Component getWorkStatus() {
        return SoldierStatusHolder.clayMobWorkStatus(this);
    }

    /**
     * Tries to teleport this {@code ClayMob} to its owner.
     *
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
            moving.teleportTo((double) x + 0.5, y, (double) z + 0.5);
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
    public void startSeenByPlayer(@NonNull ServerPlayer serverPlayer) {
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
        return poiInfo.getPos();
    }

    public @Nullable Direction getPoiSide() {
        return poiInfo.side();
    }

    public @NonNull PoiPosInfo getPoiInfo() {
        return poiInfo;
    }

    public void clearPoiInfo() {
        setPoiInfo(PoiPosInfo.EMPTY);
    }

    public void setPoiInfo(@NonNull PoiPosInfo pos) {
        poiInfo = pos;
        boolean hasPos = !pos.isEmpty();
        entityData.set(HAS_POI_POS, hasPos);
        if (level() instanceof ServerLevel serverLevel) {
            poiPosCapability = hasPos ? UseAssignedPoiGoal.createCache(this, serverLevel) : null;
        }
    }

    public @Nullable AssignableWorksiteCapability getPoiCapability() {
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
    public boolean hasPoiPos() {
        return entityData.get(HAS_POI_POS);
    }

    @Override
    protected void doPush(@NonNull Entity entity) {
        if (!canPushPlayer() && entity instanceof Player) {
            return;
        }
        super.doPush(entity);
    }

    /**
     * @return whether this {@code ClayMob} can push a Player
     */
    protected boolean canPushPlayer() {
        return false;
    }

    @Override
    public int getTeamColor() {
        return getClayTeam().getColor(this, 0);
    }

    @Override
    protected void playAttackSound() {
        this.playSound(SoundEvents.MUD_BRICKS_HIT, 0.3F, 1.0F);
    }

    public List<String> getInfoState() {
        List<String> info = new ArrayList<>(4);

        info.add("TeamPlayerData: " + teamPlayerData);
        info.add("Owner: " + (cachedTeamOwner == null ? "Null" : cachedTeamOwner));
        info.add("PoiPos: " + getPoiPos() + " PoiCap: " + poiPosCapability);
        info.add("Active Goals:");
        goalSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).map(g -> g.getGoal().getClass().getSimpleName()).forEach(c -> info.add(" - " + c));

        info.add("Target: " + getTarget());
        return info;
    }

    /**
     * @return whether this {@code ClayMob} should be shown in the StatDisplay count bar.
     */
    public abstract boolean showInStatDisplay();

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        list.add(this.getDisplayName());

        list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.HEALTH, this.getHealth(), this.getMaxHealth()).withStyle(ChatFormatting.GRAY)));
        list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.ARMOR, this.getArmorValue()).withStyle(ChatFormatting.GRAY)));
        if (!getClayTeamKey().equals(ClayMobTeamManger.NO_TEAM_KEY)) {
            list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.TEAM, getClayTeam().getDisplayNameWithColor(c -> c.getColor(this, 0))).withStyle(ChatFormatting.GRAY)));
        }
        getCachedTeamOwner().ifPresent(owner -> list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.OWNER, owner.getLastDisplayName())).withStyle(ChatFormatting.GRAY)));
    }

    @FunctionalInterface
    protected interface DamageCalculator {
        float calculate(boolean waxed, @Nullable Entity attacker);
    }

    public enum OrderedCommand implements StringRepresentable {
        IGNORE_OWNER("ignore"),
        SITTING("sitting"),
        FOLLOW_OWNER("follow_owner");

        public static final Codec<OrderedCommand> CODEC = StringRepresentable.fromEnum(OrderedCommand::values);

        private final String serializedName;

        OrderedCommand(String serializedName) {
            this.serializedName = serializedName;
        }

        public Component getDisplayName() {
            return Component.translatable(translatableKey());
        }

        public String translatableKey() {
            return "clay_mob.command." + serializedName;
        }

        @Override
        public @NonNull String getSerializedName() {
            return serializedName;
        }
    }
}


