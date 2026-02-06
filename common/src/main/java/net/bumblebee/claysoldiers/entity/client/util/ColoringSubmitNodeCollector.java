package net.bumblebee.claysoldiers.entity.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class ColoringSubmitNodeCollector implements SubmitNodeCollector {
    private final SubmitNodeCollector nodeCollector;
    private final int color;

    public ColoringSubmitNodeCollector(SubmitNodeCollector nodeCollector, int color) {
        this.nodeCollector = nodeCollector;
        this.color = color;
    }

    @Override
    public OrderedSubmitNodeCollector order(int i) {
        return nodeCollector.order(i);
    }

    @Override
    public void submitHitbox(PoseStack poseStack, EntityRenderState entityRenderState, HitboxesRenderState hitboxesRenderState) {
        nodeCollector.submitHitbox(poseStack, entityRenderState, hitboxesRenderState);
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
    public void submitText(PoseStack poseStack, float v, float v1, FormattedCharSequence formattedCharSequence, boolean b, Font.DisplayMode displayMode, int i, int i1, int i2, int i3) {
        nodeCollector.submitText(poseStack, v, v1, formattedCharSequence, b, displayMode, i, i1, i2, i3);
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
    public <S> void submitModel(
            Model<? super S> model,
            S renderState,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packOverlay,
            int color,
            @Nullable TextureAtlasSprite textureAtlasSprite,
            int outlineColor,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        nodeCollector.submitModel(
                model,
                renderState,
                poseStack,
                renderType,
                packedLight,
                packOverlay,
                this.color,
                textureAtlasSprite,
                outlineColor,
                crumblingOverlay
        );
    }

    @Override
    public void submitModelPart(
            ModelPart modelPart,
            PoseStack poseStack,
            RenderType renderType,
            int i,
            int i1,
            @Nullable TextureAtlasSprite textureAtlasSprite,
            boolean b,
            boolean b1,
            int color,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            int outlineColor
    ) {
        nodeCollector.submitModelPart(
                modelPart,
                poseStack,
                renderType,
                i,
                i1,
                textureAtlasSprite,
                b,
                b1,
                color,
                crumblingOverlay,
                outlineColor
        );
    }

    @Override
    public void submitBlock(
            PoseStack poseStack,
            BlockState blockState,
            int i,
            int i1,
            int i2
    ) {
        nodeCollector.submitBlock(poseStack, blockState, i, i1, i2);
    }

    @Override
    public void submitMovingBlock(
            PoseStack poseStack,
            MovingBlockRenderState movingBlockRenderState
    ) {
        nodeCollector.submitMovingBlock(poseStack, movingBlockRenderState);
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, BlockStateModel blockStateModel, float v, float v1, float v2, int i, int i1, int i2) {
        nodeCollector.submitBlockModel(poseStack, renderType, blockStateModel, v, v1, v2, i, i1, i2);
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext itemDisplayContext, int i, int i1, int i2, int[] ints, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
        nodeCollector.submitItem(poseStack, itemDisplayContext, i, i1, i2, ints, list, renderType, foilType);
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
