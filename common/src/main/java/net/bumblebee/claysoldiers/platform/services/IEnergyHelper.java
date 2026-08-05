package net.bumblebee.claysoldiers.platform.services;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.item.BatteryItem;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;

public interface IEnergyHelper {
    default int getEnergyColor() {
        return ClaySoldiersCommon.CONFIG.getClientConfig().getEnergyColor();
    }

    String getEnergyUnitName();

    int getMaxEnergyStorage(ItemStack stack);

    int getEnergyStoredAsInt(ItemStack stack);

    int insert(ItemStack stack, int amount);

    int extract(ItemStack stack, int wanted);

    TypedDataComponent<? extends Number> getComponent(int energy);

    HamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity, int capacity, int maxInsert, int maxExtract);

    UsingEnergyStorage createEnergyChipStorage(IntConsumer onChange, int capacity, int maxInsert, int maxExtract);

    default BatteryItem createBatteryItem(Item.Properties properties, BatteryProperties batteryProperty) {
        return new BatteryItem(properties, batteryProperty);
    }
}
