package net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.SkullAccessoryData;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SkullRenderable implements RenderableAccessory<SkullAccessoryData> {
    private static final float SKULL_SCALE = 1.1875F;

    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, SkullAccessoryData data, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier) {
        if (!claySoldier.skullAccessory.isEmpty()) {
            var type = data.getType();
            pPoseStack.pushPose();
            ClaySoldierModel model = renderedFrom.getSoldierModel();
            model.root().translateAndRotate(pPoseStack);
            model.getHead().translateAndRotate(pPoseStack);
            if (type != null) {
                pPoseStack.scale(SKULL_SCALE, -SKULL_SCALE, -SKULL_SCALE);
                pPoseStack.translate(-0.5, 0.0, -0.5);
                SkullModelBase skullmodelbase = renderedFrom.getSkullBase(type);
                if (skullmodelbase == null) {
                    return;
                }
                RenderType rendertype;
                var profile = data.getProfile();
                if (profile != null) {
                    rendertype = renderedFrom.getPlayerSkinRenderCache().getOrDefault(profile).renderType();
                } else if (claySoldier.wornHeadProfile != null) {
                    rendertype = renderedFrom.getPlayerSkinRenderCache().getOrDefault(claySoldier.wornHeadProfile).renderType();
                } else {
                    rendertype = SkullBlockRenderer.getSkullRenderType(type, null);
                }

                SkullBlockRenderer.submitSkull(claySoldier.wornHeadAnimationPos, pPoseStack, nodeCollector, pPackedLight, skullmodelbase, rendertype, claySoldier.outlineColor, null);
            } else {
                translateToHead(pPoseStack);
                claySoldier.skullAccessory.submit(pPoseStack, nodeCollector, pPackedLight, OverlayTexture.NO_OVERLAY, claySoldier.outlineColor);
            }

            pPoseStack.popPose();
        }
    }


    public static void translateToHead(PoseStack poseStack) {
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
    }
}
