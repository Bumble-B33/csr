package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.entity.VampiricClayMob;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class VampireClaySoldierEntity extends UndeadClaySoldier implements VampiricClayMob {
    private static final EntityDataAccessor<Boolean> ALPHA = SynchedEntityData.defineId(VampireClaySoldierEntity.class, EntityDataSerializers.BOOLEAN);
    public static final String ALPHA_TAG = "Alpha";

    public VampireClaySoldierEntity(EntityType<? extends VampireClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.VAMPIRE);
    }

    public void setIsAlpha(boolean subjugate) {
        this.entityData.set(ALPHA, subjugate);
    }

    /**
     * Returns whether this {@code Vampire} is an Alpha.
     */
    public boolean isAlpha() {
        return entityData.get(ALPHA);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (isAlpha()) {
            pCompound.putBoolean(ALPHA_TAG, true);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains(ALPHA_TAG)) {
            setIsAlpha(true);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ALPHA, false);
    }

    @Override
    protected boolean specificTargetPredicate(LivingEntity target) {
        if (target instanceof VampireSubjugate vampireSubjugate && vampireSubjugate.isSubjugateOf(this)) {
            return false;
        }
        return super.specificTargetPredicate(target);
    }

    @Override
    protected double getSunFleeSpeed() {
        return 1.5D;
    }

    @Override
    protected void specializedAttack(Entity target) {
        if (isAlpha()) {
            heal(getPowerMultiplier());
            if (target instanceof VampireSubjugate vampire) {
                vampire.applyConversionEffect(this);
            }
        } else {
            heal(0.5f * getPowerMultiplier());
        }
    }

    @Override
    public void readItemPersistentData(CompoundTag tag) {
        if (tag.contains(ALPHA_TAG)) {
            setIsAlpha(tag.getBoolean(ALPHA_TAG));
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

    @Override
    public LevelAccessor getLevel() {
        return level();
    }
}
