package net.bumblebee.claysoldiers.item;

import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class FabricBatteryItem extends BatteryItem implements SimpleEnergyItem {

    public FabricBatteryItem(Properties properties, BatteryProperties batteryProperty) {
        super(properties, batteryProperty);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return batteryProperty.capacity();
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return batteryProperty.maxInsert();
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return batteryProperty.maxExtract();
    }
}
