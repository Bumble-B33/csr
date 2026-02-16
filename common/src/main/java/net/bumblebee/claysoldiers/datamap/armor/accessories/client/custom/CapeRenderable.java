package net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.CapeAccessoryData;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class CapeRenderable implements RenderableAccessory<CapeAccessoryData> {
    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, CapeAccessoryData data, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier) {
        if (!claySoldier.isInvisible &&  data.textureLocation() != null) {
            if (!this.hasLayer(claySoldier.chestEquipment, EquipmentClientInfo.LayerType.WINGS, renderedFrom)) {
                pPoseStack.pushPose();
                if (this.hasLayer(claySoldier.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID, renderedFrom)) {
                    pPoseStack.translate(0.0F, -0.053125F, 0.06875F);
                }

                AbstractClaySoldierRenderState renderState = claySoldier.renderStateFrom;

                nodeCollector.submitModel(
                        renderedFrom.getCapeModel(),
                        renderState,
                        pPoseStack,
                        RenderType.entitySolid(data.textureLocation()),
                        pPackedLight,
                        OverlayTexture.NO_OVERLAY,
                        getCapeColor(data, claySoldier),
                        null, renderState.outlineColor, null
                );
                pPoseStack.popPose();
            }

        }
    }

    private boolean hasLayer(ItemStack stack, EquipmentClientInfo.LayerType layer, IAccessoryRenderLayer renderer) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            EquipmentClientInfo equipmentclientinfo = renderer.getEquipmentAssets().get(equippable.assetId().get());
            return !equipmentclientinfo.getLayers(layer).isEmpty();
        } else {
            return false;
        }
    }

    public int getCapeColor(CapeAccessoryData data, AccessoryRenderState soldier) {
        if (data.affectedByOffsetColor()) {
            return soldier.offsetColor;
        }
        return data.color().getColor(soldier.id, soldier.ageInTicks);
    }
}
