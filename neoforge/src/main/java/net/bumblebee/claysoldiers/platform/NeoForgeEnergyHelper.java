package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.block.chipassembler.UsingEnergyStorage;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.cap.NeoForgeHamsterWheelEnergy;
import net.bumblebee.claysoldiers.cap.NeoForgeChipAssemblerEnergy;
import net.bumblebee.claysoldiers.platform.services.IEnergyHelper;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

public class NeoForgeEnergyHelper implements IEnergyHelper {
    @Override
    public String getEnergyUnitName() {
        return "FE";
    }

    @Override
    public int getEnergyStoredAsInt(ItemStack stack) {
        return ifEnergyIsPresent(stack, EnergyHandler::getAmountAsInt);
    }

    @Override
    public int getMaxEnergyStorage(ItemStack stack) {
       return ifEnergyIsPresent(stack, EnergyHandler::getCapacityAsInt);
    }

    @Override
    public int insert(ItemStack stack, int amount) {
        return ifEnergyIsPresent(stack, c -> {
            Transaction tx = Transaction.openRoot();
            int inserted = c.insert(amount, tx);
            tx.commit();

            return inserted;
        });
    }

    @Override
    public int extract(ItemStack stack, int wanted) {
        return ifEnergyIsPresent(stack, c -> {
            Transaction tx = Transaction.openRoot();
            int extract = c.extract(wanted, tx);
            tx.commit();

            return extract;
        });
    }

    @Override
    public TypedDataComponent<Integer> getComponent(int energy) {
        return new TypedDataComponent<>(ClaySoldiersNeoForge.BATTERY_ENERGY.get(), energy);
    }

    @Override
    public HamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity, int capacity, int maxInsert, int maxExtract) {
        return new NeoForgeHamsterWheelEnergy(hamsterWheelBlockEntity, capacity, maxInsert, maxExtract);
    }

    @Override
    public UsingEnergyStorage createEnergyChipStorage(IntConsumer onChange, int capacity, int maxInsert, int maxExtract) {
        return new NeoForgeChipAssemblerEnergy(onChange, capacity, maxInsert, maxExtract);
    }

    private int ifEnergyIsPresent(ItemStack stack, ToIntFunction<EnergyHandler> action) {
        EnergyHandler cap = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
        if (cap == null) {
            return 0;
        }
        return action.applyAsInt(cap);
    }

}
