package net.bumblebee.claysoldiers.entity.client.programmable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class BatteryRenderLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    public BatteryRenderLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState renderState, float v, float v1) {
        if (!renderState.holdsBattery) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(2.5f, 2.5f, 2.5f);
        poseStack.mulPose(Axis.XP.rotation(Mth.PI));
        poseStack.translate(0, -0.1f, 0.15f);

        renderState.carriedItemRenderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
        poseStack.popPose();

    }
}
