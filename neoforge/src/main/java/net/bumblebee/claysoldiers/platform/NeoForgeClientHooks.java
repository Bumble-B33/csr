package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.init.ModCreativeTab;
import net.bumblebee.claysoldiers.platform.services.IClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public class NeoForgeClientHooks implements IClientHooks {

    @Override
    public boolean hasSoldierTabOpen() {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen creativeInventory) {
            return creativeInventory.getCurrentPage().getVisibleTabs().stream().anyMatch(tab -> tab == ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB.get() || tab == ModCreativeTab.CLAY_SOLDIERS_TAB.get());
        }
        return false;
    }
}
