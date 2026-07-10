package net.bumblebee.claysoldiers.block.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class EaselBlockEntityRenderer implements BlockEntityRenderer<EaselBlockEntity, EaselBlockEntityRenderState> {
    public static final Identifier STAND_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/easel.png");
    private static final Identifier BLUEPRINT_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/easel_blueprint.png");
    private static final RenderType RENDER_TYPE_STAND = RenderTypes.entityCutout(STAND_TEXTURE);
    private static final RenderType RENDER_TYPE_BLUEPRINT = RenderTypes.entityCutout(BLUEPRINT_TEXTURE);

    public static final ModelLayerLocation STAND_LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_stand"), "main");
    public static final ModelLayerLocation BLUEPRINT_LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_blueprint"), "main");

    private final ModelPart stand;
    private final ModelPart blueprint;

    public EaselBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
        this(pContext.entityModelSet(), pContext.sprites());
    }

    public EaselBlockEntityRenderer(EntityModelSet set, SpriteGetter materials) {
        this.stand = set.bakeLayer(STAND_LAYER_LOCATION);
        this.blueprint = set.bakeLayer(BLUEPRINT_LAYER_LOCATION);
    }

    @Override
    public EaselBlockEntityRenderState createRenderState() {
        return new EaselBlockEntityRenderState();
    }

    @Override
    public void submit(EaselBlockEntityRenderState easelBlockEntityRenderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-easelBlockEntityRenderState.yRot));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        nodeCollector.submitModelPart(stand, poseStack, RENDER_TYPE_STAND, easelBlockEntityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, easelBlockEntityRenderState.breakProgress);

        if (!easelBlockEntityRenderState.hasBlueprintData) {
            poseStack.popPose();
            return;
        }

        if (easelBlockEntityRenderState.mirrored) {
            poseStack.scale(-1, 1, 1);
            poseStack.translate(-1, 0, 0);
        }
        nodeCollector.submitModelPart(blueprint, poseStack, RENDER_TYPE_BLUEPRINT, easelBlockEntityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, easelBlockEntityRenderState.breakProgress);

        poseStack.popPose();
        var settings = easelBlockEntityRenderState.settings;

        if (settings == null || !shouldShowOutline()) {
            return;
        }

        Vec3i offset = settings.getOutlineOffset();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotation(settings.getOutlineRotation()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        if (easelBlockEntityRenderState.mirrored) {
            poseStack.scale(-1, 1, 1);
            poseStack.translate(-1, 0, 0);
        }

        if (!easelBlockEntityRenderState.isFinished) {
            renderStructureOutline(poseStack, nodeCollector, easelBlockEntityRenderState.shape,
                    offset.getX(), offset.getY(), offset.getZ(),
                    easelBlockEntityRenderState.hasStarted ? 0 : 1, 1, 0,
                    0.4f);

        }

    }

    @Override
    public void extractRenderState(EaselBlockEntity blockEntity, EaselBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.yRot = blockEntity.getFacing().getOpposite().toYRot();
        var blueprintData = blockEntity.getBlueprintData();
        renderState.hasBlueprintData = blueprintData != null;
        renderState.mirrored = blockEntity.getMirror() != Mirror.NONE;
        renderState.settings = blockEntity.getTemplateSettings();
        renderState.isFinished = blockEntity.isFinished();
        renderState.shape = blueprintData != null ? blueprintData.getShape() : null;
        renderState.hasStarted = blockEntity.hasStarted();
    }

    public void submitItem(PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, int pPackedOverlay) {

        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        nodeCollector.submitModelPart(stand, pPoseStack, RENDER_TYPE_STAND, pPackedLight, pPackedOverlay, null);

        pPoseStack.popPose();

    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static void renderStructureOutline(PoseStack pPoseStack, SubmitNodeCollector nodeCollector, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        nodeCollector.submitCustomGeometry(pPoseStack, RenderTypes.lines(), new SubmitNodeCollector.CustomGeometryRenderer() {
            @Override
            public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
                float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;

                pShape.forAllEdges(
                        (x1, y1, z1, x2, y2, z2) -> {
                            float xLength = (float) (x2 - x1);
                            float yLength = (float) (y2 - y1);
                            float zLength = (float) (z2 - z1);
                            float f3 = Mth.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
                            xLength /= f3;
                            yLength /= f3;
                            zLength /= f3;
                            vertexConsumer.addVertex(pose, (float) (x1 + pX), (float) (y1 + pY), (float) (z1 + pZ))
                                    .setColor(pRed, pGreen, pBlue, pAlpha)
                                    .setLineWidth(width)
                                    .setNormal(pose, xLength, yLength, zLength);
                            vertexConsumer.addVertex(pose, (float) (x2 + pX), (float) (y2 + pY), (float) (z2 + pZ))
                                    .setColor(pRed, pGreen, pBlue, pAlpha)
                                    .setLineWidth(width)
                                    .setNormal(pose, xLength, yLength, zLength);
                        }
                );
            }
        });
    }

    private static boolean shouldShowOutline() {
        return ClaySoldiersClient.hasPlayerClayGogglesEquipped();
    }

    public void getExtents(Consumer<Vector3fc> output) {
        this.stand.getExtentsForGui(new PoseStack(), output);
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
