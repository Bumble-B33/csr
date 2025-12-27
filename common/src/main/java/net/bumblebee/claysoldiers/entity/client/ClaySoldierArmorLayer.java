package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.ClientSoldierWearableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierWearableEffect;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ClaySoldierArmorLayer extends SlimeRootLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final Map<SoldierEquipmentSlot, Equippable> FALLBACK_EQUIPPABLE = new EnumMap<>(SoldierEquipmentSlot.class);
    static {
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.HEAD, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.CHEST, Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.LEGS, Equippable.builder(EquipmentSlot.LEGS).setAsset(EquipmentAssets.IRON).build());
        FALLBACK_EQUIPPABLE.put(SoldierEquipmentSlot.FEET, Equippable.builder(EquipmentSlot.FEET).setAsset(EquipmentAssets.IRON).build());

    }
    private final ClaySoldierModel innerModel;
    private final ClaySoldierModel outerModel;
    private final TextureAtlas armorTrimAtlas;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<LayerTextureKey, ResourceLocation> layerTextureLookup;
    private final Function<TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

    public ClaySoldierArmorLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer, ClaySoldierModel pInnerModel, ClaySoldierModel pOuterModel, ModelManager pModelManager, EquipmentAssetManager equipmentAssets) {
        super(pRenderer);
        this.innerModel = pInnerModel;
        this.outerModel = pOuterModel;
        this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
        this.layerTextureLookup = Util.memoize(p_386235_ -> p_386235_.layer.getTextureLocation(p_386235_.layerType));
        this.trimSpriteLookup = Util.memoize(p_386234_ -> armorTrimAtlas.getSprite(p_386234_.textureId()));
        this.equipmentAssets = equipmentAssets;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierRenderState claySoldierRenderState, float v, float v1) {
        this.renderArmorPiece(poseStack, pBuffer, claySoldierRenderState, SoldierEquipmentSlot.CHEST, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.CHEST));
        this.renderArmorPiece(poseStack, pBuffer, claySoldierRenderState, SoldierEquipmentSlot.LEGS, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.LEGS));
        this.renderArmorPiece(poseStack, pBuffer, claySoldierRenderState, SoldierEquipmentSlot.FEET, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.FEET));
        this.renderArmorPiece(poseStack, pBuffer, claySoldierRenderState, SoldierEquipmentSlot.HEAD, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.HEAD));

        this.renderSlimeRoot(poseStack, pBuffer, claySoldierRenderState, pPackedLight);
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, AbstractClaySoldierRenderState claySoldier, SoldierEquipmentSlot slot, int packedLight, ClaySoldierModel model) {
        ClientSoldierWearableEffect wearableEffect = (ClientSoldierWearableEffect) getWearableEffect(claySoldier, slot);
        if (wearableEffect == null) {
            return;
        }
        Equippable equippable = wearableEffect.getEquippable();
        if (equippable == null) {
            equippable = FALLBACK_EQUIPPABLE.get(slot);
        }

        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(slot)
                ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                : EquipmentClientInfo.LayerType.HUMANOID;

        this.getParentModel().copyPropertiesTo(model);
        this.setPartVisibility(model, slot);

        if (wearableEffect.shouldRenderArmor()) {
            int color = claySoldier.offsetColor;
            if (!wearableEffect.isAffectedByOffsetColor() || color == -1) {
                color = wearableEffect.getColorHelper().getColor(claySoldier.id, claySoldier.ageInTicks);
            }

            renderArmorLayers(layerType, equippable.assetId().orElseThrow(), model, poseStack, bufferSource, packedLight, color, false);
        }
        for (ClientSoldierWearableEffect.TrimHolder trimHolder : wearableEffect.getArmorTrims()) {
            renderTrims(layerType, equippable.assetId().orElseThrow(), model, trimHolder.trim(), poseStack, bufferSource, packedLight, trimHolder.color().getColor(claySoldier.id, claySoldier.ageInTicks));
        }
    }

    private void setPartVisibility(ClaySoldierModel pModel, SoldierEquipmentSlot pSlot) {
        pModel.setAllVisible(false);
        switch (pSlot) {
            case HEAD:
                pModel.head.visible = true;
                pModel.hat.visible = true;
                break;
            case CHEST:
                pModel.body.visible = true;
                pModel.rightArm.visible = true;
                pModel.leftArm.visible = true;
                break;
            case LEGS:
                pModel.body.visible = true;
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
                break;
            case FEET:
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
        }
    }

    private ClaySoldierModel getArmorModel(SoldierEquipmentSlot pSlot) {
        return this.usesInnerModel(pSlot) ? this.innerModel : this.outerModel;
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

    private void renderArmorLayers(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model armorModel,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int color,
            boolean foil
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(equipmentAsset).getLayers(layerType);
        if (!list.isEmpty()) {

            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                ResourceLocation resourcelocation = this.layerTextureLookup.apply(new LayerTextureKey(layerType, equipmentclientinfo$layer));
                VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(resourcelocation), foil);
                armorModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
            }
        }
    }

    private void renderTrims(
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model armorModel,
            ArmorTrim armorTrim,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int color
    ) {
        TextureAtlasSprite textureatlassprite = this.trimSpriteLookup.apply(new TrimSpriteKey(armorTrim, layerType, equipmentAsset));
        VertexConsumer vertexConsumer = textureatlassprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
        armorModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    public record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }

    public record TrimSpriteKey(ArmorTrim trim, EquipmentClientInfo.LayerType layerType,
                                ResourceKey<EquipmentAsset> equipmentAssetId) {
        private static String getColorPaletteSuffix(Holder<TrimMaterial> trimMaterial, ResourceKey<EquipmentAsset> equipmentAsset) {
            String s = trimMaterial.value().overrideArmorAssets().get(equipmentAsset);
            return s != null ? s : trimMaterial.value().assetName();
        }

        public ResourceLocation textureId() {
            ResourceLocation resourcelocation = this.trim.pattern().value().assetId();
            String s = getColorPaletteSuffix(this.trim.material(), this.equipmentAssetId);
            return resourcelocation.withPath(p_387008_ -> "trims/entity/" + this.layerType.getSerializedName() + "/" + p_387008_ + "_" + s);
        }
    }
}
