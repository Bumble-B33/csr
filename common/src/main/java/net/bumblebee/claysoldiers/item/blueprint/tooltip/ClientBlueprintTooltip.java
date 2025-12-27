package net.bumblebee.claysoldiers.item.blueprint.tooltip;

import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientBlueprintTooltip implements ClientTooltipComponent {
    private static final int MAX_ITEMS_PER_ROW = 10;
    private static final int ITEM_SIZE = 18;
    private static final int ITEM_PADDING = 1;
    private final List<ItemStack> contents;

    public ClientBlueprintTooltip(ResourceLocation dataKey) {
        List<ItemStack> contentCopy;
        BlueprintData data = Minecraft.getInstance().player.registryAccess().lookupOrThrow(ModRegistries.BLUEPRINTS).getValue(dataKey);
        if (data == null) {
            contentCopy = List.of();
        } else {
            try {
                contentCopy = data.getTemplate().getNeededItems();
            } catch (IllegalStateException e) {
                contentCopy = List.of();
            }
        }
        this.contents = contentCopy;
    }

    @Override
    public int getHeight(Font font) {
        return 4 + ITEM_SIZE * divideCeil(contents.size());
    }

    @Override
    public int getWidth(Font font) {
        return ITEM_SIZE * Math.min(contents.size(), MAX_ITEMS_PER_ROW);
    }

    @Override
    public void renderImage(Font font, int pX, int pY, int width, int height, GuiGraphics guiGraphics) {
        for (int itemIndex = 0; itemIndex < contents.size(); itemIndex++) {
            int elementX = ITEM_PADDING + pX + ((itemIndex % MAX_ITEMS_PER_ROW) * ITEM_SIZE);
            int elementY = ITEM_PADDING + pY + ((itemIndex / MAX_ITEMS_PER_ROW) * ITEM_SIZE);
            renderItem(elementX, elementY, itemIndex, guiGraphics, font);
        }
    }

    private void renderItem(int pX, int pY, int pItemIndex, GuiGraphics pGuiGraphics, Font pFont) {
        ItemStack itemstack = this.contents.get(pItemIndex);
        pGuiGraphics.renderItem(itemstack, pX + 1, pY + 1, pItemIndex);
        pGuiGraphics.renderItemDecorations(pFont, itemstack, pX + 1, pY + 1);
    }

    private static int divideCeil(int numerator) {
        return (int) Math.ceil((float) numerator / ClientBlueprintTooltip.MAX_ITEMS_PER_ROW);
    }

}
