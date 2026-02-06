package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;

public abstract class SlimeRootLayer<T extends ClayMobRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public SlimeRootLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    protected void renderSlimeRoot(PoseStack poseStack, SubmitNodeCollector nodeCollector, T clayMob, int packedLight) {
        if (clayMob.slimeRoot && !clayMob.isPassenger) {
            poseStack.pushPose();
            scalePoseStackRoot(poseStack);
            //Todo
            //itemRenderer.renderStatic(Items.SLIME_BLOCK.getDefaultInstance(), ItemDisplayContext.FIXED, packedLight, clayMob.lightLevel, poseStack, bufferSource, Minecraft.getInstance().level, 1);
            var i = new ItemStackRenderState();

            poseStack.popPose();
        }
    }

    protected void scalePoseStackRoot(PoseStack poseStack) {
        poseStack.scale(1.5f, 1.5f, 1.5f);
        poseStack.translate(0f, 0.8f, 0f);
    }


}
