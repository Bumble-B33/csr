package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerHurtMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void playerShootSoldier(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            try {
                if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
                    Player self = (Player) (Object) this;
                    ClaySoldierSpawnItem.onPlayerHurt(self, livingEntity);
                }
            } catch (RuntimeException e) {
                ClaySoldiersCommon.ERROR_HANDLER.error("Error mixin Player#hurtServer", e);
            }
        }
    }
}
