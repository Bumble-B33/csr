package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Predicate;

public class VampireEyesLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final ResourceLocation EYES_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_eyes.png");
    private static final RenderType VAMPIRE_EYES = RenderType.entityTranslucentEmissive(EYES_LOCATION);
    private static final RenderType VAMPIRE_EYES_ACTIVE = RenderType.eyes(EYES_LOCATION);

    private final Predicate<AbstractClaySoldierRenderState> shouldEyesGlow;

    public VampireEyesLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer, Predicate<AbstractClaySoldierRenderState> shouldEyesGlow) {
        super(renderer);
        this.shouldEyesGlow = shouldEyesGlow;
    }

    public static VampireEyesLayer forVampiricClayMob(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer) {
        return new VampireEyesLayer(renderer, soldier -> soldier.isNightForVampire);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierRenderState claySoldier, float v1, float v2) {
        if (shouldEyesGlow.test(claySoldier)) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(VAMPIRE_EYES_ACTIVE);
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 0xF00000, OverlayTexture.NO_OVERLAY);
        } else {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(VAMPIRE_EYES);
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 0xF00000, OverlayTexture.NO_OVERLAY,
                    ARGB.color(0x7F, 0xFFFFFF));
        }
    }
}
