package net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.translation.EffectTranslatableProperty;
import net.bumblebee.claysoldiers.soldierproperties.translation.ITranslatableProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.ToIntFunction;

public class EffectImmunityMap implements Iterable<ITranslatableProperty> {
    public static final Codec<EffectImmunityMap> CODEC = Codec.unboundedMap(BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), EffectImmunityType.CODEC).xmap(EffectImmunityMap::fromMap, EffectImmunityMap::fromImmunityMap);
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Holder<MobEffect>, EffectImmunityType>> MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, MobEffect.STREAM_CODEC, EffectImmunityType.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectImmunityMap> STREAM_CODEC = MAP_STREAM_CODEC.map(EffectImmunityMap::new, EffectImmunityMap::fromImmunityMap);
    public static final EffectImmunityMap EMPTY = new EffectImmunityMap();
    public static final ToIntFunction<EffectImmunityMap> TO_INT = EffectImmunityMap::size;
    public static final ValueCombiner<EffectImmunityMap> COMBINER = EffectImmunityMap::combine;

    private final Map<Holder<MobEffect>, EffectImmunityType> immuneEffects;

    public EffectImmunityMap() {
        this.immuneEffects = new HashMap<>();
    }
    public EffectImmunityMap(Map<Holder<MobEffect>, EffectImmunityType> effectMap) {
        this.immuneEffects = new HashMap<>(effectMap);
    }

    private static EffectImmunityMap combine(EffectImmunityMap map1, EffectImmunityMap map2) {
        if (map1.isEmpty()) {
            return map2;
        }
        if (map2.isEmpty()) {
            return map1;
        }

        var combined = new EffectImmunityMap(map1.immuneEffects);
        combined.putAll(map2.immuneEffects);
        return combined;
    }
    private static EffectImmunityMap fromMap(Map<Holder<MobEffect>, EffectImmunityType> immuneEffects) {
        return new EffectImmunityMap(immuneEffects);
    }
    private static Map<Holder<MobEffect>, EffectImmunityType> fromImmunityMap(EffectImmunityMap map) {
        return map.immuneEffects;
    }
    public void put(Holder<MobEffect> effect, EffectImmunityType newType) {
        immuneEffects.compute(effect, (key, value) -> {
            if (value == null) {
                return newType;
            }
            return value == newType ? value : null;
        });
    }
    private void putAll(Map<Holder<MobEffect>, EffectImmunityType> effects) {
        effects.forEach(this::put);
    }

    public boolean isImmune(Holder<MobEffect> mobEffect) {
        return immuneEffects.get(mobEffect) == EffectImmunityType.IMMUNE;
    }
    public boolean isPersistent(Holder<MobEffect> mobEffect) {
        return immuneEffects.get(mobEffect) == EffectImmunityType.PERSISTENT;
    }


    protected int size() {
        return immuneEffects.size();
    }
    protected boolean isEmpty() {
        return immuneEffects.isEmpty();
    }
    @Override
    public Iterator<ITranslatableProperty> iterator() {
        return immuneEffects.entrySet().stream().map(EffectImmunityMap::create).iterator();
    }

    @Override
    public String toString() {
        return "EffectImmunityMap" + immuneEffects;
    }

    private static ITranslatableProperty create(Map.Entry<Holder<MobEffect>, EffectImmunityType> mapEntry) {
        return ITranslatableProperty.keyValuePair(
                EffectTranslatableProperty.create(mapEntry.getKey()),
                mapEntry.getValue()
        );
    }
}
