package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.function.Function;
import java.util.function.Predicate;

public class ClayMobStatusRenderlayer<T extends ClayMobEntity, E extends LivingEntityRenderState, M extends EntityModel<E>> extends RenderLayer<E, M> {
    private final Font font;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final Predicate<E> isInSittingPose;
    private final Predicate<E> showWorkStatus;
    private final Function<E, Component> getWorkStatus;
    private final Function<E, Vec3> attachmentPoint;

    public ClayMobStatusRenderlayer(MobRenderer<T, E, M> renderer, EntityRenderDispatcher entityRenderDispatcher, Predicate<E> isInSittingPose, Predicate<E> showWorkStatus, Function<E, Component> getWorkStatus, Function<E, Vec3> attachmentPoint) {
        super(renderer);
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.font = renderer.getFont();
        this.isInSittingPose = isInSittingPose;
        this.showWorkStatus = showWorkStatus;
        this.getWorkStatus = getWorkStatus;
        this.attachmentPoint = attachmentPoint;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, E clayMob, float yRot, float xRot) {
        if (!showWorkStatus.test(clayMob)) {
            return;
        }
        var workStatus = getWorkStatus.apply(clayMob);
        if (workStatus == null) {
            return;
        }

        var lastPose = poseStack.last().copy();
        poseStack.popPose();


        renderStatus(clayMob, poseStack, nodeCollector, packedLight, workStatus);

        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.mulPose(lastPose.pose());
    }

    private void renderStatus(E soldier, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, Component workStatus) {
        pPoseStack.pushPose();
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        if (!isInSittingPose.test(soldier)) {
            pPoseStack.translate(0, 0.45, 0);
        } else {
            pPoseStack.translate(0, 0.35, 0);
        }


        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.translate(0, 0.45, 0);

        nodeCollector.submitNameTag(
                pPoseStack,
                attachmentPoint.apply(soldier),
                0,
                workStatus,
                !soldier.isDiscrete,
                soldier.lightCoords,
                soldier.distanceToCameraSq,
                Minecraft.getInstance().gameRenderer.getLevelRenderState().cameraRenderState
        );


        //renderStatusName(soldier, workStatus, pPoseStack, nodeCollector, pPackedLight);

        pPoseStack.popPose();
    }

    private void renderStatusName(E renderState, Component displayName, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight) {
        Vec3 vec3 = attachmentPoint.apply(renderState);
        if (vec3 != null) {
            boolean flag = !renderState.isDiscrete;
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
            poseStack.mulPose(this.entityRenderDispatcher.camera.rotation());
            poseStack.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f = (float)(-font.width(displayName)) / 2.0F;
            int backGroundAlpha = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(
                    displayName, f, 0, -2130706433, false, matrix4f, null, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, backGroundAlpha, packedLight
            );
            if (flag) {
                font.drawInBatch(
                        displayName, f, 0, -1, false, matrix4f, null, Font.DisplayMode.NORMAL, 0, LightTexture.lightCoordsWithEmission(packedLight, 2)
                );
            }

            poseStack.popPose();
        }
    }
}
