package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierSnowball;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A capability for custom item throwing behaviour, where the default functionality is not enough
 */
public interface ThrowableItemCapability {
    BiFunction<ItemStack, @Nullable SoldierHoldableEffect, ThrowableItemCapability> SNOWBALL = (stack, effect) -> ClaySoldierSnowball::new;
    BiFunction<ItemStack, @Nullable SoldierHoldableEffect, ThrowableItemCapability> GLISTERING_MELON_SLICE = (stack, effect) -> new ThrowHealingCapability();

    Map<ItemLike, BiFunction<ItemStack, @Nullable SoldierHoldableEffect, ThrowableItemCapability>> THROWABLE_ITEM_MAP = new HashMap<>(Map.of(
            Items.SNOWBALL, SNOWBALL,
            Items.GLISTERING_MELON_SLICE, GLISTERING_MELON_SLICE
    ));

    /**
     * Creates a projectile for this ranged attack
     * @param shooter the attacker
     * @param holdableEffect the thrown effect
     * @return the projectile of this attack
     */
    @NotNull
    Projectile createProjectile(Level level, LivingEntity shooter, SoldierHoldableEffect holdableEffect);

    /**
     * Performs a ranged attack for the given {@code ClaySoldier} with the give {@code SoldierHoldableEffect}
     *
     * @param shooter        the performer fo the attack
     * @param pTarget        the target of the attack
     * @param holdableEffect the thrown effect
     */
    default void performRangedAttack(AbstractClaySoldierEntity shooter, Level level, LivingEntity pTarget, SoldierHoldableEffect holdableEffect, float pVelocity) {
        Projectile projectile = createProjectile(level, shooter, holdableEffect);

        double deltaX = pTarget.getX() - shooter.getX();
        double deltaY = pTarget.getY(0.3333333333333333) - projectile.getY();
        double deltaZ = pTarget.getZ() - shooter.getZ();
        projectile.shoot(deltaX, deltaY, deltaZ, pVelocity * 2, 0);

        shooter.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(projectile);
    }
}
