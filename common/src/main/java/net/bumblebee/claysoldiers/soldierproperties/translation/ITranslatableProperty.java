package net.bumblebee.claysoldiers.soldierproperties.translation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface ITranslatableProperty {
    /**
     * Returns the display name of this property.
     * May be {@code null} to indicate this property should not be displayed.
     * @return the display name of this property
     */
    @Nullable
    Component getDisplayName();

    /**
     * @param livingEntity either the player viewing the tooltip or the owner of the tooltip
     */
    default @Nullable Component getAnimatedDisplayName(LivingEntity livingEntity) {
        return getDisplayName();
    }

    static ITranslatableProperty keyValuePair(Component keyDisplayName, Component valueDisplayName) {
        return () -> {
            if (keyDisplayName == null || valueDisplayName == null) {
                return null;
            }
            return keyDisplayName.copy().append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY)).append(valueDisplayName);
        };
    }
    static ITranslatableProperty keyValuePair(ITranslatableProperty key, ITranslatableProperty value) {
        return keyValuePair(key.getDisplayName(), value.getDisplayName());
    }
}
