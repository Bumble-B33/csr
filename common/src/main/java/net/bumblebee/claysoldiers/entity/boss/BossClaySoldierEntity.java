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
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClayBossSpawnPayload;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.combined.SoldierPropertyCombinedMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

    private static final SoldierStatusHolder EMPTY = () -> null;
    private final ServerBossEvent bossEvent;
    private final SoldierPropertyCombinedMap baseProperties;

    @Nullable
    private BossClaySoldierBehaviour bossAI;

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
        this.bossAI.setUpBoss(this);
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
                .build();
    }


    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        if (!baseProperties.isEmpty()) {
            writeBasePropertiesToTag(baseProperties, pCompound);
        }

        BossTypes.CODEC.encodeStart(NbtOps.INSTANCE, getBossType())
                .ifSuccess(tag -> pCompound.put(TYPE_TAG, tag))
                .ifError(err -> LOGGER.error("Error saving Boss Type: {}", err.message()));
        writeBossAIToTag(getBossAI(), pCompound);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        readAndSetBasePropertiesFromTag(pCompound);

        if (readAndSetBossAI(pCompound) && pCompound.contains(TYPE_TAG)) {
            BossTypes.CODEC.parse(NbtOps.INSTANCE, pCompound.get(TYPE_TAG))
                    .ifSuccess(this::setBossType)
                    .ifError(err -> LOGGER.error("Error parsing Boss Type: {}", err.message()));
        }
    }

    @Override
    public void readItemPersistentData(CompoundTag tag) {
        if (!readAndSetBossAI(tag)) {
            setBossAI(ModBossBehaviours.DEFAULT.get());
        }
        readAndSetBasePropertiesFromTag(tag);

    }

    private boolean readAndSetBossAI(CompoundTag pCompound) {
        if (pCompound.contains(BOSS_AI_TAG)) {
            return BossClaySoldierBehaviour.CODEC.parse(NbtOps.INSTANCE, pCompound.get(BOSS_AI_TAG))
                    .ifSuccess(this::setBossAI)
                    .ifError(err -> LOGGER.error("Error parsing Boss AI: {}", err.message()))
                    .isSuccess();
        } else {
            return false;
        }
    }

    public static void writeBossAIToTag(BossClaySoldierBehaviour ai, CompoundTag pCompound) {
        BossClaySoldierBehaviour.CODEC.encodeStart(NbtOps.INSTANCE, ai)
                .ifSuccess(tag -> pCompound.put(BOSS_AI_TAG, tag))
                .ifError(err -> LOGGER.error("Error saving Boss AI: {}", err.message()));

    }

    public static void writeBasePropertiesToTag(SoldierPropertyMap properties, CompoundTag compound) {
        SoldierPropertyMap.CODEC_FOR_NON_ITEM.encodeStart(NbtOps.INSTANCE, properties)
                .ifSuccess(tag -> compound.put(BASE_PROPERTIES_TAG, tag))
                .ifError(err -> LOGGER.error("Error saving Base Properties: {}", err.message()));
    }

    private void readAndSetBasePropertiesFromTag(CompoundTag compound) {
        if (compound.contains(BASE_PROPERTIES_TAG)) {
            SoldierPropertyMap.CODEC_FOR_NON_ITEM.parse(NbtOps.INSTANCE, compound.get(BASE_PROPERTIES_TAG))
                    .ifSuccess(this::setBaseProperties)
                    .ifError(err -> LOGGER.error("Error parsing Base Properties: {}", err.message()));
        }
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
    protected void customServerAiStep() {
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
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
        return Math.max(0.2f, allProperties().getSoldierSize());
    }

    @Override
    protected boolean specificTargetPredicate(LivingEntity target) {
        return targetPredicate(target);
    }

    @Override
    protected boolean dropInventoryOnDeath() {
        return false;
    }


    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return pStack.is(ModTags.Items.SOLDIER_BOSS_EQUIPABLE);
    }

    @Override
    public boolean canRevive() {
        return false;
    }

    @Override
    public boolean canBeKilledByItem() {
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
        type.modifyBossEvent(bossEvent);
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
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
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
        if (level() instanceof ServerLevel serverLevel) {
            LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable(getBossAI().getLootTable());
            LootParams.Builder paramBuilder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
                    .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
            if (this.lastHurtByPlayerTime > 0 && this.lastHurtByPlayer != null) {
                paramBuilder = paramBuilder
                        .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer)
                        .withLuck(this.lastHurtByPlayer.getLuck());
            }
            return lootTable.getRandomItems(paramBuilder.create(LootContextParamSets.ENTITY), this.getLootTableSeed());
        }
        return List.of();
    }

    public enum BossTypes implements StringRepresentable {
        NORMAL("normal", event -> event.setColor(BossEvent.BossBarColor.BLUE)),
        ZOMBIE("zombie", event -> event.setColor(BossEvent.BossBarColor.GREEN)),
        VAMPIRE("vampire", event -> event.setDarkenScreen(true).setColor(BossEvent.BossBarColor.RED));

        public static final Codec<BossTypes> CODEC = StringRepresentable.fromEnum(BossTypes::values);

        private final String serializedName;
        private final Consumer<BossEvent> modifyBossEvent;

        BossTypes(String serializedName, Consumer<BossEvent> modifyBossEvent) {
            this.serializedName = serializedName;
            this.modifyBossEvent = modifyBossEvent;
        }

        private void modifyBossEvent(BossEvent event) {
            modifyBossEvent.accept(event);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}