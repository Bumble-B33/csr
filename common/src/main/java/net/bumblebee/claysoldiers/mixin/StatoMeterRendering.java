package net.bumblebee.claysoldiers.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.statitem.StatItemRenderUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class StatoMeterRendering {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void renderItem(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci) {

        if (!player.isScoping() && item.is(ModItems.STATOMETER.get())) {
            boolean isMainhand = hand == InteractionHand.MAIN_HAND;
            HumanoidArm humanoidarm = isMainhand ? player.getMainArm() : player.getMainArm().getOpposite();
            StatItemRenderUtil.renderStatoMeter(poseStack, nodeCollector, packedLight, equippedProgress, humanoidarm, swingProgress, item);
            ci.cancel();
        }
    }
}
