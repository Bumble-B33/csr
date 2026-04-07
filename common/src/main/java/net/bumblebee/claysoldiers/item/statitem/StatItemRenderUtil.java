package net.bumblebee.claysoldiers.item.statitem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class StatItemRenderUtil {
    public static void renderStatoMeter(
            PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, float equippedProgress, HumanoidArm arm, float swingProgress, ItemStack stack
    ) {
        float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(f * 0.125F, -0.125F, 0.0F);
        if (!Minecraft.getInstance().player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
            renderPlayerArm(poseStack, nodeCollector, packedLight, equippedProgress, swingProgress, arm);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(f * 0.51F, -0.08F + equippedProgress * -1.2F, -0.75F);
        float sqrtSwingProgress = Mth.sqrt(swingProgress);
        float f2 = Mth.sin(sqrtSwingProgress * (float) Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * Mth.sin(sqrtSwingProgress * (float) (Math.PI * 2));
        float f5 = -0.3F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(f * f3, f4 - 0.3F * f2, f5);
        poseStack.mulPose(Axis.XP.rotationDegrees(f2 * -45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(f * f2 * -30.0F));

        if (arm == HumanoidArm.RIGHT) {
            poseStack.mulPose(Axis.YP.rotationDegrees(80f));
            poseStack.mulPose(Axis.XP.rotationDegrees(20f));
            poseStack.translate(0.25f, -0.1f, 0.1f);
        } else {
            poseStack.mulPose(Axis.YN.rotationDegrees(80));
            poseStack.mulPose(Axis.XP.rotationDegrees(20));
            poseStack.translate(-0.25f, -0.1f, 0.1f);
        }

        ItemInHandRenderer r =  Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        r.renderItem(Minecraft.getInstance().player, stack, arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, nodeCollector, packedLight);


        poseStack.popPose();

    }

    private static void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, float equippedProgress, float swingProgress, HumanoidArm arm) {
        boolean flag = arm != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(swingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
        float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);
        poseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + equippedProgress * -0.6F, f4 + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = Minecraft.getInstance().player;
        poseStack.translate(f * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        poseStack.translate(f * 5.6F, 0.0F, 0.0F);
        AvatarRenderer<AbstractClientPlayer> avatarrenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(abstractclientplayer);
        ResourceLocation resourcelocation = abstractclientplayer.getSkin().body().texturePath();
        if (flag) {
            avatarrenderer.renderRightHand(
                    poseStack, nodeCollector, packedLight, resourcelocation, abstractclientplayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            avatarrenderer.renderLeftHand(poseStack, nodeCollector, packedLight, resourcelocation, abstractclientplayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }

}
