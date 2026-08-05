package net.bumblebee.claysoldiers.block.hammock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SugarCaneHammockBlockEntityRenderer implements BlockEntityRenderer<SugarCaneHammockBlockEntity, SugarCaneHammockRenderState> {
    private static final float DEG_90 = Mth.PI / 2f;
    private static final float FUDGE = 0.02f;
    private static final Identifier HAMMOCK_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/sugar_cane_hammock_part.png");
    public static final RenderType RENDER_TYPE = RenderTypes.entityCutout(HAMMOCK_TEXTURE);
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "sugar_cane_hammock"), "hammock");

    private final ModelPart hammock;

    public SugarCaneHammockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.hammock = context.bakeLayer(LAYER_LOCATION);
    }


    @Override
    public void submit(SugarCaneHammockRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        nodeCollector.submitModelPart(hammock, poseStack, RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY, null);

        renderLines(poseStack, nodeCollector, 0xFFc29d62, state.lightCoords);

        if (state.claySoldier != null) {
            renderSoldier(state.claySoldier, poseStack, nodeCollector, state.lightCoords, cameraRenderState, 0);
        }


        poseStack.popPose();
    }

    private void renderSoldier(FakeClaySoldierAccess access, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CameraRenderState cameraRenderState, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0.75f, 0.73f, 0.5f);
        poseStack.mulPose(Axis.ZP.rotation(DEG_90));
        poseStack.mulPose(Axis.YP.rotation(DEG_90));

        access.submit(poseStack, nodeCollector, packedLight, cameraRenderState, partialTicks);

        poseStack.popPose();
    }





    @Override
    public void extractRenderState(SugarCaneHammockBlockEntity blockEntity, SugarCaneHammockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        var data = blockEntity.getSoldierData();
        if (data != null) {
            state.claySoldier = data.getClientSoldier();
        } else {
            state.claySoldier = null;
        }
        state.yRot = blockEntity.getFacing().getOpposite().toYRot();
    }

    @Override
    public SugarCaneHammockRenderState createRenderState() {
        return new SugarCaneHammockRenderState();
    }

    public static LayerDefinition createHammockLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition hammock = partdefinition.addOrReplaceChild("hammock", CubeListBuilder.create()
                .texOffs(-4, 0).addBox(-4.5F, 0.0F, -2.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 4).addBox(-5.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 9).addBox(4.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 11.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    private static void renderLines(PoseStack poseStack, SubmitNodeCollector nodeCollector, int color, int packedLight) {
        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, vertexConsumer) -> {
            float y0 = 11;
            float y1 = 13.5f;

            renderLine(pose, vertexConsumer, 3f, y0, 6, 3.5f, y1, 3.5f, color, packedLight);
            renderLine(pose, vertexConsumer, 13f, y0, 6, 12.5f, y1, 3.5f, color, packedLight);

            renderLine(pose, vertexConsumer, 3f, y0, 10, 3.5f, y1, 12.5f, color, packedLight);
            renderLine(pose, vertexConsumer, 13f, y0, 10, 12.5f, y1, 12.5f, color, packedLight);

        });
    }

    private static void renderLine(final PoseStack.Pose pose, final VertexConsumer builder, float x0, float y0, float z0, float x1, float y1, float z1, int color, int packedLight) {
        float x0f = x0 / 16f;
        float y0f = y0 / 16f;
        float z0f = z0 / 16f;
        float x1f = x1 / 16f;
        float y1f = y1 / 16f;
        float z1f = z1 / 16f;

        float dx = x1f - x0f;
        float dz = z1f - z0f;

        float horizontalDistance = Mth.sqrt(dx * dx + dz * dz);

        float dxOff = 0;
        float dzOff = 0;
        if (horizontalDistance > 0.0f) {
            float offsetFactor = (1.0f / horizontalDistance) * FUDGE / 2.0f;
            dxOff = dz * offsetFactor;
            dzOff = dx * offsetFactor;
        }

        // Degenerate Start
        builder.addVertex(pose, x0f - dxOff, y0f, z0f + dzOff).setColor(color).setLight(packedLight);

        builder.addVertex(pose, x0f - dxOff, y0f, z0f + dzOff).setColor(color).setLight(packedLight);
        builder.addVertex(pose, x0f + dxOff, y0f, z0f - dzOff).setColor(color).setLight(packedLight);

        builder.addVertex(pose, x1f - dxOff, y1f, z1f + dzOff).setColor(color).setLight(packedLight);
        builder.addVertex(pose, x1f + dxOff, y1f, z1f - dzOff).setColor(color).setLight(packedLight);

        // Degenerate End
        builder.addVertex(pose, x1f + dxOff, y1f, z1f - dzOff).setColor(color).setLight(packedLight);
    }
}
