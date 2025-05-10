package net.bumblebee.claysoldiers.entity.throwables;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class ClaySoldierSnowball extends Snowball {
    private static final String PROPERTIES_TAG = "SoldierProperties";
    private SoldierPropertyMap soldierProperties;

    public ClaySoldierSnowball(EntityType<? extends Snowball> entityType, Level level) {
        super(entityType, level);
        this.soldierProperties = SoldierPropertyMap.EMPTY_MAP;
    }

    public ClaySoldierSnowball(Level pLevel, LivingEntity shooter, SoldierHoldableEffect holdableEffect) {
        super(ModEntityTypes.CLAY_SOLDIER_SNOWBALL.get(), pLevel);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
        this.setOwner(shooter);
        this.soldierProperties = holdableEffect.properties();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        SoldierPropertyMap.CODEC.encodeStart(NbtOps.INSTANCE, soldierProperties)
                .ifSuccess(tag -> compound.put(PROPERTIES_TAG, tag))
                .ifError(err -> ClaySoldiersCommon.LOGGER.error("Error Saving Properties for Clay Soldier Snowball {}", soldierProperties));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(PROPERTIES_TAG)) {
            SoldierPropertyMap.CODEC.parse(NbtOps.INSTANCE, compound)
                    .ifSuccess(s -> soldierProperties = s)
                    .ifError(err -> ClaySoldiersCommon.LOGGER.error("Error reading Properties for Clay Soldier Snowball"));
        }
    }

    @Override
    public void onHitEntity(EntityHitResult pResult) {
        Entity hitTarget = pResult.getEntity();

        if (this.getOwner() instanceof AbstractClaySoldierEntity soldier) {
            float bonusDamage = 0;
            for (var specialAttack : soldierProperties.specialAttacks(SpecialAttackType.MELEE_AND_RANGED, SpecialEffectCategory.HARMFUL)) {
                specialAttack.performAttackEffect(soldier, hitTarget);
                bonusDamage += specialAttack.getBonusDamage(soldier, hitTarget);
            }
            hitTarget.hurt(this.damageSources().thrown(this, soldier), soldierProperties.damage() + bonusDamage);
        }
    }

}
