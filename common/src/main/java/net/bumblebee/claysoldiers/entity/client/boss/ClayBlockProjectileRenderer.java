package net.bumblebee.claysoldiers.entity.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.boss.ClayBlockProjectileEntity;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayBlockProjectileRenderState;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ClayBlockProjectileRenderer extends EntityRenderer<ClayBlockProjectileEntity, ClayBlockProjectileRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_block_projectile"), "main");

    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/clay.png");
    private final SkullModel model;

    public ClayBlockProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(LAYER_LOCATION));
    }

    public static LayerDefinition createClayBlockLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(2)), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    protected int getBlockLightLevel(ClayBlockProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(ClayBlockProjectileRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        var clientSoldier = renderState.clientClaySoldierEntity;
        if (clientSoldier != null) {
            clientSoldier.render(renderState.partialRot, poseStack, nodeCollector, cameraRenderState);
        } else {
            renderBlock(renderState, poseStack, nodeCollector);
        }
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
    }

    private void renderBlock(ClayBlockProjectileRenderState entity, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        poseStack.pushPose();

        float blockSize = entity.size;
        poseStack.scale(-blockSize, -blockSize, blockSize);
        RenderType renderType = this.model.renderType(TEXTURE_LOCATION);

        var state = new SkullModelBase.State();
        state.animationPos = 0f;
        state.yRot = entity.rot + entity.partialRot;
        state.xRot = entity.rot + entity.partialRot;
        nodeCollector.submitModel(this.model, state, poseStack, renderType, entity.lightCoords, OverlayTexture.NO_OVERLAY, entity.outlineColor, null);
        poseStack.popPose();

        entity.rot += 1;
    }

    @Override
    public ClayBlockProjectileRenderState createRenderState() {
        return new ClayBlockProjectileRenderState();
    }

    @Override
    public void extractRenderState(ClayBlockProjectileEntity clayBlock, ClayBlockProjectileRenderState reusedState, float partialTick) {
        super.extractRenderState(clayBlock, reusedState, partialTick);
        reusedState.size = clayBlock.getBlockSize();
        reusedState.partialRot = partialTick;
        reusedState.rot += 1;
        reusedState.clientClaySoldierEntity = clayBlock.getClientSoldier();
        clayBlock.clientTick(partialTick);
    }

}
