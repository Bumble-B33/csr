package net.bumblebee.claysoldiers.entity.client.wraith;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.client.ClayMobStatusRenderlayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayWraithRenderState;
import net.bumblebee.claysoldiers.entity.client.util.ColoringSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityAttachment;

public class WraithRenderer extends MobRenderer<ClayWraithEntity, ClayWraithRenderState, WraithModel> {
    private static final ResourceLocation WRAITH_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_wraith/wraith.png");
    private static final ResourceLocation WRAITH_CHARGING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png");
    private static final float SCALE = ClayWraithEntity.WRAITH_SCALE;
    private static final int DEFAULT_ALPHA = (ClayWraithEntity.MAX_LIFE_SPAN * 3) - 10;

    public WraithRenderer(EntityRendererProvider.Context context) {
        super(context, new WraithModel(context.bakeLayer(WraithModel.LAYER_LOCATION)), 0.3f * SCALE);
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, context.getEntityRenderDispatcher(), w -> w.isInSittingPose, w -> w.shouldShowWorkStatus, w -> w.workStatus, w -> w.statusAttachmentPoint));

    }

    @Override
    protected void scale(ClayWraithRenderState pLivingEntity, PoseStack pPoseStack) {
        pPoseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ClayWraithRenderState createRenderState() {
        return new ClayWraithRenderState();
    }

    @Override
    public void extractRenderState(ClayWraithEntity clayWraithEntity, ClayWraithRenderState clayWraithRenderState, float partialTick) {
        super.extractRenderState(clayWraithEntity, clayWraithRenderState, partialTick);
        clayWraithRenderState.clayTeamColor = clayWraithEntity.getClayTeam().getColor(clayWraithEntity, partialTick);
        clayWraithRenderState.lifePoint = clayWraithEntity.getLifePoint();
        clayWraithRenderState.hasLimitedLife = clayWraithEntity.hasLimitedLife();
        clayWraithRenderState.isInSittingPose = clayWraithEntity.isInSittingPose();
        clayWraithRenderState.shouldShowWorkStatus = ClayMobRenderState.shouldShowWorkStatus(clayWraithEntity);
        clayWraithRenderState.workStatus = clayWraithEntity.getWorkStatus();
        clayWraithRenderState.statusAttachmentPoint = clayWraithEntity.getAttachments().get(EntityAttachment.NAME_TAG, 0, clayWraithEntity.getYRot(partialTick));
    }

    @Override
    public ResourceLocation getTextureLocation(ClayWraithRenderState pEntity) {
        return WRAITH_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(ClayWraithEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public void submit(ClayWraithRenderState wraith, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        var coloringNodeCollector = new ColoringSubmitNodeCollector(nodeCollector, ARGB.color(getAlpha(wraith), wraith.clayTeamColor));
        super.submit(wraith, poseStack, coloringNodeCollector, cameraRenderState);
    }

    /*@Override
    public void submit(ClayWraithRenderState wraith, PoseStack poseStack, SubmitNodeCollector buffer, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        if (wraith.hasPose(Pose.SLEEPING)) {
            Direction direction = wraith.bedOrientation;
            if (direction != null) {
                float f = wraith.eyeHeight - 0.1F;
                poseStack.translate((float)(-direction.getStepX()) * f, 0.0F, (float)(-direction.getStepZ()) * f);
            }
        }

        float f1 = wraith.scale;
        poseStack.scale(f1, f1, f1);
        this.setupRotations(wraith, poseStack, wraith.bodyRot, f1);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(wraith, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(wraith);
        boolean flag1 = this.isBodyVisible(wraith);
        boolean flag = !flag1 && !wraith.isInvisibleToPlayer;
        RenderType rendertype = this.getRenderType(wraith, flag1, flag, wraith.appearsGlowing);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = buffer.getBuffer(rendertype);
            int overlay = getOverlayCoords(wraith, this.getWhiteOverlayProgress(wraith));
            int color = ARGB.color(getAlpha(wraith), wraith.clayTeamColor);
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, overlay, color);

        }

        if (this.shouldRenderLayers(wraith)) {
            for (RenderLayer<ClayWraithRenderState, WraithModel> renderlayer : this.layers) {
                renderlayer.render(poseStack, buffer, packedLight, wraith, wraith.yRot, wraith.xRot);
            }
        }

        poseStack.popPose();
    }*/


    private int getAlpha(ClayWraithRenderState wraith) {
        if (wraith.hasLimitedLife) {
            return Math.max(0, (wraith.lifePoint * 3) - 10);
        }
        return DEFAULT_ALPHA;
    }
}