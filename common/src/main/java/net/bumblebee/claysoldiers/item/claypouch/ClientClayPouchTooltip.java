package net.bumblebee.claysoldiers.item.claypouch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public int getHeight(Font font) {
        return 4 + ITEM_SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return ITEM_SIZE;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        extractItem(ITEM_PADDING + x, ITEM_PADDING + y, stack, count +"", guiGraphics, font);
    }


    private void extractItem(int pX, int pY, ItemStack stack, String count, GuiGraphicsExtractor pGuiGraphics, Font pFont) {
        pGuiGraphics.item(stack, pX + 1, pY + 1);
        pGuiGraphics.itemDecorations(pFont, stack, pX + 1, pY + 1, count);
    }
}
