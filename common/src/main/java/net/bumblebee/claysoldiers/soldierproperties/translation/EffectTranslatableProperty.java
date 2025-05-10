package net.bumblebee.claysoldiers.soldierproperties.translation;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;


public interface EffectTranslatableProperty extends ITranslatableProperty {
    Component getEffectDisplayName();
    int getEffectColor();

    @Override
    default @Nullable Component getDisplayName() {
        return getEffectDisplayName().copy().withColor(getEffectColor());
    }

    static EffectTranslatableProperty create(MobEffect effect) {
        return new EffectTranslatableProperty() {
            @Override
            public Component getEffectDisplayName() {
                return effect.getDisplayName();
            }

            @Override
            public int getEffectColor() {
                return effect.getColor();
            }
        };
    }
    static EffectTranslatableProperty create(Holder<MobEffect> effect) {
        return create(effect.value());
    }
}
