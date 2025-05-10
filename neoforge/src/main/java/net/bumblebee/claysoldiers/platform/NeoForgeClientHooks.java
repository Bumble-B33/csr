package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.init.ModCreativeTab;
import net.bumblebee.claysoldiers.platform.services.IClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;

public class NeoForgeClientHooks implements IClientHooks {
    @Override
    public Model getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> _default) {
        return ClientHooks.getArmorModel(entityLiving, itemStack, slot, _default);
    }

    @Override
    public boolean isNameplateInRenderDistance(Entity entity, double distanceToSqr) {
        return ClientHooks.isNameplateInRenderDistance(entity, distanceToSqr);
    }

    @Override
    public boolean hasSoldierTabOpen() {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen creativeInventory) {
            return creativeInventory.getCurrentPage().getVisibleTabs().stream().anyMatch(tab -> tab == ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB.get() || tab == ModCreativeTab.CLAY_SOLDIERS_TAB.get());
        }
        return false;
    }
}
