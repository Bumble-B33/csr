package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.VampiricClayMob;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.function.Predicate;

public class VampireEyesLayer extends RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel> {
    private static final ResourceLocation EYES_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_eyes.png");
    private static final RenderType VAMPIRE_EYES = RenderType.entityTranslucentEmissive(EYES_LOCATION);
    private static final RenderType VAMPIRE_EYES_ACTIVE = RenderType.eyes(EYES_LOCATION);

    private final Predicate<AbstractClaySoldierEntity> shouldEyesGlow;

    public VampireEyesLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> renderer, Predicate<AbstractClaySoldierEntity> shouldEyesGlow) {
        super(renderer);
        this.shouldEyesGlow = shouldEyesGlow;
    }

    public static VampireEyesLayer forVampiricClayMob(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> renderer) {
        return new VampireEyesLayer(renderer, soldier -> ((VampiricClayMob) soldier).isNightForVampire());
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (shouldEyesGlow.test(pLivingEntity)) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(VAMPIRE_EYES_ACTIVE);
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 0xF00000, OverlayTexture.NO_OVERLAY);
        } else {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(VAMPIRE_EYES);
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 0xF00000, OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color(0x7F, 0xFFFFFF));
        }
    }
}
