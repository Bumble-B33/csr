package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.init.ModCreativeTab;
import net.bumblebee.claysoldiers.platform.services.IClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public class FabricClientHooks implements IClientHooks {

    @Override
    public boolean hasSoldierTabOpen() {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen creativeInventory) {
            return creativeInventory.getSelectedItemGroup() == ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB.get();
        }
        return false;
    }
}
