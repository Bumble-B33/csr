package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.Predicate;

public class ClayMobStatusRenderlayer<T extends ClayMobEntity, E extends LivingEntityRenderState, M extends EntityModel<E>> extends RenderLayer<E, M> {
    private final Predicate<E> isInSittingPose;
    private final Predicate<E> showWorkStatus;
    private final Function<E, Component> getWorkStatus;
    private final Function<E, Vec3> attachmentPoint;

    public ClayMobStatusRenderlayer(MobRenderer<T, E, M> renderer, Predicate<E> isInSittingPose, Predicate<E> showWorkStatus, Function<E, Component> getWorkStatus, Function<E, Vec3> attachmentPoint) {
        super(renderer);
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
                Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState
        );

        pPoseStack.popPose();
    }
}
