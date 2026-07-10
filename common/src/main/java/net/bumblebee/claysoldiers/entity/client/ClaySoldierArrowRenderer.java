package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.throwables.ClaySoldierArrow;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class ClaySoldierArrowRenderer extends ArrowRenderer<ClaySoldierArrow, ArrowRenderState> {
    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    public ClaySoldierArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
    }

    @Override
    public void submit(ArrowRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
