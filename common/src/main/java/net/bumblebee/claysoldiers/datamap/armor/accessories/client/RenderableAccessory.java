package net.bumblebee.claysoldiers.datamap.armor.accessories.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * This interface represents an accessory of a clay soldier that should be rendered but is not associated with any {@code ArmorSlot}.
 */
public interface RenderableAccessory<T extends SoldierAccessoryData> {

    /**
     * Renders this accessory.
     * @param renderedFrom the RenderLayer this Accessory is rendered from.
     */
    void submit(IAccessoryRenderLayer renderedFrom, T data, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier);

}
