package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ClayHorseWingsRenderLayer extends RenderLayer<ClayHorseRenderState, ClayHorseModel> {
    private static final ResourceLocation WINGS_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_horse/wings.png");
    private final ClayHorseWingsModel model;


    public ClayHorseWingsRenderLayer(RenderLayerParent<ClayHorseRenderState, ClayHorseModel> pRenderer, EntityModelSet entityModelSet) {
        super(pRenderer);
        this.model = new ClayHorseWingsModel(entityModelSet.bakeLayer(ClayHorseWingsModel.LAYER_LOCATION));

    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ClayHorseRenderState pLivingEntity, float v, float v1) {
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(WINGS_LOCATION));
        model.setUpWingAnim(pLivingEntity, pLivingEntity.ageInTicks);
        model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
    }
}
