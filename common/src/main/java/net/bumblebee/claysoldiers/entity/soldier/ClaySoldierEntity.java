package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.VampireSubjugate;
import net.bumblebee.claysoldiers.init.ModEffects;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaySoldierEntity extends AbstractClaySoldierEntity implements VampireSubjugate {
    @Nullable
    private UUID vampOwnerUUID;
    @Nullable
    private ClayMobEntity cachedVampOwner;

    public ClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.NORMAL);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        addVampOwner(output);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        readVampOwner(input);
    }

    @Override
    public void setVampOwner(@Nullable ClayMobEntity pOwner) {
        this.vampOwnerUUID = pOwner != null ? pOwner.getUUID() : null;
        this.cachedVampOwner = pOwner;
    }

    @Nullable
    @Override
    public ClayMobEntity getVampOwner() {
        if (!hasVampiricConversionEffect()) {
            return null;
        }

        if (this.cachedVampOwner != null && !this.cachedVampOwner.isRemoved()) {
            return this.cachedVampOwner;
        } else if (this.vampOwnerUUID != null && this.level() instanceof ServerLevel serverlevel) {
            Entity entity = serverlevel.getEntity(this.vampOwnerUUID);
            if (entity instanceof ClayMobEntity clayMob) {
                cachedVampOwner = clayMob;
                return clayMob;
            }
            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasVampiricConversionEffect() {
        return hasEffect(ModEffects.VAMPIRE_CONVERSION);
    }

    @Override
    public void applyConversionEffect(ClayMobEntity source) {
        if (!hasVampiricConversionEffect()) {
            addEffect(new MobEffectInstance(ModEffects.VAMPIRE_CONVERSION, 200, 4));
        }
        setVampOwner(source);
    }

    private void addVampOwner(ValueOutput compound) {
        if (this.vampOwnerUUID != null || hasVampiricConversionEffect()) {
            compound.store(VAMPIRIC_OWNER_TAG, UUIDUtil.CODEC, vampOwnerUUID);
        }
    }

    private void readVampOwner(ValueInput compound) {
        vampOwnerUUID = compound.read(VAMPIRIC_OWNER_TAG, UUIDUtil.CODEC).orElse(null);
        this.cachedVampOwner = null;
    }

    @Override
    public void convertToVampire() {
        VampireClaySoldierEntity vampire = ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get().create(level(), EntitySpawnReason.CONVERSION);
        if (vampire != null) {
            vampire.snapTo(getX(), getY(), getZ(), getYRot(), getXRot());
            vampire.setIsAlpha(false);
            copyBasePropertiesTo(vampire, false);
            if (getVampOwner() != null) {
                vampire.setClayTeamType(getVampOwner().getClayTeamType());
            } else {
                vampire.setClayTeamType(this.getClayTeamType());
            }
            level().addFreshEntity(vampire);
        }
    }
}
