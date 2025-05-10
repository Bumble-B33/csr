package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class ShieldRenderable implements RenderableAccessory {
    public static final ResourceLocation STUDDED_SHIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/studded_clay_shield.png");
    public static final ResourceLocation SHIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/clay_shield.png");
    public static final Codec<ShieldRenderable> CODEC = ResourceLocation.CODEC.xmap(ShieldRenderable::new, s -> s.textureLocation);
    public static final StreamCodec<ByteBuf, ShieldRenderable> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(ShieldRenderable::new, s -> s.textureLocation);

    private final ResourceLocation textureLocation;

    public ShieldRenderable(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling) {
        if (isFalling || claySoldier.isInSittingPose()) {
            return;
        }

        boolean isRightHanded = claySoldier.getMainArm() == HumanoidArm.RIGHT;
        boolean right = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND) : claySoldier.hasShieldInHand(InteractionHand.OFF_HAND);
        boolean left = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.OFF_HAND) : claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND);

        pPoseStack.pushPose();
        if (right) {
            this.renderArmWithShield(renderedFrom, HumanoidArm.RIGHT, pPoseStack, pBuffer, pPackedLight);
        }
        if (left) {
            this.renderArmWithShield(renderedFrom, HumanoidArm.LEFT, pPoseStack, pBuffer, pPackedLight);
        }
        pPoseStack.popPose();
    }


    private void renderArmWithShield(IAccessoryRenderLayer renderLayer, HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        renderLayer.getSoldierModel().translateToHand(pArm, pPoseStack);
        boolean leftHand = pArm == HumanoidArm.LEFT;

        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        if (leftHand) {
            pPoseStack.translate(-0.55f, 0, 0.3);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180f));
        } else {
            pPoseStack.translate(-0.55f, 0, -0.3);
        }
        renderShieldModel(renderLayer, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();

    }

    private void renderShieldModel(IAccessoryRenderLayer renderLayer, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(textureLocation));

        renderLayer.getShieldModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);


        pPoseStack.popPose();
    }

}
