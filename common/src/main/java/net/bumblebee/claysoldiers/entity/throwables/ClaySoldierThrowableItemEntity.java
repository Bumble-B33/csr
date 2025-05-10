package net.bumblebee.claysoldiers.entity.throwables;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represent a projectile of an item thrown by {@code Clay Soldier}
 */
public class ClaySoldierThrowableItemEntity extends ThrowableItemProjectile {
    // Not null on server
    @Nullable
    private ItemStackWithEffect thrownItem;

    public ClaySoldierThrowableItemEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.thrownItem = null;
    }
    public ClaySoldierThrowableItemEntity(Level pLevel, LivingEntity shooter, @NotNull ItemStackWithEffect thrownItem) {
        super(ModEntityTypes.CLAY_SOLDIER_THROWABLE_ITEM.get(), shooter, pLevel);
        this.thrownItem = thrownItem;
    }


    @Override
    @SuppressWarnings("ConstantConditions")
    protected @NotNull Item getDefaultItem() {
        if (thrownItem == null) {
            // It says it is notnull but crashes if not check it
            if (getEntityData() == null) {
                return Items.STRUCTURE_VOID;
            }
            if (getItem().isEmpty()) {
                return Items.BARRIER;
            }
            return getItem().getItem();
        }
        return thrownItem.stack().getItem();
    }

    @Override
    public void setItem(ItemStack stack) {
        super.setItem(stack);
        if (thrownItem == null || thrownItem.is(stack.getItem())) {
            thrownItem = new ItemStackWithEffect(stack);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity hitTarget = pResult.getEntity();
        if (thrownItem == null || thrownItem.effect() == null) {
            hitTarget.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
            return;
        }
        var effect = thrownItem.effect();
        if (effect != null && this.getOwner() instanceof AbstractClaySoldierEntity soldier) {
            float bonusDamage = 0;
            for (var specialAttack : effect.getSpecialRangedAttacks()) {
                specialAttack.performAttackEffect(soldier, hitTarget);
                bonusDamage += specialAttack.getBonusDamage(soldier, hitTarget);
            }
            hitTarget.hurt(this.damageSources().thrown(this, soldier), effect.damage() + bonusDamage);
            int secOnFireInTicks = effect.properties().setOnFire();
            if (secOnFireInTicks > 0) {
                hitTarget.igniteForTicks(secOnFireInTicks);
            }
        }
    }
}
