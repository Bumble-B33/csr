package net.bumblebee.claysoldiers.entity.client.wraith;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClayMobStatusRenderlayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayWraithRenderState;
import net.bumblebee.claysoldiers.entity.client.util.ColoringSubmitNodeCollector;
import net.bumblebee.claysoldiers.entity.common.ClayWraithEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityAttachment;

public class WraithRenderer extends MobRenderer<ClayWraithEntity, ClayWraithRenderState, WraithModel> {
    private static final Identifier WRAITH_LOCATION = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_wraith/wraith.png");
    private static final float SCALE = ClayWraithEntity.WRAITH_SCALE;
    private static final int DEFAULT_ALPHA = (ClayWraithEntity.MAX_LIFE_SPAN * 3) - 10;

    public WraithRenderer(EntityRendererProvider.Context context) {
        super(context, new WraithModel(context.bakeLayer(WraithModel.LAYER_LOCATION)), 0.3f * SCALE);
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, w -> w.isInSittingPose, w -> w.shouldShowWorkStatus, w -> w.workStatus, w -> w.statusAttachmentPoint));

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
        clayWraithRenderState.clayTeamColor = clayWraithEntity.getClayTeamHolder().value().getColor(clayWraithEntity, partialTick);
        clayWraithRenderState.lifePoint = clayWraithEntity.getLifePoint();
        clayWraithRenderState.hasLimitedLife = clayWraithEntity.hasLimitedLife();
        clayWraithRenderState.isInSittingPose = clayWraithEntity.isInSittingPose();
        clayWraithRenderState.shouldShowWorkStatus = ClayMobRenderState.shouldShowWorkStatus(clayWraithEntity);
        clayWraithRenderState.workStatus = clayWraithEntity.getWorkStatus();
        clayWraithRenderState.statusAttachmentPoint = clayWraithEntity.getAttachments().get(EntityAttachment.NAME_TAG, 0, clayWraithEntity.getYRot(partialTick));
    }

    @Override
    public Identifier getTextureLocation(ClayWraithRenderState pEntity) {
        return WRAITH_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(ClayWraithEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public void submit(ClayWraithRenderState wraith, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        var coloringNodeCollector = new ColoringSubmitNodeCollector(nodeCollector, ARGB.color(getAlpha(wraith), wraith.clayTeamColor), m -> getModel() == m);
        super.submit(wraith, poseStack, coloringNodeCollector, cameraRenderState);
    }


    private int getAlpha(ClayWraithRenderState wraith) {
        if (wraith.hasLimitedLife) {
            return Math.max(0, (wraith.lifePoint * 3) - 10);
        }
        return DEFAULT_ALPHA;
    }
}