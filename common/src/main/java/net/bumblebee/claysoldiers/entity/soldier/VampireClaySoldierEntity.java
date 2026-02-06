package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.entity.VampireSubjugate;
import net.bumblebee.claysoldiers.entity.VampiricClayMob;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (isAlpha()) {
            output.putBoolean(ALPHA_TAG, true);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setIsAlpha(input.getBooleanOr(ALPHA_TAG, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ALPHA, false);
    }

    @Override
    protected boolean specificTargetPredicate(LivingEntity target, ServerLevel level) {
        if (target instanceof VampireSubjugate vampireSubjugate && vampireSubjugate.isSubjugateOf(this)) {
            return false;
        }
        return super.specificTargetPredicate(target, level);
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
    public void readItemPersistentData(ValueInput tag) {
        setIsAlpha(tag.getBooleanOr(ALPHA_TAG, false));
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
