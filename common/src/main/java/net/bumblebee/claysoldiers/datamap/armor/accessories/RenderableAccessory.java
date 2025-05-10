package net.bumblebee.claysoldiers.datamap.armor.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * This interface represents an accessory of a clay soldier that should be rendered but is not associated with any {@code ArmorSlot}.
 */
public interface RenderableAccessory {
    /**
     * Renders this accessory.
     * @param renderedFrom the RenderLayer this Accessory is rendered from.
     */
    void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling);
}
