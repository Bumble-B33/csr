package net.bumblebee.claysoldiers.entity.throwables;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;

public class ClaySoldierSnowball extends Snowball {
    private static final String PROPERTIES_TAG = "SoldierProperties";
    private SoldierPropertyMap soldierProperties;

    public ClaySoldierSnowball(EntityType<? extends Snowball> entityType, Level level) {
        super(entityType, level);
        this.soldierProperties = SoldierPropertyMap.EMPTY_MAP;
    }

    public ClaySoldierSnowball(Level pLevel, LivingEntity shooter, ItemStackWithEffect stackWithEffect) {
        super(ModEntityTypes.CLAY_SOLDIER_SNOWBALL.get(), pLevel);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
        this.setOwner(shooter);
        var effect = stackWithEffect.effect();
        if (effect != null) {
            this.soldierProperties = effect.properties();
        } else {
            this.soldierProperties = SoldierPropertyMap.EMPTY_MAP;
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.store(PROPERTIES_TAG, SoldierPropertyMap.CODEC, soldierProperties);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        compound.read(PROPERTIES_TAG, SoldierPropertyMap.CODEC).ifPresent(p -> {
            soldierProperties = p;
        });
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
