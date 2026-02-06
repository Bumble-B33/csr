package net.bumblebee.claysoldiers.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntityRenderer;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

public class SpecialItemRenderers {
    public record EaselBlockSpecialRenderer(EaselBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {


        @Override
        public void submit(ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean b, int i2) {
            renderer.submitItem(poseStack, submitNodeCollector, packedLight, packedOverlay);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public @Nullable SpecialModelRenderer<?> bake(BakingContext bakingContext) {
                return new EaselBlockSpecialRenderer(new EaselBlockEntityRenderer(bakingContext.entityModelSet(), bakingContext.materials()));
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }

        @Override
        public void getExtents(Set<Vector3f> set) {
            renderer.getExtents(set);
        }
    }

    public record HamsterWheelSpecialRenderer(HamsterWheelBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {

        @Override
        public void submit(ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean b, int i2) {
            renderer.submitItem(poseStack, submitNodeCollector, packedLight, packedOverlay);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public @Nullable SpecialModelRenderer<?> bake(BakingContext bakingContext) {
                return new HamsterWheelSpecialRenderer(new HamsterWheelBlockEntityRenderer(bakingContext.entityModelSet(), bakingContext.materials()));
            }
        }

        @Override
        public void getExtents(Set<Vector3f> set) {
            renderer.getExtents(set);
        }

    }
    public record ClayStaffSpecialRenderer(ClayStaffModel model) implements SpecialModelRenderer<ItemStack> {

        @Override
        public void submit(@Nullable ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean b, int i2) {
            ClayStaffModel.renderAsItem(model, stack, itemDisplayContext, poseStack, submitNodeCollector, packedLight, packedOverlay);
        }

        @Override
        public void getExtents(Set<Vector3f> set) {
            model.getExtents(set);
        }

        @Override
        public ItemStack extractArgument(ItemStack itemStack) {
            return itemStack;
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<?> bake(BakingContext context) {
                return new ClayStaffSpecialRenderer(ClayStaffModel.create(context.entityModelSet()::bakeLayer));
            }
        }
    }
}