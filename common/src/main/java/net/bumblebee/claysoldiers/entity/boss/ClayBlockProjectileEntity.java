package net.bumblebee.claysoldiers.entity.boss;

import net.bumblebee.claysoldiers.entity.client.ClientClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.bumblebee.claysoldiers.init.ModDamageTypes;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClayBlockProjectileEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Float> BLOCK_SIZE = SynchedEntityData.defineId(ClayBlockProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SOLDIER_TEAM_ID = SynchedEntityData.defineId(ClayBlockProjectileEntity.class, EntityDataSerializers.INT);
    private static final BlockState CLAY_BLOCKSTATE = Blocks.CLAY.defaultBlockState();

    private static final String BLOCK_SIZE_TAG = "BlockSize";
    private static final String PIERCE_TAG = "PierceCount";
    private static final String SOLDIER_TEAM_ID_TAG = "soldierTeam";
    private static final String LIFETIME_TAG = "LifeTime";
    private static final String WEAPON_TAG = "Weapon";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClayBlockProjectileEntity.class);

    private final DamageSource damageSource;
    @Nullable
    private ClientClaySoldierEntity clientSoldier;
    @Nullable
    private WalkAnimationState walkState;
    public int showParticle = 3;
    private int pierceCount = 0;
    private int lifeTime = 20 * 7;
    @Nullable
    private ItemStack firedFromWeapon = null;


    public ClayBlockProjectileEntity(EntityType<? extends ClayBlockProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.accelerationPower = 0.02;
        this.damageSource = new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(ModDamageTypes.CLAY_HURT).orElseThrow(),
                this, null
        );
    }

    public ClayBlockProjectileEntity(Level level, LivingEntity owner, Vec3 movement) {
        super(ModEntityTypes.CLAY_BLOCK_PROJECTILE.get(), owner, movement, level);
        this.accelerationPower = 0.02;
        this.damageSource = new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(ModDamageTypes.CLAY_HURT).orElseThrow(),
                this, owner
        );
    }

    public ClayBlockProjectileEntity(Level level, LivingEntity owner, float yOffset) {
        super(ModEntityTypes.CLAY_BLOCK_PROJECTILE.get(), owner.getX(), owner.getY() + yOffset, owner.getZ(), level);
        this.accelerationPower = 0.02;
        this.setOwner(owner);
        this.damageSource = new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(ModDamageTypes.CLAY_HURT).orElseThrow(),
                this, owner
        );
    }


    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if (getBlockSize() != 1) {
            compound.putFloat(BLOCK_SIZE_TAG, getBlockSize());
        }
        if (pierceCount > 0) {
            compound.putInt(PIERCE_TAG, pierceCount);
        }
        if (hasClayTeam()) {
            compound.store(SOLDIER_TEAM_ID_TAG, ResourceLocation.CODEC, getClayTeam().key().location());
        }
        compound.putInt(LIFETIME_TAG, lifeTime);
        if (this.firedFromWeapon != null) {
            compound.store(WEAPON_TAG, ItemStack.CODEC, firedFromWeapon);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        setBlockSize(Math.max(0.2f, compound.getFloatOr(BLOCK_SIZE_TAG, 1f)));
        setPierceCount(compound.getIntOr(PIERCE_TAG, 0));

        compound.read(SOLDIER_TEAM_ID_TAG, ResourceLocation.CODEC).ifPresent(this::setClayTeam);
        lifeTime = compound.getIntOr(LIFETIME_TAG, 20*7);

        firedFromWeapon = compound.read(WEAPON_TAG, ItemStack.CODEC).orElse(null);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLOCK_SIZE, 1f);
        builder.define(SOLDIER_TEAM_ID, -1);
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = result.getEntity();
            boolean didHurt;
            if (this.getOwner() instanceof LivingEntity livingentity) {
                didHurt = entity.hurtServer(serverlevel, damageSource, 4.0F * getBlockSize());
                if (didHurt) {
                    if (entity.isAlive()) {
                        EnchantmentHelper.doPostAttackEffects(serverlevel, entity, damageSource);
                    } else {
                        livingentity.heal(2.0F);
                    }
                }
            } else {
                entity.hurtServer(serverlevel, this.damageSources().magic(), 2.5F * getBlockSize());
            }
        }
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        onHitEffect(result, false);
    }

    private void onHitEffect(@Nullable HitResult hitResult, boolean forceDelete) {
        if (!level().isClientSide()) {
            if (getOwner() instanceof ServerPlayer serverPlayer) {
                ModCriterions.HIT_WITH_CLAY_BLOCK_TRIGGER.get().trigger(
                        serverPlayer,
                        hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null,
                        getClientSoldier() != null,
                        firedFromWeapon
                        );
            }
            if (forceDelete || pierceCount < 0 || hitResult.getType() == HitResult.Type.BLOCK) {
                if (hasClayTeam()) {
                    spawnSoldier((ServerLevel) level());
                }
                discard();
            } else {
                pierceCount--;
            }
        } else {
            float particleOffset = 0.08f;
            for (int i = 0; i < 5 * Math.max(1, getBlockSize() * getBlockSize()); i++) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, CLAY_BLOCKSTATE),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        ((double) getRandom().nextFloat() - 0.5) * particleOffset,
                        ((double) getRandom().nextFloat() - 0.5) * particleOffset,
                        ((double) getRandom().nextFloat() - 0.5) * particleOffset
                );
            }
        }
    }

    private void spawnSoldier(ServerLevel level) {
        AbstractClaySoldierEntity soldier = ModEntityTypes.CLAY_SOLDIER_ENTITY.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (soldier != null) {
            Vec3 pos = position();
            var team = getClayTeam().key().location();
            soldier.setClayTeamType(team);
            soldier.setPos(pos);
            soldier.setSpawnedFrom(ClayMobTeamManger.createStackForTeam(team, level.registryAccess()), false);
            soldier.snapTo(pos, this.getYRot(), this.getXRot());
            soldier.yHeadRot = soldier.getYRot();
            soldier.yBodyRot = soldier.getYRot();
            soldier.finalizeSpawn(level, level.getCurrentDifficultyAt(soldier.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            level.addFreshEntity(soldier);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        if (showParticle++ >= 3) {
            showParticle = 0;
            return new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.CLAY.defaultBlockState());
        }
        return null;
    }

    public void setBlockSize(float size) {
        entityData.set(BLOCK_SIZE, size);
    }

    public float getBlockSize() {
        return entityData.get(BLOCK_SIZE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (BLOCK_SIZE.equals(key)) {
            this.refreshDimensions();
        }
        if (SOLDIER_TEAM_ID.equals(key) && level().isClientSide()) {
            var holder = getClayTeam();
            if (holder != null) {
                walkState = new WalkAnimationState();
                clientSoldier = ClientClaySoldierEntity.createAsProjectile(walkState, holder);
            } else {
                clientSoldier = null;
                walkState = null;
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getBlockSize());
    }

    public void setPierceCount(int pierceCount) {
        this.pierceCount = pierceCount;
    }

    public void setFiredFrom(ItemStack weapon) {
        this.firedFromWeapon = weapon;
    }

    public void setClayTeam(ResourceLocation id) {
        ClayMobTeamManger.getOptional(id, registryAccess()).ifPresentOrElse(
                team -> entityData.set(SOLDIER_TEAM_ID,
                        registryAccess().lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).getId(team)),
                () -> LOGGER.error("Error Setting Team {} for Clay Block Projectile", id)
        );
    }

    public boolean hasClayTeam() {
        return entityData.get(SOLDIER_TEAM_ID) >= 0;
    }

    @Nullable
    private Holder.Reference<ClayMobTeam> getClayTeam() {
        var reg = registryAccess().lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS);
        return reg.get(entityData.get(SOLDIER_TEAM_ID)).orElse(null);
    }

    @Nullable
    public ClientClaySoldierEntity getClientSoldier() {
        return clientSoldier;
    }

    public void clientTick(float partialTick) {
        if (walkState != null) {
            float f = Math.min(partialTick * 4.0F, 1.0F);
            this.walkState.update(f, 0.4F, 1f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime--;
        if (lifeTime <= 0) {
            onHitEffect(null, true);
        }
    }
}
