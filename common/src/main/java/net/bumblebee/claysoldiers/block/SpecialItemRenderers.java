package net.bumblebee.claysoldiers.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntityRenderer;
import net.bumblebee.claysoldiers.entity.client.programmable.BatteryContainerModel;
import net.bumblebee.claysoldiers.entity.client.programmable.BatteryContainerRenderState;
import net.bumblebee.claysoldiers.item.BatteryItem;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class SpecialItemRenderers {
    public record EaselBlockSpecialRenderer(EaselBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean b, int i2) {
            renderer.submitItem(poseStack, submitNodeCollector, packedLight, packedOverlay);
        }


        @Override
        public void getExtents(Consumer<Vector3fc> consumer) {
            renderer.getExtents(consumer);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public SpecialModelRenderer<Void> bake(BakingContext bakingContext) {
                return new EaselBlockSpecialRenderer(new EaselBlockEntityRenderer(bakingContext.entityModelSet()));
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }

    public record HamsterWheelSpecialRenderer(HamsterWheelBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean b, int i2) {
            renderer.submitItem(poseStack, submitNodeCollector, packedLight, packedOverlay);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<Void> bake(BakingContext bakingContext) {
                return new HamsterWheelSpecialRenderer(new HamsterWheelBlockEntityRenderer(bakingContext.entityModelSet()));
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> consumer) {
            renderer.getExtents(consumer);
        }
    }

    public record ClayStaffSpecialRenderer(ClayStaffModel model) implements SpecialModelRenderer<ClayStaffRenderState> {

        @Override
        public void submit(@Nullable ClayStaffRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean foil, int outlineColor) {
            if (state == null) {
                return;
            }
            model.submitAsItem(state, poseStack, submitNodeCollector, packedLight, packedOverlay, foil, outlineColor);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> consumer) {
            model.getExtents(consumer);
        }

        @Override
        public ClayStaffRenderState extractArgument(ItemStack itemStack) {
            return ClayStaffModel.extractRenderState(itemStack);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked<ClayStaffRenderState> {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<ClayStaffRenderState> bake(BakingContext context) {
                return new ClayStaffSpecialRenderer(ClayStaffModel.create(context.entityModelSet()::bakeLayer));
            }
        }
    }

    public record BatteryContainerRenderer(BatteryContainerModel model) implements SpecialModelRenderer<BatteryContainerRenderState> {
        @Override
        public void submit(@Nullable BatteryContainerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean foil, int outlineColor) {
            if (state == null) {
                return;
            }
            model.submitAsItem(state, poseStack, nodeCollector, packedLight, packedOverlay, foil, outlineColor);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> consumer) {
            model.getExtents(consumer);
        }

        @Override
        public BatteryContainerRenderState extractArgument(ItemStack stack) {
            return new BatteryContainerRenderState(BatteryItem.getFillPercent(stack));
        }

        public record Unbaked(int height) implements SpecialModelRenderer.Unbaked<BatteryContainerRenderState> {
            public static final MapCodec<Unbaked> MAP_CODEC = Codec.INT.validate(i -> {
                try {
                    BatteryContainerModel.validateHeight(i, true);
                    return DataResult.success(i);
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }).xmap(Unbaked::new, Unbaked::height).fieldOf("size");

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<BatteryContainerRenderState> bake(BakingContext context) {
                return new BatteryContainerRenderer(BatteryContainerModel.createItem(context.entityModelSet(), height));
            }
        }
    }

}