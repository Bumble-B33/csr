package net.bumblebee.claysoldiers.entity.client.programmable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.function.Function;

public class ClaySoldierFishingRenderLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private final Function<AbstractClaySoldierRenderState, ItemStackRenderState> fishingRod;

    public ClaySoldierFishingRenderLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer, Function<AbstractClaySoldierRenderState, ItemStackRenderState> fishingRod) {
        super(renderer);
        this.fishingRod = fishingRod;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState renderState, float v, float v1) {
        if (!renderState.isFishingAnker) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(1, -0.3, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(6));

        poseStack.scale(3, 3, 3);

        fishingRod.apply(renderState).submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
