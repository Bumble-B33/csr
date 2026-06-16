package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.platform.services.ICommonHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

import java.util.OptionalInt;

public class NeoForgeCommonHooks implements ICommonHooks {
    @Override
    public boolean onLivingDeath(LivingEntity entity, DamageSource src) {
        return CommonHooks.onLivingDeath(entity, src);
    }

    @Override
    public OptionalInt openMenu(Player serverPlayer, MenuProvider menuProvider, int extraData) {
        return serverPlayer.openMenu(menuProvider, buf -> buf.writeVarInt(extraData));
    }

    @Override
    public boolean canEntityGrief(ServerLevel level, Entity livingEntity) {
        return ICommonHooks.super.canEntityGrief(level, livingEntity) && EventHooks.canEntityGrief(level, livingEntity);
    }
}
