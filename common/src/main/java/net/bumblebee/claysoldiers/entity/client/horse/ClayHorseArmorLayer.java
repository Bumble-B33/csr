package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AnimalArmorItem;

public class ClayHorseArmorLayer extends RenderLayer<AbstractClayHorse, ClayHorseModel> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse_armor"), "main");

    private final ClayHorseModel model;

    public ClayHorseArmorLayer(RenderLayerParent<AbstractClayHorse, ClayHorseModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new ClayHorseModel(modelSet.bakeLayer(LAYER_LOCATION));
    }


    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClayHorse clayHorseEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        var effect = clayHorseEntity.getArmor().effect();
        if (effect == null) {
            return;
        }

        this.getParentModel().copyPropertiesTo(this.model);
        this.model.prepareMobModel(clayHorseEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
        this.model.setupAnim(clayHorseEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(((AnimalArmorItem) effect.armorItem()).getTexture()));
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, effect.color().getColor(clayHorseEntity, pPartialTicks));
    }
}
