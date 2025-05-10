package net.bumblebee.claysoldiers.block.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EaselBlockEntityRenderer implements BlockEntityRenderer<EaselBlockEntity> {
    public static final ResourceLocation STAND_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/easel.png");
    private static final ResourceLocation BLUEPRINT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/easel_blueprint.png");
    private static final RenderType RENDER_TYPE_STAND = RenderType.entityCutoutNoCull(STAND_TEXTURE);
    private static final RenderType RENDER_TYPE_BLUEPRINT = RenderType.entityCutoutNoCull(BLUEPRINT_TEXTURE);

    public static final ModelLayerLocation STAND_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_stand"), "main");
    public static final ModelLayerLocation BLUEPRINT_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_blueprint"), "main");

    private final ModelPart stand;
    private final ModelPart blueprint;

    public EaselBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
        this.stand = pContext.bakeLayer(STAND_LAYER_LOCATION);
        this.blueprint = pContext.bakeLayer(BLUEPRINT_LAYER_LOCATION);
    }

    @Override
    public void render(EaselBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlueprintTemplateSettings settings = pBlockEntity.getTemplateSettings();
        BlueprintData data = pBlockEntity.getBlueprintData();
        float yRot = pBlockEntity.getFacing().getOpposite().toYRot();


        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        stand.render(pPoseStack, pBuffer.getBuffer(RENDER_TYPE_STAND), pPackedLight, pPackedOverlay, -1);

        if (data == null) {
            pPoseStack.popPose();
            return;
        }
        if (pBlockEntity.getMirror() != Mirror.NONE) {
            pPoseStack.scale(-1, 1, 1);
            pPoseStack.translate(-1, 0, 0);
        }
        blueprint.render(pPoseStack, pBuffer.getBuffer(RENDER_TYPE_BLUEPRINT), pPackedLight, pPackedOverlay, -1);
        pPoseStack.popPose();

        if (settings == null || !shouldShowOutline()) {
            return;
        }

        Vec3i offset = settings.getOutlineOffset();

        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotation(settings.getOutlineRotation()));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);

        if (pBlockEntity.getMirror() != Mirror.NONE) {
            pPoseStack.scale(-1, 1, 1);
            pPoseStack.translate(-1, 0, 0);
        }
        if (!pBlockEntity.isFinished()) {
            renderStructureOutline(pPoseStack, pBuffer.getBuffer(RenderType.lines()), data.getShape(),
                    offset.getX(), offset.getY(), offset.getZ(),
                    pBlockEntity.hasStarted() ? 0 : 1, 1, 0,
                    0.4f);

        }
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public boolean shouldRenderOffScreen(EaselBlockEntity pBlockEntity) {
        return true;
    }

    private static void renderStructureOutline(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        pShape.forAllEdges(
                (x1, y1, z1, x2, y2, z2) -> {
                    float xLength = (float) (x2 - x1);
                    float yLength = (float) (y2 - y1);
                    float zLength = (float) (z2 - z1);
                    float f3 = Mth.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
                    xLength /= f3;
                    yLength /= f3;
                    zLength /= f3;
                    pConsumer.addVertex(posestack$pose, (float) (x1 + pX), (float) (y1 + pY), (float) (z1 + pZ))
                            .setColor(pRed, pGreen, pBlue, pAlpha)
                            .setNormal(posestack$pose, xLength, yLength, zLength);
                    pConsumer.addVertex(posestack$pose, (float) (x2 + pX), (float) (y2 + pY), (float) (z2 + pZ))
                            .setColor(pRed, pGreen, pBlue, pAlpha)
                            .setNormal(posestack$pose, xLength, yLength, zLength);
                }
        );
    }

    private static boolean shouldShowOutline() {
        return ClaySoldiersClient.hasPlayerClayGogglesEquipped();
    }

    public static LayerDefinition createStandLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition stand = partdefinition.addOrReplaceChild("stand", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, 3.0F, -3.0F, 10.0F, 1.0F, 1.0F)
                        .texOffs(14, 2).addBox(-1.5F, -3.0F, -1.999F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(8.0F, 7.0F, 8.0F, -0.4363F, 0.0F, Mth.PI));

        PartDefinition back_r1 = stand.addOrReplaceChild("back_r1", CubeListBuilder.create().texOffs(8, 2).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition left_r1 = stand.addOrReplaceChild("left_r1", CubeListBuilder.create().texOffs(0, 2).addBox(0.0F, 0.0F, -1.0F, 1.0F, 16.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.5F, -1.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition right_r1 = stand.addOrReplaceChild("right_r1", CubeListBuilder.create().texOffs(4, 2).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 16.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.5F, -1.0F, 0.0F, 0.0F, 0.2618F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createBlueprintLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("blueprint",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -2.001F, 8.0F, 8.0F, 0.0F),
                PartPose.offsetAndRotation(8.0F, 7.0F, 8.0F, -0.4363F, 0.0F, Mth.PI));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}
