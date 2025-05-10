package net.bumblebee.claysoldiers.platform.services;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.OptionalInt;

public interface ICommonHooks {
    /**
     * @return {@code true} to prevent death
     */
    default boolean onLivingDeath(LivingEntity entity, DamageSource src) {
        return false;
    }

    OptionalInt openMenu(Player serverPlayer, MenuProvider menuProvider, int extraData);

    default boolean canEntityGrief(Level level, Entity livingEntity) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    default boolean shouldRiderSit(Entity vehicle) {
        return true;
    }

    boolean isBlueprintEnabled(FeatureFlagSet set);

    long getHamsterWheelSpeed();

}
