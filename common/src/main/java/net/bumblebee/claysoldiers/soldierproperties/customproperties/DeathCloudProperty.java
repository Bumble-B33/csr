package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.translation.EffectTranslatableProperty;
import net.bumblebee.claysoldiers.util.EffectHolder;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public record DeathCloudProperty(Holder<MobEffect> effectHolder, int amplifier, int duration) implements EffectHolder, EffectTranslatableProperty {
    public static final Codec<DeathCloudProperty> CODEC = EffectHolder.getCodec(DeathCloudProperty::fromEffectHolder);
    public static final Codec<List<DeathCloudProperty>> LIST_CODEC = CodecUtils.getSingleOrListCodec(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<DeathCloudProperty>> LIST_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), DeathCloudProperty::effectHolder,
            ByteBufCodecs.VAR_INT, DeathCloudProperty::amplifier,
            ByteBufCodecs.VAR_INT, DeathCloudProperty::duration,
            DeathCloudProperty::new
    ).apply(ByteBufCodecs.list());
    public static final List<DeathCloudProperty> EMPTY = List.of();
    public static final ToIntFunction<List<DeathCloudProperty>> TO_INT = List::size;
    public static final ValueCombiner<List<DeathCloudProperty>> COMBINER = (d1, d2) -> {
        List<DeathCloudProperty> list = new ArrayList<>(d1);
        list.addAll(d2);
        return list;
    };

    public MobEffectInstance asInstance() {
        return new MobEffectInstance(effectHolder, duration, amplifier, true, true);
    }
    private static DeathCloudProperty fromEffectHolder(EffectHolder effectHolder) {
        return new DeathCloudProperty(effectHolder.effectHolder(), effectHolder.amplifier(), effectHolder.duration());
    }

    @Override
    public String toString() {
        return effectHolderToString();
    }

    @Override
    public Component getEffectDisplayName() {
        return effectHolder.value().getDisplayName();
    }

    @Override
    public int getEffectColor() {
        return effectHolder.value().getColor();
    }
}
