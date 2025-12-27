package net.bumblebee.claysoldiers.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntityRenderer;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SpecialItemRenderers {
    public record EaselBlockSpecialRenderer(EaselBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {

        @Override
        public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1, boolean b) {
            renderer.render(poseStack, multiBufferSource, i, i1);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
                return new EaselBlockSpecialRenderer(new EaselBlockEntityRenderer(modelSet));
            }
        }

    }

    public record HamsterWheelSpecialRenderer(HamsterWheelBlockEntityRenderer renderer) implements NoDataSpecialModelRenderer {

        @Override
        public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1, boolean b) {
            renderer.render(poseStack, multiBufferSource, i, i1);
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
                return new HamsterWheelSpecialRenderer(new HamsterWheelBlockEntityRenderer(modelSet));
            }
        }

    }
    public record ClayStaffSpecialRenderer(ClayStaffModel model) implements SpecialModelRenderer<ItemStack> {

        @Override
        public void render(@Nullable ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean hasFoil) {
            ClayStaffModel.renderAsItem(model, stack, itemDisplayContext, poseStack, buffer, packedLight, packedOverlay);

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
            public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
                return new ClayStaffSpecialRenderer(ClayStaffModel.create(modelSet::bakeLayer));
            }
        }
    }

    public record ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel<?> model) implements SpecialModelRenderer<ItemStack> {

        @Override
        public void render(@Nullable ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, boolean hasFoil) {
            model.render(poseStack, buffer, packedLight, 0xFF00FF);
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
            public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
                return new ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel.createModel(modelSet::bakeLayer));
            }
        }
    }




}