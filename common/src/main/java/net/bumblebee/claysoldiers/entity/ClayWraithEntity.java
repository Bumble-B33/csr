package net.bumblebee.claysoldiers.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.bumblebee.claysoldiers.entity.goal.ClayMobSitGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierInventoryHandler;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierLike;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class ClayWraithEntity extends ClayMobTeamOwnerEntity implements ClaySoldierLike, VampiricClayMob {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double WRAITH_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
    public static final float FLAP_DEGREES_PER_TICK = 45.836624F;
    public static final int TICKS_PER_FLAP = Mth.ceil((float) (Math.PI * 5.0 / 4.0));
    public static final float WRAITH_SCALE = 0.5f;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(ClayWraithEntity.class, EntityDataSerializers.BYTE);
    private static final int FLAG_IS_CHARGING = 1;
    private static final byte MIN_LIFE_SPAN = 0;
    private static final byte MAX_LIFE_SPAN = 63;
    public static final String WRAITH_ATTACK_TAG = "WraithAttacks";
    public static final String LIFE_TICKS_TAG = "LifeTicks";
    public static final String MAX_LIFE_TICKS_TAG = "MaxLifeTicks";

    @Nullable
    private BlockPos boundOrigin;
    private int maxLimitedLifeTicks = -1;
    private int limitedLifeTicks;
    private List<SpecialAttack<?>> attackFunctions = List.of();

    public ClayWraithEntity(EntityType<? extends ClayMobEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new WraithMovementControl(this);
    }

    @Override
    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    public static AttributeSupplier setWraithAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 0D)
                .add(Attributes.ATTACK_DAMAGE, 2.5f)
                .add(Attributes.ATTACK_SPEED, 0.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WraithSitGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WraithChargeAttackGoal());
        this.goalSelector.addGoal(3, new WraithRandomMoveGoal());

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, AbstractClaySoldierEntity.class, false, this::targetPredicate));
    }

    @Override
    protected boolean targetPredicate(LivingEntity other) {
        if (!(other instanceof ClayMobEntity target)) {
            return false;
        }
        return shouldAttackTeamHolder(target);
    }

    @Override
    protected double getDefaultAttackReach() {
        return WRAITH_ATTACK_REACH;
    }

    @Override
    protected void handleTeamChange(ResourceLocation teamId) {
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
        this.checkInsideBlocks();
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (!isNightForVampire()) {
            this.limitedLifeTicks--;
        }

        if (hasLimitedLife() && this.limitedLifeTicks <= MIN_LIFE_SPAN) {
            updateLifePercent();
            this.kill();
        }
        if (!level().isClientSide && hasLimitedLife() && limitedLifeTicks % 5 == 0) {
            updateLifePercent();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.boundOrigin != null) {
            pCompound.putInt("BoundX", this.boundOrigin.getX());
            pCompound.putInt("BoundY", this.boundOrigin.getY());
            pCompound.putInt("BoundZ", this.boundOrigin.getZ());
        }
        if (hasLimitedLife()) {
            pCompound.putInt(MAX_LIFE_TICKS_TAG, this.maxLimitedLifeTicks);
            pCompound.putInt(LIFE_TICKS_TAG, this.limitedLifeTicks);
        }
        if (!attackFunctions.isEmpty()) {
            writeSpecialAttackToTag(pCompound, attackFunctions);
        }

    }

    public static void writeSpecialAttackToTag(CompoundTag tag, List<SpecialAttack<?>> attacks) {
        SpecialAttack.LIST_CODEC
                .encodeStart(NbtOps.INSTANCE, attacks)
                .resultOrPartial(LOGGER::error)
                .ifPresent(wraithAttack -> {
                    tag.put(WRAITH_ATTACK_TAG, wraithAttack);
                });
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("BoundX")) {
            this.boundOrigin = new BlockPos(pCompound.getInt("BoundX"), pCompound.getInt("BoundY"), pCompound.getInt("BoundZ"));
        }

        readItemPersistentData(pCompound);
    }

    @Nullable
    public BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos pBoundOrigin) {
        this.boundOrigin = pBoundOrigin;
    }

    public void setLimitedLife(int pLimitedLifeTicks) {
        this.maxLimitedLifeTicks = pLimitedLifeTicks;
        this.limitedLifeTicks = pLimitedLifeTicks;
    }

    public boolean hasLimitedLife() {
        return maxLimitedLifeTicks > MIN_LIFE_SPAN;
    }

    public int getLimitedLifeTicks() {
        return limitedLifeTicks;
    }

    public boolean isCharging() {
        int i = this.entityData.get(DATA_FLAGS_ID);
        return (i & ClayWraithEntity.FLAG_IS_CHARGING) != 0;
    }

    public void setIsCharging(boolean pCharging) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        if (pCharging) {
            i |= ClayWraithEntity.FLAG_IS_CHARGING;
        } else {
            i &= ~ClayWraithEntity.FLAG_IS_CHARGING;
        }
        this.entityData.set(DATA_FLAGS_ID, (byte) (i & 0xFF));
    }

    /**
     * Returns the lifetime left of this wraith.
     * The value is always between [{@value MIN_LIFE_SPAN}, {@value MAX_LIFE_SPAN}].
     * Where {@value MAX_LIFE_SPAN} is the starting life point and {@value MIN_LIFE_SPAN} is the end.
     */
    public int getLifePoint() {
        return (this.entityData.get(DATA_FLAGS_ID) >> 1);
    }

    private void updateLifePercent() {
        float percent;
        if (maxLimitedLifeTicks <= MIN_LIFE_SPAN) {
            percent = 1;
        } else {
            percent = (float) limitedLifeTicks / maxLimitedLifeTicks;
        }
        if (percent < 0) {
            percent = 0;
        } else if (percent > 1) {
            percent = 1;
        }
        byte b = (byte) ((isCharging() ? 1 : 0) | (byte) ((byte) (percent * MAX_LIFE_SPAN) << 1));
        this.entityData.set(DATA_FLAGS_ID, b);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    private void doAttackFunctions(LivingEntity target) {
        for (var attack : attackFunctions) {
            if (attack.getAttackType().is(SpecialAttackType.MELEE)) {
                attack.performAttackEffect(this, target);
            }
        }
    }

    public void setAttackFunctions(List<SpecialAttack<?>> attacks) {
        attackFunctions = attacks;
    }

    @Override
    public ClayMobEntity asClayMob() {
        return this;
    }

    @Override
    public <T extends ClaySoldierInventoryHandler> void copyInventory(T toCopyTo) {
    }

    @Override
    public void readItemPersistentData(CompoundTag tag) {
        if (tag.contains(LIFE_TICKS_TAG, Tag.TAG_INT)) {
            limitedLifeTicks = tag.getInt(LIFE_TICKS_TAG);
            maxLimitedLifeTicks = Math.max(tag.getInt(MAX_LIFE_TICKS_TAG), limitedLifeTicks);
        } else if (tag.contains(LIFE_TICKS_TAG, Tag.TAG_STRING)) {
            CodecUtils.getTimeFromEither(Either.right(tag.getString(LIFE_TICKS_TAG)))
                    .ifSuccess(ticks -> {
                        limitedLifeTicks = ticks;
                        maxLimitedLifeTicks = Math.max(tag.getInt(MAX_LIFE_TICKS_TAG), limitedLifeTicks);
                    })
                    .ifError(err -> LOGGER.error("Error Reading {} for Wraith as Seconds: {}", LIFE_TICKS_TAG, err.message()));
        }
        if (tag.contains(WRAITH_ATTACK_TAG, Tag.TAG_COMPOUND)) {
            SpecialAttack.LIST_CODEC
                    .parse(NbtOps.INSTANCE, tag.get(WRAITH_ATTACK_TAG))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(specialAttack -> this.attackFunctions = specialAttack);
        }
    }

    @Override
    public float getNightPower() {
        return (level().getMoonBrightness() + 1) * 1.5f;
    }

    @Override
    public float getSpeed() {
        return super.getSpeed() * getPowerMultiplier();
    }

    @Nullable
    public static ClayWraithEntity spawnWraith(ServerLevel level, ClayMobEntity caster, int duration) {
        return spawnWraith(level, caster, duration, true, (w) -> {
        });
    }

    @Nullable
    public static ClayWraithEntity spawnWraith(ServerLevel level, ClayMobEntity caster, int duration, Consumer<ClayWraithEntity> onSpawn) {
        return spawnWraith(level, caster, duration, false, onSpawn);
    }

    @Nullable
    private static ClayWraithEntity spawnWraith(ServerLevel level, ClayMobEntity caster, int duration, boolean summoned, Consumer<ClayWraithEntity> onSpawn) {
        if (duration <= 0) {
            return null;
        }

        BlockPos bound = caster.blockPosition();
        ClayWraithEntity wraith = ModEntityTypes.CLAY_WRAITH.get().create(caster.level());
        if (wraith != null) {
            wraith.moveTo(caster.position(), caster.getYRot(), caster.getXRot());
            wraith.finalizeSpawn(level, level.getCurrentDifficultyAt(bound), summoned ? MobSpawnType.MOB_SUMMONED : MobSpawnType.CONVERSION, null);
            wraith.setBoundOrigin(bound);
            wraith.setLimitedLife(20 * (duration + caster.getRandom().nextInt(duration)));
            wraith.setClayTeamType(caster.getClayTeamType());
            level.addFreshEntityWithPassengers(wraith);
            level.gameEvent(GameEvent.ENTITY_PLACE, bound, GameEvent.Context.of(caster));
            onSpawn.accept(wraith);
        }
        return wraith;
    }

    @Override
    public LevelAccessor getLevel() {
        return level();
    }

    class WraithMovementControl extends MoveControl {
        public WraithMovementControl(ClayWraithEntity wraith) {
            super(wraith);
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - ClayWraithEntity.this.getX(), this.wantedY - ClayWraithEntity.this.getY(), this.wantedZ - ClayWraithEntity.this.getZ());
                double d0 = vec3.length();
                if (d0 < ClayWraithEntity.this.getBoundingBox().getSize()) {
                    this.operation = Operation.WAIT;
                    ClayWraithEntity.this.setDeltaMovement(ClayWraithEntity.this.getDeltaMovement().scale(0.5));
                } else {
                    ClayWraithEntity.this.setDeltaMovement(ClayWraithEntity.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05 / d0)));
                    if (ClayWraithEntity.this.getTarget() == null) {
                        Vec3 vec31 = ClayWraithEntity.this.getDeltaMovement();
                        ClayWraithEntity.this.setYRot(-((float) Mth.atan2(vec31.x, vec31.z)) * (180.0F / (float) Math.PI));
                        ClayWraithEntity.this.yBodyRot = ClayWraithEntity.this.getYRot();
                    } else {
                        double d2 = ClayWraithEntity.this.getTarget().getX() - ClayWraithEntity.this.getX();
                        double d1 = ClayWraithEntity.this.getTarget().getZ() - ClayWraithEntity.this.getZ();
                        ClayWraithEntity.this.setYRot(-((float) Mth.atan2(d2, d1)) * (180.0F / (float) Math.PI));
                        ClayWraithEntity.this.yBodyRot = ClayWraithEntity.this.getYRot();
                    }
                }
            }
        }
    }

    class WraithChargeAttackGoal extends Goal {
        public WraithChargeAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = ClayWraithEntity.this.getTarget();
            return livingentity != null
                    && livingentity.isAlive()
                    && !ClayWraithEntity.this.getMoveControl().hasWanted()
                    && ClayWraithEntity.this.random.nextInt(reducedTickDelay(7)) == 0 && ClayWraithEntity.this.distanceToSqr(livingentity) > 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return ClayWraithEntity.this.getMoveControl().hasWanted() && ClayWraithEntity.this.isCharging() && ClayWraithEntity.this.getTarget() != null && ClayWraithEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity livingentity = ClayWraithEntity.this.getTarget();
            if (livingentity != null) {
                Vec3 vec3 = livingentity.getEyePosition();
                ClayWraithEntity.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0);
            }

            ClayWraithEntity.this.setIsCharging(true);
            ClayWraithEntity.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            ClayWraithEntity.this.setIsCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = ClayWraithEntity.this.getTarget();
            if (target != null) {
                if (ClayWraithEntity.this.getBoundingBox().inflate(2).intersects(target.getBoundingBox())) {
                    ClayWraithEntity.this.doHurtTarget(target);
                    doAttackFunctions(target);
                    ClayWraithEntity.this.setIsCharging(false);
                } else {
                    double distanceToTarget = ClayWraithEntity.this.distanceToSqr(target);
                    if (distanceToTarget < 9.0) {
                        Vec3 targetPos = target.getEyePosition();
                        ClayWraithEntity.this.moveControl.setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 1.0);
                    }
                }
            }
        }
    }

    class WraithRandomMoveGoal extends Goal {
        public WraithRandomMoveGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (ClayWraithEntity.this.isOrderedToSit()) {
                return false;
            }
            return !ClayWraithEntity.this.getMoveControl().hasWanted() && ClayWraithEntity.this.random.nextInt(reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos blockpos = ClayWraithEntity.this.getBoundOrigin();
            if (blockpos == null) {
                blockpos = ClayWraithEntity.this.blockPosition();
            }

            for (int i = 0; i < 3; i++) {
                BlockPos blockpos1 = blockpos.offset(ClayWraithEntity.this.random.nextInt(15) - 7, ClayWraithEntity.this.random.nextInt(11) - 5, ClayWraithEntity.this.random.nextInt(15) - 7);
                if (ClayWraithEntity.this.level().isEmptyBlock(blockpos1)) {
                    ClayWraithEntity.this.moveControl
                            .setWantedPosition((double) blockpos1.getX() + 0.5, (double) blockpos1.getY() + 0.5, (double) blockpos1.getZ() + 0.5, 0.25);
                    if (ClayWraithEntity.this.getTarget() == null) {
                        ClayWraithEntity.this.getLookControl()
                                .setLookAt((double) blockpos1.getX() + 0.5, (double) blockpos1.getY() + 0.5, (double) blockpos1.getZ() + 0.5, 180.0F, 20.0F);
                    }
                    break;
                }
            }
        }
    }

    static class WraithSitGoal extends ClayMobSitGoal {
        public WraithSitGoal(ClayMobEntity clayMobEntity) {
            super(clayMobEntity);
        }

        @Override
        public boolean canUse() {
            if (isCurrentlyFighting()) {
                return false;
            } else if (clayMobEntity.isPassenger()) {
                return false;
            } else if (this.clayMobEntity.isInWaterOrBubble()) {
                return false;
            } else {
                LivingEntity owner = this.clayMobEntity.getClayTeamOwner();
                if (owner == null) {
                    return false;
                } else {
                    return (!(this.clayMobEntity.distanceToSqr(owner) < 144.0) || owner.getLastHurtByMob() == null) && this.clayMobEntity.isOrderedToSit();
                }
            }
        }
    }
}
