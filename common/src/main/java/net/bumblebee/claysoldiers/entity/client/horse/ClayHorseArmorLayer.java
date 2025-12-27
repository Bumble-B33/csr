package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierArmorLayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

import java.util.List;
import java.util.function.Function;

public class ClayHorseArmorLayer extends RenderLayer<ClayHorseRenderState, ClayHorseModel> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse_armor"), "main");

    private final ClayHorseModel model;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<ClaySoldierArmorLayer.LayerTextureKey, ResourceLocation> layerTextureLookup;

    public ClayHorseArmorLayer(RenderLayerParent<ClayHorseRenderState, ClayHorseModel> renderer, EntityModelSet modelSet, EquipmentAssetManager equipmentAssets) {
        super(renderer);
        this.model = new ClayHorseModel(modelSet.bakeLayer(LAYER_LOCATION));
        this.equipmentAssets = equipmentAssets;

        this.layerTextureLookup = Util.memoize(p_386235_ -> p_386235_.layer().getTextureLocation(p_386235_.layerType()));
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ClayHorseRenderState clayHorseEntity, float v, float v1) {
        var effect = clayHorseEntity.clayHorseArmor.effect();
        if (effect == null) {
            return;
        }
        Equippable equippable = effect.getEquippable();


        if (equippable != null && !equippable.assetId().isEmpty()) {
            this.model.setupAnim(clayHorseEntity);
            this.renderLayers(EquipmentClientInfo.LayerType.HORSE_BODY, equippable.assetId().get(), model, pPoseStack, pBuffer, pPackedLight, effect.color().getColor(0, clayHorseEntity.ageInTicks));
        }

    }

    public void renderLayers(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model armorModel,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int color
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(equipmentAsset).getLayers(layerType);
        if (!list.isEmpty()) {
            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                    ResourceLocation armorTextureLocation =
                            this.layerTextureLookup.apply(new ClaySoldierArmorLayer.LayerTextureKey(layerType, equipmentclientinfo$layer));
                    VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(armorTextureLocation), false);
                    armorModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

            }


        }
    }
}
