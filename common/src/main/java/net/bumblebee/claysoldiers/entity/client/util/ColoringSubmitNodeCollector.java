package net.bumblebee.claysoldiers.entity.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ColoringSubmitNodeCollector implements SubmitNodeCollector {
    private final SubmitNodeCollector nodeCollector;
    private final int color;
    private final Predicate<Model<?>> condition;

    public ColoringSubmitNodeCollector(SubmitNodeCollector nodeCollector, int color, Predicate<Model<?>> condition) {
        this.nodeCollector = nodeCollector;
        this.color = color;
        this.condition = condition;
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S s, PoseStack poseStack, RenderType renderType,
            int packedLight,
            int packedOverlay,
            int colorUnused,
            @Nullable TextureAtlasSprite textureAtlasSprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        nodeCollector.submitModel(
                model,
                s,
                poseStack,
                renderType,
                packedLight,
                packedOverlay,
                condition.test(model) ? color : colorUnused,
                textureAtlasSprite,
                outlineColor,
                crumblingOverlay
        );
    }

    @Override
    public OrderedSubmitNodeCollector order(int i) {
        return nodeCollector.order(i);
    }

    @Override
    public void submitShadow(PoseStack poseStack, float v, List<EntityRenderState.ShadowPiece> list) {
        nodeCollector.submitShadow(poseStack, v, list);
    }


    @Override
    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean b, int i1, double v, CameraRenderState cameraRenderState) {
        nodeCollector.submitNameTag(poseStack, vec3, i, component, b, i1, v, cameraRenderState);
    }

    @Override
    public void submitText(
            PoseStack poseStack,
            float v,
            float v1,
            FormattedCharSequence formattedCharSequence,
            boolean b,
            Font.DisplayMode displayMode,
            int i,
            int i1,
            int i2,
            int i3
    ) {
        nodeCollector.submitText(
                poseStack,
                v,
                v1,
                formattedCharSequence,
                b,
                displayMode,
                i,
                i1,
                i2,
                i3
        );
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
        nodeCollector.submitFlame(poseStack, entityRenderState, quaternionf);
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        nodeCollector.submitLeash(poseStack, leashState);
    }



    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int i1, @Nullable TextureAtlasSprite textureAtlasSprite, boolean b, boolean b1, int i2, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int i3) {
        nodeCollector.submitModelPart(modelPart, poseStack, renderType, i, i1, textureAtlasSprite, b, b1, i2, crumblingOverlay, i3);
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
        nodeCollector.submitMovingBlock(poseStack, movingBlockRenderState);
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> list, int[] ints, int i, int i1, int i2) {
        nodeCollector.submitBlockModel(poseStack, renderType, list, ints, i, i1, i2);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel blockStateModel, long l, int i) {
        nodeCollector.submitBreakingBlockModel(poseStack, blockStateModel, l, i);
    }

    @Override
    public void submitItem(PoseStack stack, ItemDisplayContext displayContext, int i, int i1, int i2, int[] ints, List<BakedQuad> list, ItemStackRenderState.FoilType foilType) {
        nodeCollector.submitItem(stack, displayContext, i, i1, i2, ints, list, foilType);
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
        nodeCollector.submitCustomGeometry(poseStack, renderType, customGeometryRenderer);
    }

    @Override
    public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
        nodeCollector.submitParticleGroup(particleGroupRenderer);
    }
}
