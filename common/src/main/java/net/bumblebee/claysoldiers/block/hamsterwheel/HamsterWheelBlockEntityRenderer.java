package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.BatteryContentRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
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
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class HamsterWheelBlockEntityRenderer implements BlockEntityRenderer<HamsterWheelBlockEntity, HamsterWheelRenderState> {
    private static final Identifier HAMSTER_WHEEL_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/hamster_wheel.png");

    private static final RenderType RENDER_TYPE_BLOCK = RenderTypes.entityCutout(HAMSTER_WHEEL_TEXTURE);
    private static final float DEG_90 = Mth.PI / 2;
    private static final float WHEEL_SPEED = Mth.PI * 0.065f;

    public static final ModelLayerLocation STAND_LAYER_LOCATION = createLayerLocation("stand");
    public static final ModelLayerLocation POWER_LAYER_LOCATION = createLayerLocation("power");
    public static final ModelLayerLocation POWER_OVERLAY_LAYER_LOCATION = createLayerLocation("power_overlay");

    public static final ModelLayerLocation BATTERY_LEFT_LOCATION = createLayerLocation("battery_left");
    public static final ModelLayerLocation BATTERY_RIGHT_LOCATION = createLayerLocation("battery_right");


    private final BatteryContentRenderer batteryContentLeft;
    private final BatteryContentRenderer batteryContentRight;


    private final ModelPart stand;
    private final ModelPart powerConnection;
    private final ModelPart powerConnectionOverlay;

    private final ModelPart batteryLeft;
    private final ModelPart batteryRight;

    private final HamsterWheelModel wheelModel;


    public HamsterWheelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet());
    }

    public HamsterWheelBlockEntityRenderer(EntityModelSet modelSet) {
        this.stand = modelSet.bakeLayer(STAND_LAYER_LOCATION);
        this.powerConnection = modelSet.bakeLayer(POWER_LAYER_LOCATION);
        this.powerConnectionOverlay = modelSet.bakeLayer(POWER_OVERLAY_LAYER_LOCATION);
        this.wheelModel = new HamsterWheelModel(modelSet.bakeLayer(HamsterWheelModel.LAYER_LOCATION));
        this.batteryLeft = modelSet.bakeLayer(BATTERY_LEFT_LOCATION);
        this.batteryRight = modelSet.bakeLayer(BATTERY_RIGHT_LOCATION);
        this.batteryContentLeft = BatteryContentRenderer.ofDefaultTexture(2, 12, 6);
        this.batteryContentRight = BatteryContentRenderer.ofDefaultTexture(11, 12, 6);

    }

    @Override
    public HamsterWheelRenderState createRenderState() {
        return new HamsterWheelRenderState();
    }

    @Override
    public void extractRenderState(HamsterWheelBlockEntity blockEntity, HamsterWheelRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.rotation = (blockEntity.getRotationTick(partialTick) % 251) * WHEEL_SPEED;
        renderState.yRot = blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING).getOpposite().toYRot();
        renderState.hasEnergyStorage = blockEntity.hasEnergyStorage();
        renderState.hasSecondBattery = blockEntity.hasSecondBattery();
        renderState.partialTicks = partialTick;
        var data = blockEntity.getSoldierData();

        renderState.clientClaySoldierEntity = data != null ? data.getClientSoldier() : null;

        HamsterWheelEnergyStorage energy = blockEntity.getEnergyStorage(null);
        renderState.hasEnergyStorage = energy != null;
        if (energy != null) {
            renderState.energyStored = energy.energyStored();
            renderState.maxEnergyStored = energy.maxEnergyStored();
        }
    }

    @Override
    public void submit(HamsterWheelRenderState hamsterWheelRenderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        var profiler = Profiler.get();
        profiler.push("hamsterWheelRender");
        float yRot = hamsterWheelRenderState.yRot;

        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        nodeCollector.submitModel(wheelModel, hamsterWheelRenderState, poseStack, RENDER_TYPE_BLOCK, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, hamsterWheelRenderState.breakProgress);

        nodeCollector.submitModelPart(stand, poseStack, RENDER_TYPE_BLOCK, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, hamsterWheelRenderState.breakProgress);

        if (hamsterWheelRenderState.hasEnergyStorage) {
            profiler.push("batteryRender");

            nodeCollector.submitModelPart(powerConnectionOverlay, poseStack, BatteryContentRenderer.ENERGY_PORT_REDNER_OVERLAY_TYPE, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, ClaySoldiersCommon.ENERGY_HELPER.getEnergyColor(), hamsterWheelRenderState.breakProgress);
            nodeCollector.submitModelPart(powerConnection, poseStack, BatteryContentRenderer.ENERGY_PORT_REDNER_TYPE, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, hamsterWheelRenderState.breakProgress);
            nodeCollector.submitModelPart(batteryLeft, poseStack, RENDER_TYPE_BLOCK, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, hamsterWheelRenderState.breakProgress);
            batteryContentLeft.submitBatteryContent(hamsterWheelRenderState.energyStored, hamsterWheelRenderState.maxEnergyStored, nodeCollector, poseStack, hamsterWheelRenderState.lightCoords);

            if (hamsterWheelRenderState.hasSecondBattery) {
                nodeCollector.submitModelPart(batteryRight, poseStack, RENDER_TYPE_BLOCK, hamsterWheelRenderState.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, hamsterWheelRenderState.breakProgress);

                batteryContentRight.submitBatteryContent(hamsterWheelRenderState.energyStored, hamsterWheelRenderState.maxEnergyStored, nodeCollector, poseStack, hamsterWheelRenderState.lightCoords);
            }
            profiler.pop();
        }

        if (hamsterWheelRenderState.clientClaySoldierEntity != null) {
            profiler.push("soldierRender");

            poseStack.translate(0.5f, 0.1f, 0.5f);
            poseStack.mulPose(Axis.YP.rotation(DEG_90));
            hamsterWheelRenderState.clientClaySoldierEntity.submit(poseStack, nodeCollector, hamsterWheelRenderState.lightCoords, cameraRenderState, hamsterWheelRenderState.partialTicks);

            profiler.pop();
        }


        profiler.pop();
    }

    public void submitItem(PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, int pPackedOverlay) {
        float yRot = 0;
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);

        nodeCollector.submitModelPart(stand, pPoseStack, RENDER_TYPE_BLOCK, pPackedLight, pPackedOverlay,null);
        nodeCollector.submitModel(wheelModel, HamsterWheelRenderState.EMPTY, pPoseStack, RENDER_TYPE_BLOCK, pPackedLight, pPackedOverlay, 0, null);
    }

    public void getExtents(Consumer<Vector3fc> set) {
        PoseStack poseStack = new PoseStack();
        stand.getExtentsForGui(poseStack, set);
        wheelModel.getExtents(set);
    }

    public static LayerDefinition createStandLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition stand = partdefinition.addOrReplaceChild("stand", CubeListBuilder.create()
                        .texOffs(14, 15).addBox(-1.0F, -3.1715F, 0.5F, 2.0F, 9.0F, 2.0F)
                        .texOffs(0, 0).addBox(-2.0F, 5.3285F, -3.501F, 4.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(8F, 6.1715F, 11.5F, 0, 0, Mth.PI));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createPowerLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("power_connection", CubeListBuilder.create()
                        .texOffs(16, 0).addBox(-4.0F, -12.0F, 7.998F, 8.0F, 8.0F, 0.0F)
                        .texOffs(0, 16).addBox(-3.0F, -11.0F, 6.996F, 6.0F, 6.0F, 1.0F)
                        .texOffs(14, 16).addBox(-1.5F, -6.0F, 6.997F, 1.0F, 6.0F, 1.0F)
                        .texOffs(18, 16).addBox(0.5F, -6.0F, 6.997F, 1.0F, 6.0F, 1.0F)
                        .texOffs(22, 16).addBox(-0.75F, -8.535F, 6.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(8, 0.0F, 8, 0, 0, Mth.PI));


        return LayerDefinition.create(meshdefinition, 32, 32);
    }
    public static LayerDefinition createPowerOverlayLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("power_connection", CubeListBuilder.create()
                        .texOffs(16, 0).addBox(-4.0F, -12.0F, 7.999F, 8.0F, 8.0F, 0.0F),
                PartPose.offsetAndRotation(8, 0.0F, 8, 0, 0, Mth.PI));


        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition createBatteryBase(PartPose offsetAndRot) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("battery", CubeListBuilder.create()
                        .texOffs(20, 23).addBox(-2.0F, -6.01F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.01f))
                        .texOffs(8, 30).addBox(-1.0F, -6.5F, 0.0F, 1.0F, 1.0F, 1.0F)
                        .texOffs(4, 30).addBox(-3.011F, -0.75F, -0.75F, 1.0F, 1.0F, 1.0F)
                        .texOffs(0, 30).addBox(-3.011F, -0.75F, 0.75F, 1.0F, 1.0F, 1.0F),
                offsetAndRot
        );

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createBatteryLeft() {
        return createBatteryBase(PartPose.offsetAndRotation(3.0F, 0.001F, 13.0F, 0, 0, Mth.PI));
    }
    public static LayerDefinition createBatteryRight() {
        return createBatteryBase(PartPose.offsetAndRotation(13.0F, 0.001F, 14.0F, 0, Mth.PI, Mth.PI));
    }

    private static ModelLayerLocation createLayerLocation(String key) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel"), key);
    }
}
