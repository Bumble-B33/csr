package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SoldierSuitLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final RenderType ZOMBIE_SUIT = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/zombie.png"));
    private static final RenderType VAMPIRE_SUIT = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_suit.png"));

    private final RenderType suit;

    public SoldierSuitLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer, RenderType suit) {
        super(pRenderer);
        this.suit = suit;
    }

    public static SoldierSuitLayer vampire(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, VAMPIRE_SUIT);
    }

    public static SoldierSuitLayer zombie(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, ZOMBIE_SUIT);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClaySoldierRenderState claySoldierRenderState, float v, float v1) {
        VertexConsumer vertexconsumer = buffer.getBuffer(suit);
        this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(claySoldierRenderState, 0.0F));

    }
}
