package net.bumblebee.claysoldiers.soldierproperties.translation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public interface KeyableTranslatableProperty extends ITranslatableProperty {
    @Nullable
    @Override
    default Component getDisplayName() {
        if (getStyle() == null) {
            return null;
        }
        return Component.translatable(translatableKey()).withStyle(getStyle());
    }


    /**
     * Returns the translation key of this property.
     * @return the translation key of this property
     */
    String translatableKey();

    /**
     * Returns the {@code ChatFormatting} of the display name of this property.
     * {@code Null} usually indicates that this property should not be displayed.
     * The {@code ChatFormatting} is normally applied before the {@link #getStyle() styling}.
     * @return the {@code ChatFormatting} of the display name of this property
     */
    @Nullable
    default ChatFormatting getFormat() {
        return ChatFormatting.RESET;
    }

    /**
     * Returns the {@code Style} of the display name of this property.
     * {@code Null} usually indicates that this property should not be displayed.
     * The {@code Style} is normally applied after the {@link #getFormat() formating}.
     * @return the {@code Style} of the display name of this property
     */
    @Nullable
    default Style getStyle() {
        if (getFormat() == null) {
            return null;
        }
        return Style.EMPTY.applyFormat(getFormat());
    }
}
