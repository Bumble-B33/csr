package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.SoldierWearableEffect;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ClaySoldierArmorLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final Map<SoldierEquipmentSlot, Equippable> FALLBACK_EQUIPPABLE = new EnumMap<>(SoldierEquipmentSlot.class);
    static {
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.HEAD, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.CHEST, Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.LEGS, Equippable.builder(EquipmentSlot.LEGS).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.FEET, Equippable.builder(EquipmentSlot.FEET).setAsset(EquipmentAssets.IRON).build());

    }
    private final ArmorModelSet<ClaySoldierModel> armorModelSet;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<LayerTextureKey, ResourceLocation> layerTextureLookup;
    private final Function<TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

    public ClaySoldierArmorLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer, ArmorModelSet<ClaySoldierModel> armorModelSet, TextureAtlas atlasManager, EquipmentAssetManager equipmentAssets) {
        super(pRenderer);
        this.layerTextureLookup = Util.memoize(p_386235_ -> p_386235_.layer.getTextureLocation(p_386235_.layerType));
        this.trimSpriteLookup = Util.memoize(p_386234_ -> atlasManager.getSprite(p_386234_.spriteId()));
        this.equipmentAssets = equipmentAssets;
        this.armorModelSet = armorModelSet;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState claySoldierRenderState, float v, float v1) {
        this.submitArmorPiece(poseStack, nodeCollector, claySoldierRenderState, SoldierEquipmentSlot.CHEST, packedLight, this.getArmorModel(SoldierEquipmentSlot.CHEST));
        this.submitArmorPiece(poseStack, nodeCollector, claySoldierRenderState, SoldierEquipmentSlot.LEGS, packedLight, this.getArmorModel(SoldierEquipmentSlot.LEGS));
        this.submitArmorPiece(poseStack, nodeCollector, claySoldierRenderState, SoldierEquipmentSlot.FEET, packedLight, this.getArmorModel(SoldierEquipmentSlot.FEET));
        this.submitArmorPiece(poseStack, nodeCollector, claySoldierRenderState, SoldierEquipmentSlot.HEAD, packedLight, this.getArmorModel(SoldierEquipmentSlot.HEAD));
    }

    private void submitArmorPiece(PoseStack poseStack, SubmitNodeCollector nodeCollector, AbstractClaySoldierRenderState claySoldier, SoldierEquipmentSlot slot, int packedLight, ClaySoldierModel model) {
        SoldierWearableEffect wearableEffect = getWearableEffect(claySoldier, slot);
        if (wearableEffect == null) {
            return;
        }
        ResourceKey<EquipmentAsset> assetId = wearableEffect.getAssetId();
        if (assetId == null) {
            assetId = FALLBACK_EQUIPPABLE.get(slot).assetId().orElseThrow();
        }

        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(slot)
                ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                : EquipmentClientInfo.LayerType.HUMANOID;

        int key = 1;
        if (wearableEffect.shouldRenderArmor()) {
            int color = claySoldier.offsetColor;
            if (!wearableEffect.isAffectedByOffsetColor() || color == -1) {
                color = wearableEffect.getColorHelper().getColor(claySoldier.id, claySoldier.ageInTicks);
            }

            key = renderArmorLayers(layerType, assetId, model, claySoldier, poseStack, nodeCollector, packedLight, color, false);
        }
        for (SoldierWearableEffect.TrimHolder trimHolder : wearableEffect.getArmorTrims()) {
            renderTrims(layerType, assetId, model, claySoldier, trimHolder.trim(), poseStack, nodeCollector, packedLight, trimHolder.color().getColor(claySoldier.id, claySoldier.ageInTicks), key);
        }
    }

    private ClaySoldierModel getArmorModel(SoldierEquipmentSlot pSlot) {
        return armorModelSet.get(Objects.requireNonNull(SoldierEquipmentSlot.convertToSlot(pSlot), "Cannot Render For SlotType " + pSlot));
    }

    private boolean usesInnerModel(SoldierEquipmentSlot pSlot) {
        return pSlot == SoldierEquipmentSlot.LEGS;
    }

    @Nullable
    private SoldierWearableEffect getWearableEffect(AbstractClaySoldierRenderState claySoldier, SoldierEquipmentSlot slot) {
        ItemStackWithEffect stackWithEffect = claySoldier.getItemBySlot(slot);

        if (stackWithEffect == null || stackWithEffect.isEmpty() || claySoldier.gliderSlot == slot) {
            return null;
        }
        return stackWithEffect.wearableEffectMap().wearableEffect(slot);
    }

    private int renderArmorLayers(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            ClaySoldierModel armorModel,
            AbstractClaySoldierRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            int color,
            boolean foil
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(equipmentAsset).getLayers(layerType);
        int j = 1;

        if (!list.isEmpty()) {
            boolean shouldRenderFoil = foil;

            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                ResourceLocation resourcelocation = this.layerTextureLookup.apply(new LayerTextureKey(layerType, equipmentclientinfo$layer));

                nodeCollector.order(j++).submitModel(armorModel, renderState, poseStack, RenderType.armorCutoutNoCull(resourcelocation), packedLight, OverlayTexture.NO_OVERLAY, color, null, renderState.outlineColor, null);

                if (shouldRenderFoil) {
                    nodeCollector.order(j++).submitModel(armorModel, renderState, poseStack, RenderType.armorEntityGlint(), packedLight, OverlayTexture.NO_OVERLAY, color, null, renderState.outlineColor, null);
                }

                shouldRenderFoil = false;
            }
        }
        return j;
    }

    private void renderTrims(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            ClaySoldierModel armorModel,
            AbstractClaySoldierRenderState renderState,
            ArmorTrim armorTrim,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            int color,
            int key
    ) {
        TextureAtlasSprite textureatlassprite = this.trimSpriteLookup.apply(new TrimSpriteKey(armorTrim, layerType, equipmentAsset));
        RenderType renderType = Sheets.armorTrimsSheet(armorTrim.pattern().value().decal());
        nodeCollector.order(key).submitModel(armorModel, renderState, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, color, textureatlassprite, renderState.outlineColor, null);
    }

    public record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }

    public record TrimSpriteKey(ArmorTrim trim, EquipmentClientInfo.LayerType layerType,
                                ResourceKey<EquipmentAsset> equipmentAssetId) {
        public ResourceLocation spriteId() {
            return this.trim.layerAssetId(this.layerType.trimAssetPrefix(), this.equipmentAssetId);
        }
    }
}
