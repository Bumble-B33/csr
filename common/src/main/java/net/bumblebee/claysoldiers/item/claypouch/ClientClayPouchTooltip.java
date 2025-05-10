package net.bumblebee.claysoldiers.item.claypouch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ClientClayPouchTooltip implements ClientTooltipComponent {
    private static final int ITEM_PADDING = 1;
    private static final int ITEM_SIZE = 18;
    private final int count;
    private final ItemStack stack;

    public ClientClayPouchTooltip(ClayPouchContent content) {
        this.count = content.getCount();
        this.stack = content.createStack(Minecraft.getInstance().level.registryAccess());

    }

    @Override
    public int getHeight() {
        return 4 + ITEM_SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return ITEM_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        renderItem(ITEM_PADDING + x, ITEM_PADDING + y, stack, count +"", guiGraphics, font);
    }

    private void renderItem(int pX, int pY, ItemStack stack, String count, GuiGraphics pGuiGraphics, Font pFont) {
        pGuiGraphics.renderItem(stack, pX + 1, pY + 1);
        pGuiGraphics.renderItemDecorations(pFont, stack, pX + 1, pY + 1, count);
    }
}
