package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierThrownPotion;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of the {@link ThrowableItemCapability} for healing and regeneration potions.
 */
public class ThrowHealingCapability implements ThrowableItemCapability {
    @Override
    public @NotNull ThrownPotion createProjectile(Level level, LivingEntity shooter, SoldierHoldableEffect holdableEffect) {
        return new ClaySoldierThrownPotion(level, shooter);
    }

    @Override
    public void performRangedAttack(AbstractClaySoldierEntity shooter, Level level, LivingEntity pTarget, SoldierHoldableEffect holdableEffect, float pVelocity) {
        Vec3 targetMovement = pTarget.getDeltaMovement();
        double potionX = pTarget.getX() + targetMovement.x - shooter.getX();
        double potionY = pTarget.getEyeY() - 1.1F - shooter.getY();
        double potionZ = pTarget.getZ() + targetMovement.z - shooter.getZ();
        double distanceTarget = Math.sqrt(potionX * potionX + potionZ * potionZ);
        Holder<Potion> potion = Potions.REGENERATION;

        if (pTarget.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        }

        ThrownPotion thrownPotion = createProjectile(level, shooter, holdableEffect);
        thrownPotion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
        thrownPotion.setXRot(thrownPotion.getXRot() + 20.0F);
        thrownPotion.shoot(potionX, potionY + distanceTarget * 0.2, potionZ, 0.75F, 8.0F);
        if (!shooter.isSilent()) {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.WITCH_THROW, shooter.getSoundSource(), 1.0F, 0.8F + shooter.getRandom().nextFloat() * 0.4F
            );
        }

        level.addFreshEntity(thrownPotion);
    }
}
