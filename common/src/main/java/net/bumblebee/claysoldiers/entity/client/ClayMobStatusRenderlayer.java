package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ClayMobStatusRenderlayer<E extends ClayMobEntity, M extends EntityModel<E>> extends RenderLayer<E, M> {
    private final MobRenderer<E, M> parent;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;

    public ClayMobStatusRenderlayer(MobRenderer<E, M> renderer, EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        super(renderer);
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.parent = renderer;
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ClayMobEntity clayMobEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!shouldShowStatus(clayMobEntity) || clayMobEntity.isVehicle()) {
            return;
        }
        var workStatus = clayMobEntity.getWorkStatus();
        if (workStatus == null) {
            return;
        }

        var lastPose = poseStack.last().copy();
        poseStack.popPose();

        renderStatus(clayMobEntity, poseStack, buffer, packedLight, partialTick, workStatus);

        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.mulPose(lastPose.pose());
    }

    private void renderStatus(ClayMobEntity soldier, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float pPartialTick, Component workStatus) {
        pPoseStack.pushPose();
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        if (!soldier.isInSittingPose()) {
            pPoseStack.translate(0, 0.45, 0);
        } else {
            pPoseStack.translate(0, 0.35, 0);
        }


        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.translate(0, 0.45, 0);
        renderStatus(soldier, workStatus, pPoseStack, pBuffer, pPackedLight, pPartialTick);

        pPoseStack.popPose();
    }

    private boolean shouldShowStatus(ClayMobEntity soldier) {
        var player = Minecraft.getInstance().player;
        return player.equals(soldier.getClayTeamOwner()) && ClaySoldiersClient.hasPlayerClayGogglesEquipped();
    }

    private void renderStatus(ClayMobEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        double distanceToSqr = entityRenderDispatcher.distanceToSqr(entity);
        if (ClaySoldiersClient.CLIENT_HOOKS.isNameplateInRenderDistance(entity, distanceToSqr)) {
            Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
            if (vec3 != null) {
                boolean isClayDiscrete = !entity.isDiscrete();
                poseStack.pushPose();
                poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
                poseStack.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = poseStack.last().pose();
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int backGroundColor = (int) (f * 255.0F) << 24;
                Font font = parent.getFont();
                float f1 = (float) (-font.width(displayName) / 2);
                font.drawInBatch(
                        displayName, f1, 0, 0x20FFFFFF, false, matrix4f, buffer, isClayDiscrete ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, backGroundColor, packedLight
                );
                if (isClayDiscrete) {
                    font.drawInBatch(displayName, f1, 0, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
                }

                poseStack.popPose();
            }
        }
    }
}
