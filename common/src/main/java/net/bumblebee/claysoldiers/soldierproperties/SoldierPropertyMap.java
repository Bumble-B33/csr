package net.bumblebee.claysoldiers.soldierproperties;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.*;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.types.UnitPropertyType;
import net.bumblebee.claysoldiers.util.codec.SoldierPropertyMapCodec;
import net.bumblebee.claysoldiers.util.codec.SoldierPropertyMapStreamCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SoldierPropertyMap implements SoldierPropertyMapReader {
    public static final Set<Supplier<? extends SoldierPropertyType<?>>> IGNORED_NON_ITEM = new HashSet<>(Set.of(SoldierPropertyTypes.ATTACK_TYPE, SoldierPropertyTypes.EVACUATION, SoldierPropertyTypes.THROWABLE));

    public static final Codec<SoldierPropertyMap> CODEC = new SoldierPropertyMapCodec();
    public static final Codec<SoldierPropertyMap> CODEC_FOR_NON_ITEM = new SoldierPropertyMapCodec(IGNORED_NON_ITEM);

    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierPropertyMap> STREAM_CODEC = new SoldierPropertyMapStreamCodec();

    public static final SoldierPropertyMap EMPTY_MAP = new SoldierPropertyMap(null, s -> Set.of());

    private final Set<SoldierProperty<?>> set;

    private <T> SoldierPropertyMap(T collection, Function<T, Set<SoldierProperty<?>>> factory) {
        this.set = factory.apply(collection);
    }

    public SoldierPropertyMap() {
        this.set = new HashSet<>();
    }

    public SoldierPropertyMap(Collection<? extends SoldierProperty<?>> collection) {
        this(collection, HashSet::new);
    }

    public static SoldierPropertyMap of(SoldierProperty<?>... properties) {
        return new SoldierPropertyMap(properties, Set::of);
    }

    /**
     * Adds a new {@code SoldierProperty} to this map. Removes any existing {@code SoldierProperty} of the same type.
     * @param identifier the property to add
     * @param value the value of the property
     * @return whether the {@code SoldierProperty} could be added.
     * @throws UnsupportedOperationException if the operation is not supported by underlying set
     */
    public <T> boolean addPropertyForce(SoldierPropertyType<T> identifier, T value) {
        return addPropertyForce(new SoldierProperty<>(identifier, value));
    }

    /**
     * Adds a new {@code SoldierProperty} to this map. Removes any existing {@code SoldierProperty} of the same type.
     * @param property the property to add
     * @return whether the {@code SoldierProperty} could be added.
     * @throws UnsupportedOperationException if the operation is not supported by underlying set
     */
    public boolean addPropertyForce(SoldierProperty<?> property) {
        set.remove(property);
        return set.add(property);
    }

    /**
     * Appends the given key value pair to the given MapProperty. If the property does not exist a new Map is created.
     * @param type the property type
     * @param key the key
     * @param value the value
     * @throws UnsupportedOperationException
     * <br>if the {@code map#put} operation is not supported by the map
     * <br>if the {@code set#add} operation is not supported by underlying set
     */
    protected <K, V> void appendMapProperty(SoldierPropertyType<Map<K, V>> type, K key, V value) {
        Map<K, V> map = getValueOrNull(type);
        if (map == null)  {
            map = new HashMap<>();
            addPropertyForce(type, map);
        }
        map.put(key, value);
    }

    /**
     * Appends the given value to the given ListProperty. If the property does not exist a new List is created.
     * @param type the property type
     * @param value the value
     * @throws UnsupportedOperationException
     * <br> if the {@code list#add} operation is not supported by the list
     * <br> if the {@code set#add} operation is not supported by underlying set
     */
    protected <T> void appendListProperty(SoldierPropertyType<List<T>> type, T value) {
        List<T> list;
        if (hasPropertyType(type)) {
            list = getValueOrDfault(type);
        } else {
            list = new ArrayList<>();
            addPropertyForce(type, list);
        }
        list.add(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> SoldierProperty<T> getProperty(SoldierPropertyType<T> type) {
        for (SoldierProperty<?> property : set) {
            if (property.type().equals(type)) {
                return (SoldierProperty<T>) property;
            }
        }
        return null;
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValueOrNull(SoldierPropertyType<T> type) {
        for (SoldierProperty<?> property : set) {
            if (property.type().equals(type)) {
                return (T) property.value();
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Iterator<SoldierProperty<?>> iterator() {
        return set.iterator();
    }

    @Override
    public Spliterator<SoldierProperty<?>> spliterator() {
        return set.spliterator();
    }

    @Override
    public String toString() {
        return "SoldierPropertyMap" + set;
    }

    /**
     * Validates this SoldierPropertyMap by thrown an Exception.
     * @throws IllegalStateException stating what was wrong
     */
    public void validate(Consumer<IllegalStateException> exceptionHandler) {
        switch (getValueOrDfault(SoldierPropertyTypes.THROWABLE.get())) {
            case HARM -> {
                if (attackType().isSupportive()) {
                    exceptionHandler.accept(new IllegalStateException("Cannot have a harmful ranged attack with an supportive attack type"));
                }
            }
            case HELPING -> {
                if (!attackType().isSupportive()) {
                    exceptionHandler.accept(new IllegalStateException("Cannot have a supportive ranged attack with an non supportive attack type"));
                }
            }
        }

    }

    public void clear() {
        set.clear();
    }

    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SoldierPropertyMap map;

        public Builder() {
            this.map = new SoldierPropertyMap();
        }

        public Builder setDamage(float damage) {
            return addProperty(SoldierPropertyTypes.DAMAGE, damage);
        }
        public Builder setProtection(float protection) {
            return addProperty(SoldierPropertyTypes.PROTECTION, protection);
        }
        public Builder setExplosionResistance(float protection) {
            return addProperty(SoldierPropertyTypes.EXPLOSION_RESISTANCE, protection);
        }
        public Builder setSetOnFire(int setOnFire) {
            return addProperty(SoldierPropertyTypes.SET_ON_FIRE, setOnFire);
        }
        public Builder throwable(RangedAttackType type) {
            return addProperty(SoldierPropertyTypes.THROWABLE, type);
        }
        public Builder throwable(RangedAttackType type, float damage) {
            return this.throwable(type).setDamage(damage);
        }
        public Builder setSeeInvis() {
            return addUnitProperty(SoldierPropertyTypes.SEE_INVISIBILITY);
        }
        public Builder setCanSwim() {
            return addUnitProperty(SoldierPropertyTypes.CAN_SWIM);

        }
        public Builder setBreathHold(int holdBreath) {
            return addProperty(SoldierPropertyTypes.BREATH_HOLD, holdBreath);

        }
        public Builder infiniteBreathHold() {
            return this.setBreathHold(BreathHoldPropertyType.MAX_BREATH_HOLD);
        }
        public Builder noBreathHold() {
            return this.setBreathHold(BreathHoldPropertyType.NO_BREATH_HOLD);
        }
        public Builder explosion(float power) {
            return addProperty(SoldierPropertyTypes.DEATH_EXPLOSION, power);
        }
        public Builder size(float size) {
            return addProperty(SoldierPropertyTypes.SIZE, size);
        }
        public Builder glowOutline() {
            return addUnitProperty(SoldierPropertyTypes.GLOW_OUTLINE);
        }
        public Builder glowing() {
            return addUnitProperty(SoldierPropertyTypes.GLOW_IN_THE_DARK);
        }
        public Builder invisible() {
            return addUnitProperty(SoldierPropertyTypes.INVISIBLE);
        }
        public Builder addAttribute(Holder<Attribute> attribute, AttributeModifier modifier) {
            this.map.appendMapProperty(SoldierPropertyTypes.ATTRIBUTES.get(), attribute, List.of(modifier));
            return this;
        }
        public Builder addDeathCloudEffect(DeathCloudProperty deathCloudEffect) {
            this.map.appendListProperty(SoldierPropertyTypes.DEATH_CLOUD.get(), deathCloudEffect);
            return this;
        }
        public Builder attackType(AttackTypeProperty type) {
            return addProperty(SoldierPropertyTypes.ATTACK_TYPE, type);
        }
        public Builder heavy(float toughness) {
            return addProperty(SoldierPropertyTypes.HEAVY, toughness);
        }
        public Builder addSpecialAttack(SpecialAttack<?> type) {
            this.map.appendListProperty(SoldierPropertyTypes.SPECIAL_ATTACK.get(), type);
            return this;
        }
        public Builder addCounterAttack(SpecialAttack<?> type) {
            this.map.appendListProperty(SoldierPropertyTypes.COUNTER_ATTACK.get(), type);
            return this;
        }
        public Builder damageBlock(float amount, float chance, boolean pierceable) {
            return addProperty(SoldierPropertyTypes.DAMAGE_BLOCK, new DamageBlock(amount, chance, pierceable));
        }
        public Builder damageBlock(float chance, float amount) {
            return damageBlock(amount, chance, true);
        }
        public Builder canReviveOther(ReviveProperty reviveProperty) {
            return addProperty(SoldierPropertyTypes.REVIVE_PROPERTY, reviveProperty);
        }
        public Builder immunity(Holder<MobEffect> effect, EffectImmunityType type) {
            EffectImmunityMap effectMap;
            if (map.hasPropertyType(SoldierPropertyTypes.IMMUNITY.get())) {
                effectMap = map.getValueOrDfault(SoldierPropertyTypes.IMMUNITY.get());
            } else {
                effectMap = new EffectImmunityMap();
                map.addPropertyForce(SoldierPropertyTypes.IMMUNITY.get(), effectMap);
            }
            effectMap.put(effect, type);
            return this;
        }
        public Builder wraith(WraithProperty wraith) {
            return addProperty(SoldierPropertyTypes.WRAITH, wraith);
        }
        public Builder bonusAttackRange(float bonusRange) {
            return addProperty(SoldierPropertyTypes.ATTACK_RANGE, bonusRange);
        }
        public Builder allowGliding() {
            return addUnitProperty(SoldierPropertyTypes.CAN_GLIDE);
        }
        public Builder allowTeleporting() {
            return addUnitProperty(SoldierPropertyTypes.TELEPORTATION);
        }
        public Builder setTeleportingToOwner() {
            return addUnitProperty(SoldierPropertyTypes.TELEPORT_TO_OWNER);
        }
        public Builder setEvacuation(IEvacuationProperty evacuation) {
            return addProperty(SoldierPropertyTypes.EVACUATION, evacuation);
        }
        public Builder canBounce() {
            return addUnitProperty(SoldierPropertyTypes.BOUNCE);
        }

        public Builder addUnitProperty(Supplier<UnitPropertyType> unit) {
            return addProperty(unit, UnitProperty.INSTANCE);
        }

        public <V, T extends SoldierPropertyType<V>> Builder addProperty(Supplier<T> type, V value) {
            this.map.addPropertyForce(type.get(), value);
            return this;
        }

        public SoldierPropertyMap build() {
            map.validate(this::throwException);
            return map;
        }

        private void throwException(IllegalStateException exception) {
            throw exception;
        }
    }
}
