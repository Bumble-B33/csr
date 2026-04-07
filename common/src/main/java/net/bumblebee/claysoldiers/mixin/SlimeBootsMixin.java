package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.SlimeBootsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class SlimeBootsMixin {

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void noFallDamage(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (SlimeBootsUtil.canBounce(entity, true)) {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
            if (entity instanceof AbstractClaySoldierEntity claySoldier) {
                claySoldier.onBounce();
            }
            ci.cancel();
        }

    }

    @Inject(method = "updateEntityMovementAfterFallOn", at = @At("HEAD"), cancellable = true)
    private void jumpUp(BlockGetter level, Entity entity, CallbackInfo ci) {
        if (SlimeBootsUtil.canBounce(entity, false)) {
            Vec3 vec3 = entity.getDeltaMovement();
            if (vec3.y < 0.0) {
                double livingScale = entity instanceof LivingEntity ? 1.0 : 0.8;
                entity.setDeltaMovement(vec3.x, -vec3.y * livingScale, vec3.z);
            }
            ci.cancel();
        }
    }
}
