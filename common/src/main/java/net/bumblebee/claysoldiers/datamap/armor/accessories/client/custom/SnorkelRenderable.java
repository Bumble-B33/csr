package net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.TextureAccessoryData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SnorkelRenderable implements RenderableAccessory<TextureAccessoryData> {


    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, TextureAccessoryData data, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier) {
        nodeCollector.submitModel(
                renderedFrom.getSnorkelModel(),
                claySoldier.renderStateFrom,
                pPoseStack,
                RenderType.entitySolid(data.textureLocation()),
                pPackedLight,
                OverlayTexture.NO_OVERLAY,
                0,
                null
                );
    }
}
