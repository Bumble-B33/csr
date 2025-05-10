package net.bumblebee.claysoldiers.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Function;

/**
 * An interface representing a class that holds an {@link MobEffect} that is serializable to {@link Codec}.
 */
public interface EffectHolder {
    Codec<EffectHolder> CODEC = RecordCodecBuilder.create(in -> CodecUtils.addEffectAnd().apply(in, EffectHolder::create));

    /**
     * Returns the effect.
     */
    default MobEffect effect() {
        return effectHolder().value();
    };

    /**
     * The duration of the effect in ticks.
     * @return duration of the effect
     */
    int duration();

    /**
     * The amplifier of the effect. Starting with 0.
     * @return amplifier of the effect
     */
    int amplifier();

    /**
     * Returns the {@code Holder} instance of the effect.
     * @return {@code Holder} instance of the effect
     */
    Holder<MobEffect> effectHolder();


    default String effectHolderToString() {
        return "Effect: " + effect().getDisplayName().getString() + " Amplifier: " + amplifier() + " Duration: " + duration();
    }

    /**
     * Returns a {@code Codec} for the given type {@link T}.
     * @param creates a function creating {@link T} from an {@code EffectHolder}
     * @param <T> the Child class of the {@code EffectHolder}
     */
    static <T extends EffectHolder> Codec<T> getCodec(Function<EffectHolder, T> creates) {
        return CODEC.xmap(creates, e -> e);
    }

    static <T extends EffectHolder> Codec<T> getCodec(Function<EffectHolder, T> creates, String prefix) {
        Codec<EffectHolder> codec = RecordCodecBuilder.create(in -> CodecUtils.addEffectAnd(prefix).apply(in, EffectHolder::create));
        return codec.xmap(creates, e -> e);
    }
    /**
     * Creates a new {@code EffectHolder} with the given values.
     */
    static EffectHolder create(Holder<MobEffect> effect, int duration, int amplifier) {
        return new EffectHolder() {
            @Override
            public Holder<MobEffect> effectHolder() {
                return effect;
            }

            @Override
            public MobEffect effect() {
                return effect.value();
            }

            @Override
            public int duration() {
                return duration;
            }

            @Override
            public int amplifier() {
                return amplifier;
            }
        };
    }
}