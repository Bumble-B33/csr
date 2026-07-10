package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.entity.common.throwables.ClaySoldierArrow;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ThrownArrowCapability implements ThrowableItemCapability {
    @Override
    public @NotNull Projectile createProjectile(Level level, LivingEntity shooter, ItemStackWithEffect holdableEffect) {
        return new ClaySoldierArrow(shooter, level, holdableEffect);
    }

    @Override
    public void performRangedAttack(LivingEntity shooter, ServerLevel level, LivingEntity target, ItemStackWithEffect holdableEffect, float pVelocity) {
        double xd = target.getX() - shooter.getX();
        double yd = target.getY(0.33333333) - shooter.getY();
        double zd = target.getZ() - shooter.getZ();
        double distanceToTarget = Math.sqrt(xd * xd + zd * zd);

        Projectile.spawnProjectileUsingShoot(
                createProjectile(level, shooter, holdableEffect), level, ClaySoldierArrow.createDefaultPickupItem(), xd, yd + distanceToTarget * 0.02F, zd, 2F, 2
        );

    }
}
