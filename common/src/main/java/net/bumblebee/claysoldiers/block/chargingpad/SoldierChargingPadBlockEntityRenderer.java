package net.bumblebee.claysoldiers.block.chargingpad;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.entity.client.programmable.BatteryContainerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SoldierChargingPadBlockEntityRenderer implements BlockEntityRenderer<SoldierChargingPadBlockEntity, SoldierChargingPadBlockRenderState> {
    private static final float BATTERY_OFFSET = 13f / 16f;
    private final BatteryContainerModel battery;

    public SoldierChargingPadBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.battery = BatteryContainerModel.createBlock(context.entityModelSet(), 8);
    }

    @Override
    public SoldierChargingPadBlockRenderState createRenderState() {
        return new SoldierChargingPadBlockRenderState();
    }

    @Override
    public void extractRenderState(SoldierChargingPadBlockEntity blockEntity, SoldierChargingPadBlockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.energyFillPercent = blockEntity.getEnergyStorage(null).energyStored() / (float)  blockEntity.getEnergyStorage(null).maxEnergyStored();
        state.yRot = blockEntity.getBlockState().getValue(SoldierChargingPadBlock.FACING).getOpposite().toYRot();
    }

    @Override
    public void submit(SoldierChargingPadBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.translate(-0.5f, 0, -0.5f);
        poseStack.pushPose();

        poseStack.translate(BATTERY_OFFSET, 0, BATTERY_OFFSET);

        battery.submitAsBlock(state.energyFillPercent, poseStack, submitNodeCollector, state.lightCoords, 0, state.breakProgress);

        poseStack.popPose();

        poseStack.popPose();
    }

}
