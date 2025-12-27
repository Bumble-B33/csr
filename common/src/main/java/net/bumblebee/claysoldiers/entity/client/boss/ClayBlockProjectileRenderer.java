package net.bumblebee.claysoldiers.entity.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.boss.ClayBlockProjectileEntity;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayBlockProjectileRenderState;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
    public void render(ClayBlockProjectileRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var clientSoldier = renderState.clientClaySoldierEntity;
        if (clientSoldier != null) {
            clientSoldier.render(renderState.partialRot, poseStack, buffer, packedLight);
        } else {
            renderBlock(renderState, poseStack, buffer, packedLight);
        }
        super.render(renderState, poseStack, buffer, packedLight);
    }



    private void renderBlock(ClayBlockProjectileRenderState entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float blockSize = entity.size;
        poseStack.scale(-blockSize, -blockSize, blockSize);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.setupAnim(0.0F, entity.rot + entity.partialRot, entity.rot + entity.partialRot);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
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
