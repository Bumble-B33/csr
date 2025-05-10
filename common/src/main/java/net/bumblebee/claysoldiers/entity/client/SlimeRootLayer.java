package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;

public abstract class SlimeRootLayer<T extends ClayMobEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ItemRenderer itemRenderer;

    public SlimeRootLayer(RenderLayerParent<T, M> pRenderer, ItemRenderer itemRenderer) {
        super(pRenderer);
        this.itemRenderer = itemRenderer;
    }

    protected void renderSlimeRoot(PoseStack poseStack, MultiBufferSource bufferSource, T clayMob, int packedLight) {
        if (clayMob.isSlimeRooted() && !clayMob.isPassenger()) {
            poseStack.pushPose();
            scalePoseStackRoot(poseStack);
            itemRenderer.renderStatic(Items.SLIME_BLOCK.getDefaultInstance(), ItemDisplayContext.FIXED, packedLight, getLightLevel(clayMob), poseStack, bufferSource, clayMob.level(), 1);
            poseStack.popPose();
        }
    }

    protected void scalePoseStackRoot(PoseStack poseStack) {
        poseStack.scale(1.5f, 1.5f, 1.5f);
        poseStack.translate(0f, 0.8f, 0f);
    }

    private static int getLightLevel(Entity pEntity) {
        final BlockPos pos = pEntity.getOnPos();
        final int bLight = pEntity.level().getBrightness(LightLayer.BLOCK, pos);
        final int sLight = pEntity.level().getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
