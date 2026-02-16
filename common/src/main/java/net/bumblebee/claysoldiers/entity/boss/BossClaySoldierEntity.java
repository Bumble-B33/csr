package net.bumblebee.claysoldiers.entity.boss;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.goal.ClaySodlierBreathAirGoal;
import net.bumblebee.claysoldiers.entity.goal.ClaySoldierMeleeAttackGoal;
import net.bumblebee.claysoldiers.entity.goal.target.ClaySoldierNearestTargetGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.WorkSelectorGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.init.ModBossBehaviours;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayBossSpawnPayload;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.combined.SoldierPropertyCombinedMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class BossClaySoldierEntity extends AbstractClaySoldierEntity {
    private static final EntityDataAccessor<Byte> BOSS_TYPE = SynchedEntityData.defineId(BossClaySoldierEntity.class, EntityDataSerializers.BYTE);

    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private static final String BASE_PROPERTIES_TAG = "baseProperties";
    private static final String TYPE_TAG = "bossType";
    private static final String BOSS_AI_TAG = "bossAI";
    private static final String MINION_OWNER_TAG = "minionOwner";
    private static final String MINIONS_TAG = "minions";
    private static final String PHASE_COMPLETED_TAG = "phaseCompleted";

    private static final SoldierStatusHolder EMPTY = () -> null;
    private final ServerBossEvent bossEvent;
    private final SoldierPropertyCombinedMap baseProperties;

    @Nullable
    private BossClaySoldierBehaviour bossAI;

    @Nullable
    private BossClaySoldierEntity minionOwner;
    @Nullable
    private UUID minionOwnerUUID;
    @Nullable
    private List<BossClaySoldierEntity> minions;
    @Nullable
    private List<UUID> minionUUIDs;

    private int phaseCompleted = 0;
    private int nextMinionGlow = 0;

    public BossClaySoldierEntity(EntityType<? extends BossClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.BOSS, s -> EMPTY);
        this.otherPlayerDamage = (w, e) -> w ? 0.9f : 1f;
        this.defaultDamage = (w, e) -> w ? 0.9f : 1f;
        this.clayDamage = (w, e) -> w ? 0.25f : 0.5f;
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        this.baseProperties = new SoldierPropertyCombinedMap();
    }

    public void setBossAI(@NotNull BossClaySoldierBehaviour bossAI) {
        if (this.bossAI != null || bossAI == null) {
            throw new IllegalStateException("Cannot set Boss Ai twice");
        }
        this.bossAI = bossAI;
        this.bossAI.setUpBoss(this, bossEvent);
    }

    @NotNull
    public BossClaySoldierBehaviour getBossAI() {
        return Objects.requireNonNull(bossAI, "Boss AI not initialized");
    }

    public static AttributeSupplier bossAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ARMOR, 5D)
                .add(Attributes.ATTACK_DAMAGE, 2.5f)
                .add(Attributes.ATTACK_SPEED, 0.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.TEMPT_RANGE)
                .build();
    }


    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        if (!baseProperties.isEmpty()) {
            writeBasePropertiesToTag(baseProperties, output);
        }
        output.store(TYPE_TAG, BossTypes.CODEC, getBossType());

        writeBossAIToTag(getBossAI(), output);

        if (minionOwner != null) {
            output.store(MINION_OWNER_TAG, UUIDUtil.CODEC, minionOwner.getUUID());
        }
        if (minions != null) {
            output.store(MINIONS_TAG, UUIDUtil.CODEC.listOf(), minions.stream().map(Entity::getUUID).toList());
        }

        if (phaseCompleted > 0) {
            output.putInt(PHASE_COMPLETED_TAG, phaseCompleted);
        }
    }


    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        readAndSetBasePropertiesFromTag(input);
        var bossType = input.read(TYPE_TAG, BossTypes.CODEC);
        if (readAndSetBossAI(input) && bossType.isPresent()) {
            setBossType(bossType.orElseThrow());
        }

        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        minionOwnerUUID = input.read(MINION_OWNER_TAG, UUIDUtil.CODEC).orElse(null);

        minionUUIDs = input.read(MINIONS_TAG, UUIDUtil.CODEC.listOf()).orElse(null);

        phaseCompleted = input.getIntOr(PHASE_COMPLETED_TAG, 0);
    }

    @Override
    public void readItemPersistentData(ValueInput tag) {
        if (!readAndSetBossAI(tag)) {
            setBossAI(ModBossBehaviours.DEFAULT.get());
        }
        readAndSetBasePropertiesFromTag(tag);

    }

    private boolean readAndSetBossAI(ValueInput pCompound) {
        var ai = pCompound.read(BOSS_AI_TAG, BossClaySoldierBehaviour.CODEC);
        if (ai.isPresent()) {
            setBossAI(ai.orElseThrow());
            return true;
        } else {
            return false;
        }
    }

    public static void writeBossAIToTag(BossClaySoldierBehaviour ai, ValueOutput pCompound) {
        pCompound.store(BOSS_AI_TAG, BossClaySoldierBehaviour.CODEC, ai);
    }

    public static void writeBasePropertiesToTag(SoldierPropertyMap properties, ValueOutput compound) {
        compound.store(BASE_PROPERTIES_TAG, SoldierPropertyMap.CODEC_FOR_NON_ITEM, properties);
    }

    private void readAndSetBasePropertiesFromTag(ValueInput compound) {
        compound.read(BASE_PROPERTIES_TAG, SoldierPropertyMap.CODEC_FOR_NON_ITEM).ifPresent(this::setBaseProperties);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ClaySodlierBreathAirGoal(this));
        this.goalSelector.addGoal(0, new RangedAttackGoal(this, 1f, 40, 20f));
        this.goalSelector.addGoal(1, new ClaySoldierMeleeAttackGoal(this, 1, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, this.getClass()));
        this.targetSelector.addGoal(1, new ClaySoldierNearestTargetGoal(this, true, this::targetPredicate, this::specificTargetPredicate));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BOSS_TYPE, (byte) BossTypes.NORMAL.ordinal());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean aiAllowHurt = true;
        if (bossAI != null) {
            aiAllowHurt = bossAI.onHurt(this, source);
        }
        if (!aiAllowHurt) {
            return false;
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean hasNoTeam() {
        return true;
    }

    @Override
    protected boolean isClayFood(ItemStack stack) {
        return false;
    }

    @Override
    protected WorkSelectorGoal getOrCreateWorkSelectorGoal() {
        return new WorkSelectorGoal(this, List.of());
    }

    @Override
    public @Nullable UUID getClayTeamOwnerUUID() {
        return null;
    }

    @Override
    public void sendSpawnPayload(ServerPlayer tracking) {
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayer(tracking, new ClayBossSpawnPayload(this));
        super.sendSpawnPayload(tracking);
    }

    // Bossbar
    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        if (bossAI != null) {
            bossAI.getBossEventProgress(this, bossEvent);
        }


        if (tickCount % 200 == 0) {
            loadMinions();
            boolean shouldMinionsGlow = nextMinionGlow < tickCount;
            if (shouldMinionsGlow) {
                nextMinionGlow += 400;
            }

            if (minions != null) {
                Iterator<BossClaySoldierEntity> each = minions.iterator();
                while (each.hasNext()) {
                    var minion = each.next();
                    if (!minion.isAlive() && minion.getMinionOwner() == null) {
                        each.remove();
                        if (minionUUIDs != null) {
                            minionUUIDs.remove(minion.getUUID());
                        }
                    } else if (shouldMinionsGlow) {
                        minion.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, true));
                    }
                }
            }

        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        bossEvent.removeAllPlayers();
    }

    public void setBaseProperties(SoldierPropertyMapReader baseProperties) {
        if (!baseProperties.attackType().compatibleWith(getAttackType())) {
            throw new IllegalArgumentException("Cannot set Base Properties of %s as Attack Type %s is not compatible with the Attack Type of the Boss (%s).".formatted(baseProperties, baseProperties.attackType(), getAttackType()));
        }
        this.baseProperties.clear();
        this.baseProperties.combineMap(baseProperties);

        propertyCombiner.addBaseProperties(baseProperties);
        propertyCombiner.combine();
    }

    public SoldierPropertyMap getBaseProperties() {
        return baseProperties;
    }

    @Override
    public float getSoldierSize() {
        if (allProperties() == null) {
            return 1f;
        }
        return Math.clamp(allProperties().getSoldierSize(), 0.2f, 10f);
    }

    @Override
    protected boolean specificTargetPredicate(LivingEntity target, ServerLevel level) {
        return targetPredicate(target, level);
    }

    @Override
    protected boolean dropInventoryOnDeath() {
        return false;
    }


    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack pStack) {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return pStack.is(ModTags.Items.SOLDIER_BOSS_EQUIPPABLE);
    }

    @Override
    public boolean canRevive() {
        return false;
    }

    @Override
    protected boolean canPushPlayer() {
        return true;
    }

    @Override
    public boolean canBeKilledByDisruptor(ServerLevel level, ServerPlayer serverPlayer) {
        return false;
    }

    @Override
    protected OptionalInt openMenuScreen(Player player) {
        return OptionalInt.empty();
    }

    public @NotNull BossClaySoldierEntity.BossTypes getBossType() {
        return BossTypes.values()[this.entityData.get(BOSS_TYPE)];
    }

    public void setBossType(@NotNull BossClaySoldierEntity.BossTypes type) {
        if (level().isClientSide() || !getBossAI().isAllowed(type)) {
            return;
        }
        this.entityData.set(BOSS_TYPE, (byte) type.ordinal());
    }

    @Override
    protected void specializedAttack(Entity target) {
        if (isVampire()) {
            heal(2f);
        }
    }

    @Override
    protected float getAttackPower(Entity target) {
        return target instanceof ClayMobEntity ? 10 : 1;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float pVelocity) {
        performRangedAttack(target.getX(), target.getY() + (double) target.getEyeHeight() * 0.5, target.getZ(), 0.75f);
    }

    private void performRangedAttack(double x, double y, double z, float size) {
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1024, this.blockPosition(), 0);
        }

        double skullX = this.getX();
        double skullY = this.getY();
        double skullZ = this.getZ();
        double dX = x - skullX;
        double dY = y - skullY;
        double dZ = z - skullZ;
        Vec3 velocity = new Vec3(dX, dY, dZ);
        ClayBlockProjectileEntity clayBlock = new ClayBlockProjectileEntity(this.level(), this, velocity.normalize());
        clayBlock.setOwner(this);
        clayBlock.setPosRaw(skullX, skullY, skullZ);
        clayBlock.setBlockSize(size);
        this.level().addFreshEntity(clayBlock);
    }

    private BossClaySoldierEntity getMinionOwner() {
        if (minionOwner != null) {
            return minionOwner;
        }
        if (level() instanceof ServerLevel serverLevel && minionOwnerUUID != null) {
            minionOwner = (BossClaySoldierEntity) serverLevel.getEntity(minionOwnerUUID);
        }
        return minionOwner;
    }

    private void loadMinions() {
        if (minions != null || minionUUIDs == null) {
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            minions = new ArrayList<>(minionUUIDs.size());
            Iterator<UUID> uuidIterator = minionUUIDs.iterator();
            while (uuidIterator.hasNext()) {
                UUID uuidMinion = uuidIterator.next();
                var minion = (BossClaySoldierEntity) serverLevel.getEntity(uuidMinion);
                if (minion != null) {
                    minions.add(minion);
                } else {
                    uuidIterator.remove();
                    LOGGER.error("Minion {} of {} was not found in the world.", uuidMinion, this);
                }

            }

        }
    }

    public int getMinionCount() {
        loadMinions();
        return minions == null ? 0 : minions.size();
    }

    public void addAllMinions(List<BossClaySoldierEntity> minionsToAdd) {
        loadMinions();
        if (this.minions == null || this.minionUUIDs == null) {
            this.minions = new ArrayList<>(minionsToAdd.size());
            this.minionUUIDs = new ArrayList<>(minionsToAdd.size());
        }
        this.minions.addAll(minionsToAdd);
        minionsToAdd.forEach(minion -> minionUUIDs.add(minion.getUUID()));

        if (minions.size() != minionUUIDs.size()) {
            throw new IllegalStateException("Minion UUIDs and Minions do not match");
        }
    }

    public void setMinionOwner(@Nullable BossClaySoldierEntity minionOwner) {
        this.minionOwner = minionOwner;
        this.minionOwnerUUID = minionOwner == null ? null : minionOwner.getUUID();
    }

    public void notifyMinionOwnerOfDeath() {
        if (level().isClientSide()) {
            return;
        }

        var minionOwner = getMinionOwner();
        if (minionOwner == null) {
            LOGGER.error("{} tried to notify Minion Owner of death, but owner was null.", this);
            return;
        }
        minionOwner.removeMinion(this);
    }

    private void removeMinion(BossClaySoldierEntity minion) {
        loadMinions();
        if (minions == null || minionUUIDs == null) {
            LOGGER.error("Minion Owner {} of {} did not have minions.", this, minion);
            return;
        }
        minionUUIDs.remove(minion.getUUID());
        if (!minions.remove(minion)) {
            LOGGER.error("Minion Owner {} of {} did not contain this.", this, minion);
        }
        if (minions.isEmpty()) {
            minions = null;
            minionUUIDs = null;
        }
    }

    public int getPhaseCompleted() {
        return phaseCompleted;
    }

    public void completePhase() {
        phaseCompleted++;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!getBossAI().shouldDie(this)) {
            return;
        }
        super.die(damageSource);
        getBossAI().onDeath(this, damageSource);
    }

    protected boolean isUndead() {
        return getBossType() != BossTypes.NORMAL;
    }

    protected boolean isVampire() {
        return getBossType() == BossTypes.VAMPIRE;
    }

    @Override
    public boolean isZombie() {
        return getBossType() == BossTypes.ZOMBIE;
    }

    protected boolean shouldWalkToTarget(@Nullable Vec3 pos) {
        return pos == null || !this.isUndead() || !this.level().canSeeSky(BlockPos.containing(pos));
    }

    @Override
    public void onConversion(ClayMobEntity oldSoldier, ValueInput tag, @Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, this);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (bossAI == null) {
            setBossAI(ModBossBehaviours.DEFAULT.get());
        }
        if (!baseProperties.isEmpty()) {
            setBaseProperties(
                    SoldierPropertyMap.of(SoldierPropertyTypes.SIZE.get().createProperty(level.getRandom().nextFloat() * 3 + 1))
            );

        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public List<ItemStack> getBossDeathLoot(DamageSource damageSource) {
        if (level() instanceof ServerLevel serverLevel && bossAI != null) {
            var lootTableKey = bossAI.getLootTable();
            if (lootTableKey == null) {
                return List.of();
            }
            LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable(lootTableKey);
            LootParams.Builder paramBuilder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
                    .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
            Player player = this.getLastHurtByPlayer();
            if (this.lastHurtByPlayerMemoryTime > 0 && player != null) {
                paramBuilder = paramBuilder
                        .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                        .withLuck(player.getLuck());
            }
            return lootTable.getRandomItems(paramBuilder.create(LootContextParamSets.ENTITY), this.getLootTableSeed());
        }
        return List.of();
    }

    @Override
    public List<String> getInfoState() {
        var list = super.getInfoState();
        list.add(String.format("Boss Type: %s", getBossType()));
        list.add(String.format("Boss AI: %s", ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY.getKey(getBossAI())));
        list.add(String.format("Base Properties: %s", baseProperties));
        if (!level().isClientSide()) {
            getMinionOwner();
            loadMinions();
            list.add("MinionOwner: " + minionOwner + (minionOwnerUUID == null ? "" : " (uuid)"));
            list.add("Minions: " + (minions == null ? "null" : minions.size()) + " | " + (minionUUIDs == null ? "null" : minionUUIDs.size()));
            list.add("Phase Completed: " + phaseCompleted);
        }
        return list;
    }

    public enum BossTypes implements StringRepresentable {
        NORMAL("normal", event -> event.setColor(BossEvent.BossBarColor.BLUE)),
        ZOMBIE("zombie", event -> event.setColor(BossEvent.BossBarColor.GREEN)),
        VAMPIRE("vampire", event -> event.setDarkenScreen(true).setColor(BossEvent.BossBarColor.RED));

        public static final Codec<BossTypes> CODEC = StringRepresentable.fromEnum(BossTypes::values);

        private final String serializedName;

        BossTypes(String serializedName, Consumer<BossEvent> modifyBossEvent) {
            this.serializedName = serializedName;
        }


        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}