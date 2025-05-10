package net.bumblebee.claysoldiers.entity.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.boss.ClayBlockProjectileEntity;
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

public class ClayBlockProjectileRenderer extends EntityRenderer<ClayBlockProjectileEntity> {
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
    public void render(ClayBlockProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        var clientSoldier = entity.getClientSoldier();
        if (clientSoldier != null) {
            entity.clientTick(partialTicks);
            clientSoldier.render(entityYaw, partialTicks, poseStack, buffer, packedLight);
        } else {
            renderBlock(entity, partialTicks, poseStack, buffer, packedLight);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBlock(ClayBlockProjectileEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float blockSize = entity.getBlockSize();
        poseStack.scale(-blockSize, -blockSize, blockSize);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.setupAnim(0.0F, entity.rot + partialTicks, entity.rot + partialTicks);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();


        entity.rot += 1;
    }

    @Override
    public ResourceLocation getTextureLocation(ClayBlockProjectileEntity entity) {
        return TEXTURE_LOCATION;
    }
}
