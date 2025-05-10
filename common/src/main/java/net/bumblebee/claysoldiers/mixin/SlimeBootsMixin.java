package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
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
    private void noFallDamage(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (entity instanceof ClaySoldierInventoryQuery soldier && soldier.allProperties().canBounce()) {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
            ci.cancel();
        }

    }

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    private void jumpUp(BlockGetter level, Entity entity, CallbackInfo ci) {
        if (entity instanceof ClaySoldierInventoryQuery soldier && soldier.allProperties().canBounce()) {
            Vec3 vec3 = entity.getDeltaMovement();
            if (vec3.y < 0.0) {
                double d0 = entity instanceof LivingEntity ? 1.0 : 0.8;
                entity.setDeltaMovement(vec3.x, -vec3.y * d0, vec3.z);
            }
            if (entity instanceof AbstractClaySoldierEntity claySoldier) {
                claySoldier.onBounce();
            }
            ci.cancel();
        }
    }
}
