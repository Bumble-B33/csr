package net.bumblebee.claysoldiers.integration.accessories;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.bumblebee.claysoldiers.init.ModItems;

public class ModAccessoryRenderers {
    public static void init() {
        AccessoriesRendererRegistry.registerArmorRendering(ModItems.CLAY_GOGGLES.get());
    }
}
