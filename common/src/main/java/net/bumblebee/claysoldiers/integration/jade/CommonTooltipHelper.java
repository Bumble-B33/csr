package net.bumblebee.claysoldiers.integration.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface CommonTooltipHelper {
    void add(Component component);
    void append(Component component);
    void addItemStack(ItemStack stack);
    void appendItemStack(ItemStack stack);
    void airBubbles(int breath, boolean bursting);

    void appendMultilineText(Component... lines);
    void addHorizontalLine();

    void addCompoundItemStack(ItemStack large, ItemStack small);
}
