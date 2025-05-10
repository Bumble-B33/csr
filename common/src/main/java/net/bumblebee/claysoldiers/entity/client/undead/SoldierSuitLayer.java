package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SoldierSuitLayer extends RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel> {
    private static final RenderType ZOMBIE_SUIT = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/zombie.png"));
    private static final RenderType VAMPIRE_SUIT = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_suit.png"));


    private final RenderType suit;

    public SoldierSuitLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> pRenderer, RenderType suit) {
        super(pRenderer);
        this.suit = suit;
    }

    public static SoldierSuitLayer vampire(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, VAMPIRE_SUIT);
    }

    public static SoldierSuitLayer zombie(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, ZOMBIE_SUIT);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        VertexConsumer vertexconsumer = pBuffer.getBuffer(suit);
        this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F));
    }
}
