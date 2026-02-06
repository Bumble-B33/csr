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
import net.minecraft.client.renderer.SubmitNodeCollector;
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
    private final ClayHorseModel model;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<ClaySoldierArmorLayer.LayerTextureKey, ResourceLocation> layerTextureLookup;

    public ClayHorseArmorLayer(RenderLayerParent<ClayHorseRenderState, ClayHorseModel> renderer, EntityModelSet modelSet, EquipmentAssetManager equipmentAssets) {
        super(renderer);
        this.model = new ClayHorseModel(modelSet.bakeLayer(ClayHorseModel.ARMOR_LAYER_LOCATION));
        this.equipmentAssets = equipmentAssets;

        this.layerTextureLookup = Util.memoize(p_386235_ -> p_386235_.layer().getTextureLocation(p_386235_.layerType()));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, ClayHorseRenderState clayHorseRenderState, float v, float v1) {
        var effect = clayHorseRenderState.clayHorseArmor.effect();
        if (effect == null) {
            return;
        }
        Equippable equippable = effect.getEquippable();


        if (equippable != null && !equippable.assetId().isEmpty()) {
            this.renderLayers(EquipmentClientInfo.LayerType.HORSE_BODY, equippable.assetId().get(), model, clayHorseRenderState, poseStack, submitNodeCollector, clayHorseRenderState.lightCoords, effect.color().getColor(0, clayHorseRenderState.ageInTicks), clayHorseRenderState.outlineColor);
        }
    }

    public <S> void renderLayers(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model<S> armorModel,
            S renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            int color,
            int outlineColor
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(equipmentAsset).getLayers(layerType);
        if (!list.isEmpty()) {
            int j = 1;

            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                    ResourceLocation armorTextureLocation =
                            this.layerTextureLookup.apply(new ClaySoldierArmorLayer.LayerTextureKey(layerType, equipmentclientinfo$layer));
                    nodeCollector.order(j++)
                        .submitModel(
                                armorModel,
                                renderState,
                                poseStack,
                                RenderType.armorCutoutNoCull(armorTextureLocation),
                                packedLight,
                                OverlayTexture.NO_OVERLAY,
                                color,
                                null,
                                outlineColor,
                                null
                        );
            }
        }
    }
}
