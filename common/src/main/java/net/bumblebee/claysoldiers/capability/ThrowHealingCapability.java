package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.entity.common.throwables.ClaySoldierThrownPotion;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
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
    public @NotNull AbstractThrownPotion createProjectile(Level level, LivingEntity shooter, ItemStackWithEffect holdableEffect) {
        return new ClaySoldierThrownPotion(level, shooter);
    }

    @Override
    public void performRangedAttack(LivingEntity shooter, ServerLevel level, LivingEntity target, ItemStackWithEffect holdableEffect, float pVelocity) {
        Vec3 targetMovement = target.getDeltaMovement();
        double potionX = target.getX() + targetMovement.x - shooter.getX();
        double potionY = target.getEyeY() - 1.1F - shooter.getY();
        double potionZ = target.getZ() + targetMovement.z - shooter.getZ();
        double distanceTarget = Math.sqrt(potionX * potionX + potionZ * potionZ);
        Holder<Potion> potion = Potions.REGENERATION;

        if (target.getHealth() <= 4.0F) {
            potion = Potions.HEALING;
        }

        AbstractThrownPotion thrownPotion = createProjectile(level, shooter, holdableEffect);
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
