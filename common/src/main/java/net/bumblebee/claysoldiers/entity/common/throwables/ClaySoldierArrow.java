package net.bumblebee.claysoldiers.entity.common.throwables;

import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;

public class ClaySoldierArrow extends AbstractArrow {
    private static final String PROPERTIES_TAG = "SoldierProperties";
    private SoldierPropertyMap soldierProperties;


    public ClaySoldierArrow(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
        this.soldierProperties = SoldierPropertyMap.EMPTY_MAP;
        this.setBaseDamage(0);
    }

    public ClaySoldierArrow(LivingEntity mob, Level level, ItemStackWithEffect firedFromWeapon) {
        super(ModEntityTypes.CLAY_SOLDIER_ARROW.get(), mob, level, createDefaultPickupItem(), firedFromWeapon.stack());
        this.pickup = Pickup.DISALLOWED;
        var effect = firedFromWeapon.effect();
        if (effect != null) {
            this.soldierProperties = effect.properties();
        } else {
            this.soldierProperties = SoldierPropertyMap.EMPTY_MAP;
        }
        this.setCritArrow(false);
        this.setBaseDamage(0);

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store(PROPERTIES_TAG, SoldierPropertyMap.CODEC, soldierProperties);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read(PROPERTIES_TAG, SoldierPropertyMap.CODEC).ifPresent(p -> {
            soldierProperties = p;
        });
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    public static ItemStack createDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
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
            hitTarget.hurtOrSimulate(this.damageSources().thrown(this, soldier), soldierProperties.damage() + bonusDamage);
        }
    }
}
