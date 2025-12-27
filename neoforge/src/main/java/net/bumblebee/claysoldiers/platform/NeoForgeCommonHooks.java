package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.ConfigNeoForge;
import net.bumblebee.claysoldiers.platform.services.ICommonHooks;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.CommonHooks;

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
    public boolean isBlueprintEnabled(FeatureFlagSet set) {
        return set.contains(ClaySoldiersNeoForge.BLUEPRINT_FLAG);
    }

    @Override
    public long getHamsterWheelSpeed() {
        return ConfigNeoForge.HAMSTER_WHEEL_SPEED.get();
    }

}
