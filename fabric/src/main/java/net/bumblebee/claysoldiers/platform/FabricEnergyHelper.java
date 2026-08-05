package net.bumblebee.claysoldiers.platform;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBatteryProperty;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.capability.FabricChipAssemblerEnergyStorage;
import net.bumblebee.claysoldiers.capability.FabricHamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.item.BatteryItem;
import net.bumblebee.claysoldiers.item.FabricBatteryItem;
import net.bumblebee.claysoldiers.platform.services.IEnergyHelper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.function.IntConsumer;
import java.util.function.ToLongFunction;

public class FabricEnergyHelper implements IEnergyHelper {
    @Override
    public String getEnergyUnitName() {
        return " E";
    }

    @Override
    public int getEnergyStoredAsInt(ItemStack stack) {
        return ifEnergyIsPresentView(stack, EnergyStorage::getAmount);
    }

    @Override
    public int getMaxEnergyStorage(ItemStack stack) {
        return ifEnergyIsPresentView(stack, EnergyStorage::getCapacity);
    }

    @Override
    public int insert(ItemStack stack, int amount) {
        return ifEnergyIsPresent(stack, c -> {
            Transaction tx = Transaction.openOuter();
            long inserted = c.insert(amount, tx);
            tx.abort();

            SimpleEnergyItem.setStoredEnergyUnchecked(stack, c.getAmount() + inserted);

            return inserted;
        });
    }

    @Override
    public int extract(ItemStack stack, int wanted) {
        return ifEnergyIsPresent(stack, c -> {
            Transaction tx = Transaction.openOuter();
            long extracted = c.extract(wanted, tx);
            tx.abort();

            SimpleEnergyItem.setStoredEnergyUnchecked(stack, c.getAmount() - extracted);

            return extracted;
        });
    }

    @Override
    public TypedDataComponent<Long> getComponent(int energy) {
        return new TypedDataComponent<>(EnergyStorage.ENERGY_COMPONENT, (long) energy);
    }

    @Override
    public HamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity, int capacity, int maxInsert, int maxExtract) {
        return new FabricHamsterWheelEnergyStorage(hamsterWheelBlockEntity, capacity, maxInsert, maxExtract);
    }

    @Override
    public UsingEnergyStorage createEnergyChipStorage(IntConsumer onChange, int capacity, int maxInsert, int maxExtract) {
        return new FabricChipAssemblerEnergyStorage(onChange, capacity, maxInsert, maxExtract);
    }

    @Override
    public BatteryItem createBatteryItem(Item.Properties properties, BatteryProperties batteryProperty) {
        return new FabricBatteryItem(properties, batteryProperty);
    }

    private int ifEnergyIsPresentView(ItemStack stack, ToLongFunction<EnergyStorage> action) {
        var cap = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (cap == null) {
            return 0;
        }

        return Ints.saturatedCast(action.applyAsLong(cap));
    }

    private int ifEnergyIsPresent(ItemStack stack, ToLongFunction<EnergyStorage> action) {
        if (!(stack.getItem() instanceof SimpleEnergyItem)) {
            return 0;
        }
        var cap = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (cap == null) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Simple Energy Item with out Energy Storage");
            return 0;
        }

        return Ints.saturatedCast(action.applyAsLong(cap));
    }
}
