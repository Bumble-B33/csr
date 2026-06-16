package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;

public class WaxedRenderLayer<T extends ClayMobRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public WaxedRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, T clayMobRenderState, float v, float v1) {
        if (clayMobRenderState.isWaxed) {
            submitNodeCollector.order(1).submitModel(this.getParentModel(), clayMobRenderState, poseStack, RenderTypes.entityGlint(), packedLight, LivingEntityRenderer.getOverlayCoords(clayMobRenderState, 0.0F), ARGB.color(0x07, 0xe68a12), null, clayMobRenderState.outlineColor, null);
        }
    }
}
