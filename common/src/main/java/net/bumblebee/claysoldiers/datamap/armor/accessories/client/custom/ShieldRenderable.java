package net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.TextureAccessoryData;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class ShieldRenderable implements RenderableAccessory<TextureAccessoryData> {

    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, TextureAccessoryData data, PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pPackedLight, AccessoryRenderState claySoldier) {
        if (claySoldier.isFalling || claySoldier.isInSittingPose) {
            return;
        }

        boolean isRightHanded = claySoldier.mainArm == HumanoidArm.RIGHT;
        boolean right = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND) : claySoldier.hasShieldInHand(InteractionHand.OFF_HAND);
        boolean left = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.OFF_HAND) : claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND);

        pPoseStack.pushPose();
        if (right) {
            this.renderArmWithShield(renderedFrom, claySoldier.renderStateFrom, HumanoidArm.RIGHT, pPoseStack, pBuffer, pPackedLight, data.textureLocation());
        }
        if (left) {
            this.renderArmWithShield(renderedFrom, claySoldier.renderStateFrom, HumanoidArm.LEFT, pPoseStack, pBuffer, pPackedLight, data.textureLocation());
        }
        pPoseStack.popPose();
    }


    private void renderArmWithShield(IAccessoryRenderLayer renderLayer, AbstractClaySoldierRenderState claySoldier, HumanoidArm pArm, PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pPackedLight, Identifier textureLocation) {
        pPoseStack.pushPose();
        renderLayer.getSoldierModel().translateToHand(claySoldier, pArm, pPoseStack);
        boolean leftHand = pArm == HumanoidArm.LEFT;

        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        if (leftHand) {
            pPoseStack.translate(-0.55f, 0, 0.3);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180f));
        } else {
            pPoseStack.translate(-0.55f, 0, -0.3);
        }
        renderShieldModel(renderLayer, claySoldier, pPoseStack, pBuffer, pPackedLight, textureLocation);
        pPoseStack.popPose();

    }

    private void renderShieldModel(IAccessoryRenderLayer renderLayer, AbstractClaySoldierRenderState claySoldier, PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pPackedLight, Identifier textureLocation) {
        pPoseStack.pushPose();
        pPoseStack.scale(1.0F, -1.0F, -1.0F);
        pBuffer.submitModel(
                renderLayer.getShieldModel(),
                claySoldier,
                pPoseStack,
                RenderTypes.entitySolid(textureLocation),
                pPackedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                claySoldier.outlineColor,
                null
                );

        pPoseStack.popPose();
    }

}
