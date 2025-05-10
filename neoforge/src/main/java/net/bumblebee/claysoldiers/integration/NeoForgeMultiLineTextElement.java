package net.bumblebee.claysoldiers.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class NeoForgeMultiLineTextElement extends Element {
    private final FormattedText[] lines;
    private final int maxWidth;
    private final String text;

    public NeoForgeMultiLineTextElement(Component[] line) {
        this.lines = line;
        if (line.length == 0) {
            throw new IllegalArgumentException("Cannot create a MultiLineElement with 0 lines");
        }
        Font font = Minecraft.getInstance().font;
        int maxWidth = 0;
        StringBuilder builder = new StringBuilder();
        for (FormattedText text : lines) {
            maxWidth = Math.max(maxWidth, font.width(text));
            builder.append(text.getString());
            builder.append(" ");
        }
        this.maxWidth = maxWidth;
        this.text = builder.toString();
    }


    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(maxWidth, (font.lineHeight + 1) * lines.length - 1);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < lines.length; i++) {
            DisplayHelper.INSTANCE.drawText(guiGraphics, lines[i], x, y + ((1 + font.lineHeight) * i), IThemeHelper.get().getNormalColor());

        }
    }

    @Override
    public String getMessage() {
        return text;
    }
}
