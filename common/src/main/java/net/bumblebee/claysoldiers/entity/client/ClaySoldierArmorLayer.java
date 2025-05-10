package net.bumblebee.claysoldiers.entity.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.ClientSoldierWearableEffect;
import net.bumblebee.claysoldiers.datamap.armor.ClientWearableRenderer;
import net.bumblebee.claysoldiers.datamap.armor.SoldierWearableEffect;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClaySoldierArmorLayer extends SlimeRootLayer<AbstractClaySoldierEntity, ClaySoldierModel> {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final ClaySoldierModel innerModel;
    private final ClaySoldierModel outerModel;
    private final TextureAtlas armorTrimAtlas;

    public ClaySoldierArmorLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> pRenderer, ClaySoldierModel pInnerModel, ClaySoldierModel pOuterModel, ModelManager pModelManager, ItemRenderer itemRenderer) {
        super(pRenderer, itemRenderer);
        this.innerModel = pInnerModel;
        this.outerModel = pOuterModel;
        this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity pLivingEntity,
                       float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, SoldierEquipmentSlot.CHEST, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.CHEST), pPartialTicks);
        this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, SoldierEquipmentSlot.LEGS, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.LEGS), pPartialTicks);
        this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, SoldierEquipmentSlot.FEET, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.FEET), pPartialTicks);
        this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, SoldierEquipmentSlot.HEAD, pPackedLight, this.getArmorModel(SoldierEquipmentSlot.HEAD), pPartialTicks);

        this.renderSlimeRoot(pPoseStack, pBuffer, pLivingEntity, pPackedLight);
    }

    private void renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, AbstractClaySoldierEntity claySoldier, SoldierEquipmentSlot pSlot, int pPackedLight, ClaySoldierModel pModel, float partialTicks) {
        ClientSoldierWearableEffect wearableEffect = (ClientSoldierWearableEffect) getWearableEffect(claySoldier, pSlot);
        if (wearableEffect == null) {
            return;
        }
        ArmorItem armoritem = wearableEffect.copyModel();

        if (armoritem == null) {
            armoritem = wearableEffect.defaultModel(pSlot);
        }
        ItemStack armorCopyStack = wearableEffect.getArmorCopyStack();

        if (SoldierEquipmentSlot.getFromSlot(armoritem.getEquipmentSlot()).orElse(null) == pSlot) {

            this.getParentModel().copyPropertiesTo(pModel);
            this.setPartVisibility(pModel, pSlot);
            Model model = getArmorModelHook(claySoldier, armorCopyStack, pSlot, pModel);
            boolean usesInnerModel = this.usesInnerModel(pSlot);
            if (!wearableEffect.isNoArmorRender()) {
                int color = wearableEffect.getColor(claySoldier, partialTicks);
                ClientWearableRenderer.renderModel(pPoseStack, pBuffer, pPackedLight, model, color, this.getArmorResource(claySoldier, armorCopyStack, pSlot, null));
            }

            if (armorCopyStack.hasFoil()) {
                ClientWearableRenderer.renderGlint(pPoseStack, pBuffer, pPackedLight, model);
            }
            wearableEffect.renderTrims(claySoldier, this.armorTrimAtlas, armoritem.getMaterial(), pPoseStack, pBuffer, pPackedLight, model, usesInnerModel, partialTicks);
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

    private Model getArmorModelHook(AbstractClaySoldierEntity entity, ItemStack itemStack, SoldierEquipmentSlot slot, ClaySoldierModel model) {
        return ClaySoldiersClient.CLIENT_HOOKS.getArmorModel(entity, itemStack, SoldierEquipmentSlot.convertToSlot(slot), model);
    }

    private ResourceLocation getArmorResource(Entity entity, ItemStack stack, SoldierEquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem) stack.getItem();
        String texture = item.getMaterial().getRegisteredName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (usesInnerModel(slot) ? 2 : 1), type == null ? "" : String.format(java.util.Locale.ROOT, "_%s", type));

        ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(s1);

        if (resourcelocation == null) {
            resourcelocation = ResourceLocation.parse(s1);
            ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
        }

        return resourcelocation;
    }

    @Nullable
    private SoldierWearableEffect getWearableEffect(AbstractClaySoldierEntity claySoldier, SoldierEquipmentSlot slot) {
        ItemStackWithEffect stackWithEffect = claySoldier.getItemBySlot(slot);

        if (stackWithEffect == null || stackWithEffect.isEmpty() || claySoldier.isFallingWithGlider(slot)) {
            return null;
        }
        return stackWithEffect.wearableEffectMap().wearableEffect(slot);
    }
}
