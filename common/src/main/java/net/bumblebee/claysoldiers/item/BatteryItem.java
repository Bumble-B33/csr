package net.bumblebee.claysoldiers.item;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBatteryProperty;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class BatteryItem extends Item {
    protected final BatteryProperties batteryProperty;

    public BatteryItem(Properties properties, BatteryProperties batteryProperty) {
        super(properties);
        this.batteryProperty = batteryProperty;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ClaySoldiersCommon.ENERGY_HELPER.getEnergyColor();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.clamp(((getEnergyStored(stack) * (float) Item.MAX_BAR_WIDTH) / getMaxEnergyStorage(stack)), 0, Item.MAX_BAR_WIDTH);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        int max = getMaxEnergyStorage(stack);
        if (max <= 0) {
            return;
        }
        builder.accept(
                Component.literal("%s/%s%s".formatted(getEnergyStored(stack), max, ClaySoldiersCommon.ENERGY_HELPER.getEnergyUnitName()))
                        .withStyle(ChatFormatting.GRAY)
                );
    }

    public static float getFillPercent(ItemStack stack) {
        return (float) getEnergyStored(stack) / getMaxEnergyStorage(stack);
    }

    private static int getMaxEnergyStorage(ItemStack stack) {
        return ClaySoldiersCommon.ENERGY_HELPER.getMaxEnergyStorage(stack);
    }

    private static int getEnergyStored(ItemStack stack) {
        return ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(stack);
    }

    public static @Nullable BatteryProperties getMaxBatteryEnergyStorage(ItemStack stack) {
        if (stack.getItem() instanceof BatteryItem batteryItem) {
            return batteryItem.batteryProperty;
        }
        ClaySoldiersCommon.ERROR_HANDLER.warn(stack + " is not a BatteryItem");

        return null;
    }
}
