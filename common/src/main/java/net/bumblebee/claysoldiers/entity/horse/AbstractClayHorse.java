package net.bumblebee.claysoldiers.entity.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobRideableEntity;
import net.bumblebee.claysoldiers.entity.goal.horse.ClayHorseRandomStandGoal;
import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.itemeffectholder.HorseWearableItemStack;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public abstract class AbstractClayHorse extends ClayMobRideableEntity implements VariantHolder<ClayHorseVariants> {
    public static final float SCALE = 0.25f;
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractClayHorse.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> VARIANT_ID_SYNC = SynchedEntityData.defineId(AbstractClayHorse.class, EntityDataSerializers.INT);

    private static final int FLAG_NOT_USED = 2;
    private static final int FLAG_SADDLE = 4;
    private static final int FLAG_BRED = 8;
    private static final int FLAG_EATING = 16;
    private static final int FLAG_STANDING = 32;
    private static final int FLAG_OPEN_MOUTH = 64;

    private static final int ARMOR_INDEX = 0;

    private int eatingCounter;
    private int standCounter;
    public int tailCounter;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop = true;
    protected int gallopSoundCounter;
    private final NonNullList<HorseWearableItemStack> armorList = NonNullList.withSize(1, HorseWearableItemStack.EMPTY);

    protected AbstractClayHorse(EntityType<? extends ClayMobRideableEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        if (this.canPerformRearing()) {
            this.goalSelector.addGoal(9, new ClayHorseRandomStandGoal(this));
        }

        this.addBehaviourGoals();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("EatingHaystack", this.isEating());
        pCompound.putBoolean("Bred", this.isBred());
        pCompound.putInt("Variant", this.getVariant().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setEating(pCompound.getBoolean("EatingHaystack"));
        this.setBred(pCompound.getBoolean("Bred"));
        this.setVariant(ClayHorseVariants.getById(pCompound.getInt("Variant")));
        setArmor(getItemBySlot(EquipmentSlot.BODY));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_FLAGS, (byte) 0);
        builder.define(VARIANT_ID_SYNC, 0);
    }

    protected boolean getFlag(int pFlagId) {
        return (this.entityData.get(DATA_ID_FLAGS) & pFlagId) != 0;
    }

    protected void setFlag(int pFlagId, boolean pValue) {
        byte b0 = this.entityData.get(DATA_ID_FLAGS);
        if (pValue) {
            this.entityData.set(DATA_ID_FLAGS, (byte) (b0 | pFlagId));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte) (b0 & ~pFlagId));
        }
    }

    public boolean isEating() {
        return this.getFlag(FLAG_EATING);
    }

    public boolean isStanding() {
        return this.getFlag(FLAG_STANDING);
    }

    public boolean isBred() {
        return this.getFlag(FLAG_BRED);
    }

    public void setBred(boolean pBreeding) {
        this.setFlag(FLAG_BRED, pBreeding);
    }

    public boolean isSaddled() {
        return this.getFlag(FLAG_SADDLE);
    }

    public void setEating(boolean pEating) {
        this.setFlag(FLAG_EATING, pEating);
    }

    public void setStanding(boolean pStanding) {
        if (pStanding) {
            this.setEating(false);
        }

        this.setFlag(FLAG_STANDING, pStanding);
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (pFallDistance > 1.0F) {
            this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }

        int i = this.calculateFallDamage(pFallDistance, pMultiplier);
        if (i <= 0) {
            return false;
        } else {
            this.hurt(pSource, (float) i);
            if (this.isVehicle()) {
                for (Entity entity : this.getIndirectPassengers()) {
                    entity.hurt(pSource, (float) i);
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    @Override
    protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
        return Mth.ceil((pDistance * 0.5F - 3.0F) * pDamageMultiplier);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean flag = super.hurt(pSource, pAmount);
        if (flag && this.random.nextInt(3) == 0) {
            this.standIfPossible();
        }

        return flag;
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() || this.isEating() || this.isStanding();
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override
    public void aiStep() {
        if (this.random.nextInt(200) == 0) {
            this.moveTail();
        }

        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {
            if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
                this.heal(1.0F);
            }

            if (this.canEatGrass()) {
                if (!this.isEating()
                        && !this.isVehicle()
                        && this.random.nextInt(300) == 0
                        && this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                    this.setEating(true);
                }

                if (this.isEating() && ++this.eatingCounter > 50) {
                    this.eatingCounter = 0;
                    this.setEating(false);
                }
            }
        }
    }

    public void standIfPossible() {
        if (this.canPerformRearing() && this.isEffectiveAi()) {
            this.standCounter = 1;
            this.setStanding(true);
        }
    }

    protected Vec2 getRiddenRotation(LivingEntity pEntity) {
        return new Vec2(pEntity.getXRot() * 0.5F, pEntity.getYRot());
    }
    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 pDirection, LivingEntity pPassenger) {
        double d0 = this.getX() + pDirection.x;
        double d1 = this.getBoundingBox().minY;
        double d2 = this.getZ() + pDirection.z;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(Pose pose : pPassenger.getDismountPoses()) {
            blockpos$mutableblockpos.set(d0, d1, d2);
            double d3 = this.getBoundingBox().maxY + 0.75;

            do {
                double d4 = this.level().getBlockFloorHeight(blockpos$mutableblockpos);
                if ((double)blockpos$mutableblockpos.getY() + d4 > d3) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(d4)) {
                    AABB aabb = pPassenger.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(d0, (double)blockpos$mutableblockpos.getY() + d4, d2);
                    if (DismountHelper.canDismountTo(this.level(), pPassenger, aabb.move(vec3))) {
                        pPassenger.setPose(pose);
                        return vec3;
                    }
                }

                blockpos$mutableblockpos.move(Direction.UP);
            } while(!((double)blockpos$mutableblockpos.getY() < d3));
        }

        return null;
    }
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
        Vec3 vec3 = getCollisionHorizontalEscapeVector(
                this.getBbWidth(), pLivingEntity.getBbWidth(), this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F)
        );
        Vec3 vec31 = this.getDismountLocationInDirection(vec3, pLivingEntity);
        if (vec31 != null) {
            return vec31;
        } else {
            Vec3 vec32 = getCollisionHorizontalEscapeVector(
                    this.getBbWidth(), pLivingEntity.getBbWidth(), this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F)
            );
            Vec3 vec33 = this.getDismountLocationInDirection(vec32, pLivingEntity);
            return vec33 != null ? vec33 : this.position();
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity pEntity, EntityDimensions pDimensions, float pPartialTick) {
        return super.getPassengerAttachmentPoint(pEntity, pDimensions, pPartialTick)
                .add(
                        new Vec3(0.0, 0.15 * (double)this.standAnimO * (double)pPartialTick, -0.7 * (double)this.standAnimO * (double)pPartialTick)
                                .yRot(-this.getYRot() * (float) (Math.PI / 180.0))
                );
    }

    @Override
    protected void positionRider(Entity pPassenger, MoveFunction pCallback) {
        super.positionRider(pPassenger, pCallback);
        if (pPassenger instanceof LivingEntity) {
            ((LivingEntity) pPassenger).yBodyRot = this.yBodyRot;
        }
    }

    @Override
    public @NotNull ClayHorseVariants getVariant() {
        return ClayHorseVariants.getById(entityData.get(VARIANT_ID_SYNC));
    }
    @Override
    public void setVariant(ClayHorseVariants pVariant) {
        this.entityData.set(VARIANT_ID_SYNC, pVariant.getId());
    }

    // Set Inventory
    public HorseWearableItemStack getArmor() {
        return armorList.get(ARMOR_INDEX);
    }

    public void setArmor(ItemStack stack) {
        setItemSlot(EquipmentSlot.BODY, stack);
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
        super.setItemSlot(pSlot, pStack);
        if (pSlot == EquipmentSlot.BODY) {
            armorList.set(ARMOR_INDEX, new HorseWearableItemStack(pStack));
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return pStack.is(ModTags.Items.CLAY_HORSE_ARMOR);
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack pStack) {
        HorseWearableItemStack horseWearableItemStack = new HorseWearableItemStack(pStack);
        if (horseWearableItemStack.effect() == null) {
            return ItemStack.EMPTY;
        }
        EquipmentSlot slot = horseWearableItemStack.getEquipmentSlot();
        if (!getItemBySlot(slot).isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (pStack.getCount() > 1) {
            ItemStack stackLeft = pStack.copyWithCount(1);
            this.setItemSlotAndDropWhenKilled(slot, stackLeft);
            return stackLeft;
        } else {
            this.setItemSlotAndDropWhenKilled(slot, pStack);
            return pStack;
        }
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource pDamageSource, float pDamageAmount) {
        if (!pDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(pDamageSource, pDamageAmount);
            pDamageAmount = CombatRules.getDamageAfterAbsorb(this, pDamageAmount, pDamageSource, this.getArmorValue() + getArmor().protection(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }

        return pDamageAmount;
    }
    @Override
    protected OptionalInt openMenuScreen(Player player) {
        return ClaySoldiersCommon.COMMON_HOOKS.openMenu(player,
                new SimpleMenuProvider((id, inventory, player1) -> new ClayHorseMenu(id, inventory, this), getInventoryName()),
                this.getId()
        );
    }

    public float getEatAnim(float pPartialTick) {
        return Mth.lerp(pPartialTick, this.eatAnimO, this.eatAnim);
    }
    public float getStandAnim(float pPartialTick) {
        return Mth.lerp(pPartialTick, this.standAnimO, this.standAnim);
    }
    public float getMouthAnim(float pPartialTick) {
        return Mth.lerp(pPartialTick, this.mouthAnimO, this.mouthAnim);
    }
    public boolean canPerformRearing() {
        return true;
    }
    public boolean canEatGrass() {
        return true;
    }


    @Override
    protected double getDefaultAttackReach() {
        return 0;
    }
    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }
    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }
    @Override
    public boolean onClimbable() {
        return false;
    }
    @Nullable
    public SoundEvent getAmbientStandSound() {
        return this.getAmbientSound();
    }
    public int getAmbientStandInterval() {
        return this.getAmbientSoundInterval();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pPose) {
        if (Pose.SITTING == pPose && !isPassenger()) {
            return super.getDefaultDimensions(pPose).scale(1f, 0.7f);
        }

        return super.getDefaultDimensions(pPose);
    }
}
