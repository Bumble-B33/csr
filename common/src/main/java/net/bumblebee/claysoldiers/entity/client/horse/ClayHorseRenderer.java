package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.client.ClayMobStatusRenderlayer;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClayHorseRenderer extends MobRenderer<AbstractClayHorse, ClayHorseModel> {
    private static final float SCALE = ClayHorseEntity.SCALE;
    public ClayHorseRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ClayHorseModel(pContext.bakeLayer(ClayHorseModel.LAYER_LOCATION)), 0.75F * SCALE);
        this.addLayer(new ClayHorseArmorLayer(this, pContext.getModelSet()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, pContext.getEntityRenderDispatcher(), pContext.getItemRenderer()));

    }

    @Override
    protected void scale(AbstractClayHorse pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        pPoseStack.scale(SCALE, SCALE, SCALE);
        if (pLivingEntity.isInSittingPose() && !pLivingEntity.isPassenger()) {
            pPoseStack.translate(0 ,0.5, 0);
        }
        super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClayHorse pEntity) {
        return pEntity.getVariant().getTextureLocation();
    }
}
