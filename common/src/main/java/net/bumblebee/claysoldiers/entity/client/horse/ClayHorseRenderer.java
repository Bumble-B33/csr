package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.client.ClayMobStatusRenderlayer;
import net.bumblebee.claysoldiers.entity.client.SlimeRootLayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityAttachment;

public class ClayHorseRenderer extends MobRenderer<AbstractClayHorse, ClayHorseRenderState, ClayHorseModel> {
    private static final float SCALE = ClayHorseEntity.SCALE;
    public ClayHorseRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ClayHorseModel(pContext.bakeLayer(ClayHorseModel.LAYER_LOCATION)), 0.75F * SCALE);
        this.addLayer(new ClayHorseArmorLayer(this, pContext.getModelSet(), pContext.getEquipmentAssets()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, pContext.getEntityRenderDispatcher(), h -> h.isInSittingPose, h -> h.shouldShowWorkStatus, h -> h.workStatus, h -> h.statusAttachmentPoint));
        this.addLayer(new ClayHorseHornRenderLayer(this, pContext.getModelSet()));
        this.addLayer(SlimeRootLayer.ofClayHorse(this, pContext.getItemModelResolver()));
    }

    @Override
    public ResourceLocation getTextureLocation(ClayHorseRenderState clayHorseRenderState) {
        return clayHorseRenderState.variant.getTextureLocation();
    }

    @Override
    public ClayHorseRenderState createRenderState() {
        return new ClayHorseRenderState();
    }

    @Override
    public void extractRenderState(AbstractClayHorse clayHorse, ClayHorseRenderState clayHorseRenderState, float partialTick) {
        super.extractRenderState(clayHorse, clayHorseRenderState, partialTick);
        clayHorseRenderState.isRidden = clayHorse.isVehicle();
        clayHorseRenderState.eatAnimation = clayHorse.getEatAnim(partialTick);
        clayHorseRenderState.standAnimation = clayHorse.getStandAnim(partialTick);
        clayHorseRenderState.feedingAnimation = clayHorse.getMouthAnim(partialTick);
        clayHorseRenderState.animateTail = clayHorse.tailCounter > 0;
        clayHorseRenderState.isInSittingPose = clayHorse.isInSittingPose();
        clayHorseRenderState.variant = clayHorse.getVariant();
        clayHorseRenderState.isPassenger = clayHorse.isPassenger();

        clayHorseRenderState.workStatus = clayHorse.getWorkStatus();
        clayHorseRenderState.shouldShowWorkStatus = ClayMobRenderState.shouldShowWorkStatus(clayHorse);

        clayHorseRenderState.clayHorseArmor = clayHorse.getArmor();
        var effect = clayHorseRenderState.clayHorseArmor.effect();

        clayHorseRenderState.clayHorseArmorColor = effect == null ? -1 : effect.color().getColor(clayHorse, partialTick);
        clayHorseRenderState.onGround = clayHorse.onGround();
        clayHorseRenderState.statusAttachmentPoint = clayHorse.getAttachments().get(EntityAttachment.NAME_TAG, 0, clayHorse.getYRot(partialTick));
        clayHorseRenderState.isSlimeRooted = clayHorse.isSlimeRooted();
        clayHorseRenderState.hasHorn = !clayHorse.getHorn().isEmpty();
    }



    @Override
    protected void scale(ClayHorseRenderState renderState, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (renderState.isInSittingPose && !renderState.isPassenger) {
            poseStack.translate(0 ,0.5, 0);
        }
        super.scale(renderState, poseStack);
    }


}
