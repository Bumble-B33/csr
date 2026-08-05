package net.bumblebee.claysoldiers.block.chipassembler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.BatteryContentRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ChipAssemblerBlockEntityRenderer implements BlockEntityRenderer<ChipAssemblerBlockEntity, ChipAssemblerRenderState> {
    private static final Identifier CHIP_ASSEMBLER_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/chip_assembler.png");
    private static final RenderType RENDER_TYPE_BLOCK = RenderTypes.entityCutout(CHIP_ASSEMBLER_TEXTURE);

    public static final ModelLayerLocation BATTER_LAYER = createLayerLocation("battery");
    public static final ModelLayerLocation ARM_LAYER = createLayerLocation("arm");

    private final ItemModelResolver itemModelResolver;
    private final ModelPart batteryLeft;
    private final BatteryContentRenderer batteryContentRenderer;
    private final ChipAssemblerArmModel armModel;

    public ChipAssemblerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.batteryLeft = context.bakeLayer(BATTER_LAYER);
        this.batteryContentRenderer = BatteryContentRenderer.ofDefaultTexture(13, 13, 10);
        this.armModel = new ChipAssemblerArmModel(context.bakeLayer(ARM_LAYER));
    }

    @Override
    public ChipAssemblerRenderState createRenderState() {
        return new ChipAssemblerRenderState();
    }

    @Override
    public void submit(ChipAssemblerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.translate(-0.5f, 0, -0.5f);

        for (var entry : state.map.entrySet()) {
            ItemStackRenderState item = entry.getValue();
            if (item.isEmpty()) {
                continue;
            }
            ChipAssemblerInventory.Slot slot = entry.getKey();

            poseStack.pushPose();
            poseStack.translate(slot.x(), slot.height(), slot.z());
            poseStack.scale(slot.scale(), slot.scale(), slot.scale());

            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            entry.getValue().submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        nodeCollector.submitModel(armModel, state, poseStack, RENDER_TYPE_BLOCK, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);

        nodeCollector.submitModelPart(batteryLeft, poseStack, RENDER_TYPE_BLOCK, state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, state.breakProgress);
        batteryContentRenderer.submitBatteryContent(state.storedEnergy, state.maxEnergyStored, nodeCollector, poseStack, state.lightCoords);


        if (state.progress >= 0 || state.startProgress >= 0) {
            submitProgressBar((state.startProgress - state.progress) / state.startProgress, nodeCollector, poseStack, state.lightCoords);
        }

        poseStack.popPose();
    }

    @Override
    public void extractRenderState(ChipAssemblerBlockEntity blockEntity, ChipAssemblerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());

        blockEntity.getInventory().forEach((s, i) -> itemModelResolver.updateForTopItem(state.map.get(s), i, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, seed + s.ordinal()));
        state.yRot = blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING).getOpposite().toYRot();

        state.storedEnergy = blockEntity.getEnergyStorage(null).energyStored();
        state.maxEnergyStored = blockEntity.getEnergyStorage(null).maxEnergyStored();
        state.progress = blockEntity.getProgress() - partialTicks;
        state.startProgress = blockEntity.getProgressStart();

    }

    public static LayerDefinition createLeftBatteryLayer() {
        return createBatteryLayer(PartPose.offsetAndRotation(14, 0, 14, 0, 0, Mth.PI));
    }

    private static LayerDefinition createBatteryLayer(PartPose offset) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("battery", CubeListBuilder.create()
                .texOffs(52, 34).addBox(-2.0F, -10.02F, -1.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(56, 32).addBox(-1.0F, -10.5F, 0.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(60, 30).addBox(1.02F, -1.5F, 0.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(60, 32).addBox(1.02F, -3.5F, 0.0F, 1.0F, 1.0F, 1.0F), offset);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static ModelLayerLocation createLayerLocation(String key) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "chip_assembler"), key);
    }

    private static void submitProgressBar(float percent, SubmitNodeCollector nodeCollector, PoseStack poseStack, int packedLight) {
        if (percent <= 0) {
            return;
        }

        float pixelsShown = (6 * (1 - percent));

        float u0 = (20f + pixelsShown) / 64f;
        float u1 = (26f) / 64f;

        float v0 = 55f / 64f;
        float v1 = 58f / 64f;

        float x0 = 5 + pixelsShown;
        float x1 = 11;

        int overlay = OverlayTexture.NO_OVERLAY;

        nodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE_BLOCK, (pose, vc) -> {
            drawVertex(vc, pose, x0, 1.503f, 0.021f, u0, v0, 1, 0, 0, packedLight, overlay, 0xFFFFFFFF);
            drawVertex(vc, pose, x1,1.503f,0.021f, u1, v0, 1, 0, 0, packedLight, overlay, 0xFFFFFFFF);
            drawVertex(vc, pose, x1,4.033f, 1.633f, u1, v1, 1, 0, 0, packedLight, overlay, 0xFFFFFFFF);
            drawVertex(vc, pose, x0, 4.033f, 1.633f, u0, v1, 1, 0, 0, packedLight, overlay, 0xFFFFFFFF);
        });

    }

    private static void drawVertex(VertexConsumer builder, PoseStack.Pose pose,
                                  float x, float y, float z,
                                  float u, float v,
                                  float n0, float n1, float n2,
                                  int packedLight, int overlay, int color) {
        builder.addVertex(pose, x / 16f, y / 16f, z / 16f)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(packedLight)
                .setNormal(n0, n1, n2);
    }
}
