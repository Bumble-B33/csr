package net.bumblebee.claysoldiers.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public abstract class BrickedItem extends Item {
    public BrickedItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Returns the un-bricked variant of this {@code Item}.
     * @param bricked the {@code BrickedItem} to get original form
     * @return the original {@code Item}
     */
    public abstract ItemStack getOriginal(ItemStack bricked);

    /**
     * @return the display name of the original {@code Item}.
     */
    public abstract Component getOriginalDisplayName(ItemStack bricked, HolderLookup.Provider registries);

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(CommonComponents.space().append(getOriginalDisplayName(stack, context.registries())).withStyle(ChatFormatting.DARK_GRAY));
    }
}
