package net.bumblebee.claysoldiers.entity.throwables;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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
        super(ModEntityTypes.CLAY_SOLDIER_THROWABLE_ITEM.get(), shooter, pLevel, thrownItem.stack());
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
            this.discard();
            return;
        }
        var effect = thrownItem.effect();
        if (effect == null) {
            hitTarget.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
            this.discard();
            return;
        }

        float powerScale = pResult.getEntity() instanceof ClayMobEntity ? AbstractClaySoldierEntity.NON_CLAY_MOB_POWER_MULTIPLIER : 1f;
        if (this.getOwner() instanceof LivingEntity thrower) {

            float bonusDamage = 0;
            for (var specialAttack : effect.getSpecialRangedAttacks()) {
                specialAttack.performAttackEffect(thrower, hitTarget);
                bonusDamage += specialAttack.getBonusDamage(thrower, hitTarget);
            }
            hitTarget.hurt(this.damageSources().thrown(this, thrower), (effect.damage() + bonusDamage) * powerScale);
            int secOnFireInTicks = effect.properties().setOnFire();
            if (secOnFireInTicks > 0) {
                hitTarget.igniteForTicks(adjustFireTicks(secOnFireInTicks, powerScale));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    private static int adjustFireTicks(int ticks, float power) {
        return Math.min(1, (int) (ticks * power));
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        return itemstack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemstack);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}
