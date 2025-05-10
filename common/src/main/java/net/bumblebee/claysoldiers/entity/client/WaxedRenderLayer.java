package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;

public class WaxedRenderLayer<T extends ClayMobEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public WaxedRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T clayMob, float limpSwing, float limpSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (clayMob.isWaxed()) {
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityGlint());
            this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(clayMob, 0.0F), FastColor.ARGB32.color(0x07, 0xe68a12));
        }
    }
}
