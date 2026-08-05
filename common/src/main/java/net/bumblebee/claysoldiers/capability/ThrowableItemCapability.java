package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.entity.common.throwables.ClaySoldierSnowball;
import net.bumblebee.claysoldiers.entity.common.throwables.ClaySoldierThrowableItemEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A capability for custom item throwing behavior, where the default functionality is not enough
 */
public interface ThrowableItemCapability {
    ThrowableItemCapability DEFAULT = (level, shooter, holdableEffect) -> {
        var pr = new ClaySoldierThrowableItemEntity(level, shooter, holdableEffect);
        pr.setItem(holdableEffect.stack());
        return pr;
    };
    Function<ItemStackWithEffect, ThrowableItemCapability> SNOWBALL = (stack) -> ClaySoldierSnowball::new;
    Function<ItemStackWithEffect, ThrowableItemCapability> GLISTERING_MELON_SLICE = (stack) -> new ThrowHealingCapability();
    Function<ItemStackWithEffect, ThrowableItemCapability> ARROW = (stack) -> new ThrownArrowCapability();


    Map<Item, Function<ItemStackWithEffect, ThrowableItemCapability>> THROWABLE_ITEM_MAP = new HashMap<>(Map.of(
            Items.BOW, ARROW,
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
    Projectile createProjectile(Level level, LivingEntity shooter, ItemStackWithEffect holdableEffect);

    /**
     * Performs a ranged attack for the given {@code ClaySoldier} with the give {@code SoldierHoldableEffect}
     *
     * @param shooter        the performer fo the attack
     * @param target        the target of the attack
     * @param holdableEffect the thrown effect
     */
    default void performRangedAttack(LivingEntity shooter, ServerLevel level, LivingEntity target, ItemStackWithEffect holdableEffect, float pVelocity) {
        Projectile projectile = createProjectile(level, shooter, holdableEffect);

        double deltaX = target.getX() - shooter.getX();
        double deltaY = target.getY(0.3333333333333333) - projectile.getY();
        double deltaZ = target.getZ() - shooter.getZ();
        projectile.shoot(deltaX, deltaY, deltaZ, pVelocity * 2, 0);

        shooter.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(projectile);
    }
}
