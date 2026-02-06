package net.bumblebee.claysoldiers.datamap.armor.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * This interface represents an accessory of a clay soldier that should be rendered but is not associated with any {@code ArmorSlot}.
 */
public interface RenderableAccessory {
    /**
     * Renders this accessory.
     * @param renderedFrom the RenderLayer this Accessory is rendered from.
     */
    void submit(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier);

}
