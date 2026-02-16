package net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.GliderAccessoryData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class GliderRenderable implements RenderableAccessory<GliderAccessoryData> {
    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, GliderAccessoryData data, PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pPackedLight, AccessoryRenderState claySoldier) {
        if (claySoldier.isFalling && !claySoldier.isInWater) {
            pPoseStack.pushPose();
            pPoseStack.translate(-1.2, 0.12, 0);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(90F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

            pPoseStack.scale(1.5f, 1.5f, 1.5f);
            claySoldier.gliderAccessory.submit(pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, claySoldier.outlineColor);
            pPoseStack.popPose();
        }
    }
}
