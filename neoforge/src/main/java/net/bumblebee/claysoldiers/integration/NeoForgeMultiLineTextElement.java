package net.bumblebee.claysoldiers.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class NeoForgeMultiLineTextElement extends Element {
    private final FormattedText[] lines;
    private final String text;
    private final Font font;

    public NeoForgeMultiLineTextElement(Component[] line) {
        this.lines = line;
        if (line.length == 0) {
            throw new IllegalArgumentException("Cannot create a MultiLineElement with 0 lines");
        }
        this.font = Minecraft.getInstance().font;
        int maxWidth = 0;
        StringBuilder builder = new StringBuilder();
        for (FormattedText text : lines) {
            maxWidth = Math.max(maxWidth, font.width(text));
            builder.append(text.getString());
            builder.append(" ");
        }
        this.width = maxWidth;
        this.height = (font.lineHeight + 1) * lines.length - 1;
        this.text = builder.toString();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mousey, float partialTick) {
        for (int i = 0; i < lines.length; i++) {
            DisplayHelper.INSTANCE.drawText(guiGraphics, lines[i], this.getX(), this.getY() + ((1 + font.lineHeight) * i), IThemeHelper.get().getNormalColor());
        }
    }

    @Override
    public @Nullable Component getNarration() {
        return Component.literal(text);
    }
}
