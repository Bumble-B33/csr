package net.bumblebee.claysoldiers.soldierproperties;

import net.bumblebee.claysoldiers.soldierproperties.customproperties.*;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public interface SoldierPropertyMapReader extends Iterable<SoldierProperty<?>> {
    SoldierPropertyMapReader EMPTY = new SoldierPropertyMapReader() {
        @Override
        public @Nullable <T> SoldierProperty<T> getProperty(SoldierPropertyType<T> type) {
            return null;
        }

        @Override
        public <T> @Nullable T getValueOrNull(SoldierPropertyType<T> type) {
            return null;
        }

        @Override
        public @NotNull Iterator<SoldierProperty<?>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    /**
     * Returns the property with the given type. May be null if no such property exists
     */
    @Nullable
    <T> SoldierProperty<T> getProperty(SoldierPropertyType<T> type);

    /**
     * Returns the value of the given type. May be null if no such property exists
     */
    @Nullable
    <T> T getValueOrNull(SoldierPropertyType<T> type);

    /**
     * Returns the value of the given type.
     * If no such property exists the default value is returned
     */
    default <T> T getValueOrDfault(SoldierPropertyType<T> type) {
        var value = getValueOrNull(type);
        return value != null ? value : type.getDefaultValue();
    }
    /**
     * Returns the value of the given type.
     * If no such property exists the default value is returned
     */
    default <T> T getValueOrDfault(Supplier<SoldierPropertyType<T>> type) {
        return getValueOrDfault(type.get());
    }
    /**
     * Returns whether this map has the given property.
     * @param type the type of the property
     */
    default <T> boolean hasPropertyType(SoldierPropertyType<T> type) {
        return getValueOrNull(type) != null;
    }

    /**
     * Read
     * {@link SoldierPropertyType#applyAsInt(Object)}
     */
    default <T> int getPropertyValueAsInt(SoldierPropertyType<T> type) {
        return type.applyAsInt(getValueOrDfault(type));
    }

    default float damage() {
        return getValueOrDfault(SoldierPropertyTypes.DAMAGE);
    }
    default float protection() {
        return getValueOrDfault(SoldierPropertyTypes.PROTECTION);
    }
    default float explosionResistance() {
        return getValueOrDfault(SoldierPropertyTypes.EXPLOSION_RESISTANCE);
    }
    default int setOnFire() {
        return getValueOrDfault(SoldierPropertyTypes.SET_ON_FIRE);
    }
    default RangedAttackType throwable() {
        return getValueOrDfault(SoldierPropertyTypes.THROWABLE);
    }
    default boolean canSwim() {
        return hasPropertyType(SoldierPropertyTypes.CAN_SWIM.get());
    }
    default boolean canSeeInvis() {
        return hasPropertyType(SoldierPropertyTypes.SEE_INVISIBILITY.get());
    }
    default int breathHoldDuration() {
        return getValueOrDfault(SoldierPropertyTypes.BREATH_HOLD.get());
    }
    default float getSoldierSize() {
        return getValueOrDfault(SoldierPropertyTypes.SIZE);
    }
    default boolean isInvisible() {
        return hasPropertyType(SoldierPropertyTypes.INVISIBLE.get());
    }
    default boolean hasGlowOutline() {
        return hasPropertyType(SoldierPropertyTypes.GLOW_OUTLINE.get());
    }
    default boolean isGlowing() {
        return hasPropertyType(SoldierPropertyTypes.GLOW_IN_THE_DARK.get());
    }
    default AttackTypeProperty attackType() {
        return getValueOrDfault(SoldierPropertyTypes.ATTACK_TYPE);
    }

    default float heavy() {
        return getValueOrDfault(SoldierPropertyTypes.HEAVY);
    }
    @UnmodifiableView
    default List<SpecialAttack<?>> specialAttacks(SpecialAttackType type, SpecialEffectCategory category) {
        return getValueOrDfault(SoldierPropertyTypes.SPECIAL_ATTACK).stream().filter(s -> s.getAttackType().is(type)).filter(s -> s.isForCategory(category)).toList();
    }
    @UnmodifiableView
    default List<SpecialAttack<?>> counterAttacks(SpecialAttackType type) {
        return getValueOrDfault(SoldierPropertyTypes.COUNTER_ATTACK).stream().filter(s -> s.getAttackType().is(type)).filter(s -> s.isForCategory(SpecialEffectCategory.HARMFUL)).toList();
    }

    @UnmodifiableView
    default List<MobEffectInstance> getDeathCloudEffects() {
        return getValueOrDfault(SoldierPropertyTypes.DEATH_CLOUD).stream().map(DeathCloudProperty::asInstance).toList();
    }
    default DamageBlock damageBlock() {
        return getValueOrDfault(SoldierPropertyTypes.DAMAGE_BLOCK);
    }
    default ReviveProperty reviveType() {
        return getValueOrDfault(SoldierPropertyTypes.REVIVE_PROPERTY);
    }
    default EffectImmunityMap immunity() {
        return getValueOrDfault(SoldierPropertyTypes.IMMUNITY);
    }
    @Nullable
    default WraithProperty wraith() {
        return getValueOrNull(SoldierPropertyTypes.WRAITH.get());
    }
    default float bonusAttackRange() {
        return getValueOrDfault(SoldierPropertyTypes.ATTACK_RANGE);
    }
    default boolean canGlide() {
        return hasPropertyType(SoldierPropertyTypes.CAN_GLIDE.get());
    }
    default boolean canTeleport() {
        return hasPropertyType(SoldierPropertyTypes.TELEPORTATION.get());
    }
    default boolean canTeleportToOwner() {
        return hasPropertyType(SoldierPropertyTypes.TELEPORT_TO_OWNER.get());
    }
    default IEvacuationProperty getEvacuationProperty() {
        return getValueOrDfault(SoldierPropertyTypes.EVACUATION.get());
    }
    default boolean hasEvacuationProperty() {
        var value = getValueOrNull(SoldierPropertyTypes.EVACUATION.get());
        return value != null && !value.isEmpty();
    }
    default boolean canBounce() {
        return hasPropertyType(SoldierPropertyTypes.BOUNCE.get());
    }


    @NotNull
    @Override
    Iterator<SoldierProperty<?>> iterator();

    boolean isEmpty();
}
