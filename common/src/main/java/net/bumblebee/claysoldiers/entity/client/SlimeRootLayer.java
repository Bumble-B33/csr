package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public class SlimeRootLayer<T extends EntityRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ItemStackRenderState slimeBlockRenderState;
    private final ItemModelResolver itemModelResolver;
    private final Predicate<T> isSlimeRooted;
    private final Predicate<T> isPassenger;


    public SlimeRootLayer(RenderLayerParent<T, M> pRenderer, ItemModelResolver itemModelResolver, Predicate<T> isSlimeRooted, Predicate<T> isPassenger) {
        super(pRenderer);
        this.itemModelResolver = itemModelResolver;
        this.isSlimeRooted = isSlimeRooted;
        this.isPassenger = isPassenger;
        this.slimeBlockRenderState = new ItemStackRenderState();
    }

    public static <C extends ClayMobRenderState, CM extends EntityModel<C>> SlimeRootLayer<C, CM> ofSoldier(RenderLayerParent<C, CM> pRenderer, ItemModelResolver itemModelResolver) {
        return new SlimeRootLayer<>(pRenderer, itemModelResolver, r -> r.slimeRoot, r -> r.isPassenger);
    }

    public static <C extends ClayHorseRenderState, CM extends EntityModel<C>> SlimeRootLayer<C, CM> ofClayHorse(RenderLayerParent<C, CM> pRenderer, ItemModelResolver itemModelResolver) {
        return new SlimeRootLayer<>(pRenderer, itemModelResolver, r -> r.isSlimeRooted, r -> r.isPassenger) {
            @Override
            protected void submitSlimeRoot(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, ItemStackRenderState slimeBlockRenderState) {
                poseStack.scale(1, 1, 0.7f);
                poseStack.translate(0.0f, 0 ,0.5f);
                slimeBlockRenderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
                poseStack.translate(0.0f, 0 ,-1.1f);
                slimeBlockRenderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }
        };
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, T clayMob, float xRot, float yRot) {
        if (isSlimeRooted.test(clayMob) && !isPassenger.test(clayMob)) {
            poseStack.pushPose();
            itemModelResolver.updateForTopItem(slimeBlockRenderState, Items.SLIME_BLOCK.getDefaultInstance(),
                    ItemDisplayContext.FIXED, null, null, 0);
            scalePoseStackRoot(poseStack);

            submitSlimeRoot(poseStack, nodeCollector, packedLight, slimeBlockRenderState);

            poseStack.popPose();
        }
    }

    protected void submitSlimeRoot(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, ItemStackRenderState slimeBlockRenderState) {
        slimeBlockRenderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
    }

    protected void scalePoseStackRoot(PoseStack poseStack) {
        poseStack.scale(1.5f, 1.5f, 1.5f);
        poseStack.translate(0f, 0.8f, 0f);
    }
}
