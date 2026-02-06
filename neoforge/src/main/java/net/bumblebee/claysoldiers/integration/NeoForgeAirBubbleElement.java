package net.bumblebee.claysoldiers.integration;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
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

        this.width = 8 * iconsPerLine + 1;
        this.height = 5 + 4 * lineCount;
    }


    @Override
    public @Nullable Component getNarration() {
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float maxX) {
        if (iconCount == 0) {
            return;
        }
        int x = this.getX();
        int y = this.getY();

        IDisplayHelper helper = IDisplayHelper.get();
        int xOffset = (iconCount - 1) % iconsPerLine * 8;
        int yOffset = lineCount * 4 - 4;
        for (int i = iconCount; i > 0; --i) {
            if (bursting && i == iconCount) {
                helper.blitSprite(guiGraphics, RenderPipelines.GUI_TEXTURED, AIR_BUBBLE_BURSTING, x + xOffset, y + yOffset, 9, 9);
            } else if (i <= Mth.floor(armor)) {
                helper.blitSprite(guiGraphics, RenderPipelines.GUI_TEXTURED, AIR_BUBBLE, x + xOffset, y + yOffset, 9, 9);
            }

            xOffset -= 8;
            if (xOffset < 0) {
                xOffset = iconsPerLine * 8 - 8;
                yOffset -= 4;
            }
        }
    }
}
