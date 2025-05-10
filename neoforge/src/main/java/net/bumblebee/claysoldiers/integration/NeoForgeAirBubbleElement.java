package net.bumblebee.claysoldiers.integration;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

public class NeoForgeAirBubbleElement extends Element {
    public static final ResourceLocation AIR_BUBBLE = ResourceLocation.withDefaultNamespace("hud/air");
    public static final ResourceLocation AIR_BUBBLE_BURSTING = ResourceLocation.withDefaultNamespace("hud/air_bursting");

    private final int armor;
    private final boolean bursting;
    private final int iconsPerLine;
    private final int lineCount = 1;
    private final int iconCount;

    public NeoForgeAirBubbleElement(int breath, boolean bursting) {
        this.bursting = bursting;
        if (breath > 10 || breath < 0) {
            throw new IllegalArgumentException("Breath needs to be in range [0;10]");
        }
        this.armor = breath;
        iconCount = breath;
        iconsPerLine = Math.min(10, iconCount);
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(8 * iconsPerLine + 1, 5 + 4 * lineCount);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        if (iconCount == 0) {
            return;
        }

        IDisplayHelper helper = IDisplayHelper.get();
        int xOffset = (iconCount - 1) % iconsPerLine * 8;
        int yOffset = lineCount * 4 - 4;
        for (int i = iconCount; i > 0; --i) {
            if (bursting && i == iconCount) {
                helper.blitSprite(guiGraphics, AIR_BUBBLE_BURSTING, (int) (x + xOffset), (int) (y + yOffset), 9, 9);
            } else if (i <= Mth.floor(armor)) {
                helper.blitSprite(guiGraphics, AIR_BUBBLE, (int) (x + xOffset), (int) (y + yOffset), 9, 9);
            }

            xOffset -= 8;
            if (xOffset < 0) {
                xOffset = iconsPerLine * 8 - 8;
                yOffset -= 4;
            }
        }
    }
}
