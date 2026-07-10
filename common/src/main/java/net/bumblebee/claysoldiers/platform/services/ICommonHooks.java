package net.bumblebee.claysoldiers.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

import java.util.OptionalInt;

public interface ICommonHooks {
    /**
     * @return {@code true} to prevent death
     */
    default boolean onLivingDeath(LivingEntity entity, DamageSource src) {
        return false;
    }

    OptionalInt openMenu(Player serverPlayer, MenuProvider menuProvider, int extraData);

    default boolean canEntityGrief(ServerLevel level, Entity livingEntity) {
        return level.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    default TriState canSustainPlant(LevelReader level, BlockPos pos, Direction direction, BlockState state) {
        return TriState.DEFAULT;
    }

    default boolean stateCanHydrate(BlockState state, LevelReader level, BlockPos pos, FluidState fluidState, BlockPos below) {
        return true;
    }
}
