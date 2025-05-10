package net.bumblebee.claysoldiers.entity.boss;

import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.combined.SoldierPropertyCombinedMap;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.DamageBlock;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.UnitProperty;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface RandomSoldierPropertyGenerator<T> {
    RandomSoldierPropertyGenerator<Float> SIZE_GENERATOR = createFloatRange(SoldierPropertyTypes.SIZE.get(), 0.5f, 10f);
    RandomSoldierPropertyGenerator<Float> OPTIONAL_PROTECTION_GENERATOR = createOptionalFloatRange(SoldierPropertyTypes.PROTECTION.get(), 0.0f, 20f);
    RandomSoldierPropertyGenerator<Float> OPTIONAL_DAMAGE_GENERATOR = createOptionalFloatRange(SoldierPropertyTypes.DAMAGE.get(), 0.0f, 5f);
    RandomSoldierPropertyGenerator<Float> OPTIONAL_HEAVY_GENERATOR = createOptionalFloatRange(SoldierPropertyTypes.HEAVY.get(), 0.0f, 5f);
    RandomSoldierPropertyGenerator<Float> OPTIONAL_ATTACK_RANGE_GENERATOR = createOptionalFloatRange(SoldierPropertyTypes.ATTACK_RANGE.get(), 0.0f, 0.5f);
    RandomSoldierPropertyGenerator<UnitProperty> BOUNCE_GENERATOR = createUnit(SoldierPropertyTypes.BOUNCE.get());
    RandomSoldierPropertyGenerator<UnitProperty> GLOW_GENERATOR = createUnit(SoldierPropertyTypes.GLOW_IN_THE_DARK.get());
    RandomSoldierPropertyGenerator<UnitProperty> GLOW_OUTLINE_GENERATOR = createUnit(SoldierPropertyTypes.GLOW_OUTLINE.get());
    RandomSoldierPropertyGenerator<UnitProperty> CAN_SWIM = createConstant(SoldierPropertyTypes.BOUNCE.get());
    RandomSoldierPropertyGenerator<UnitProperty> SEE_INVISIBILITY = createConstant(SoldierPropertyTypes.BOUNCE.get());
    RandomSoldierPropertyGenerator<Integer> OPTIONAL_BREATH_HOLD = createOptionalIntRange(SoldierPropertyTypes.BREATH_HOLD.get(), 0, BreathHoldPropertyType.MAX_BREATH_HOLD);
    RandomSoldierPropertyGenerator<Float> OPTIONAL_EXPLOSION_RESISTANCE = createOptionalFloatRange(SoldierPropertyTypes.EXPLOSION_RESISTANCE.get(), 0, 20f);
    RandomSoldierPropertyGenerator<DamageBlock> OPTIONAL_DAMAGE_BLOCK = random -> {
        if (random.nextBoolean()) {
            return null;
        }
        return new SoldierProperty<>(SoldierPropertyTypes.DAMAGE_BLOCK.get(), new DamageBlock(floatRange(random, 0.1f, 1f), random.nextIntBetweenInclusive(1, 5)));
    };
    RandomSoldierPropertyGenerator<Integer> OPTIONAL_SET_ON_FIRE = createOptionalIntRange(SoldierPropertyTypes.SET_ON_FIRE.get(), 0, 2);

    Set<RandomSoldierPropertyGenerator<?>> BOSS = Set.of(CAN_SWIM, SEE_INVISIBILITY, SIZE_GENERATOR, OPTIONAL_PROTECTION_GENERATOR, OPTIONAL_SET_ON_FIRE, OPTIONAL_DAMAGE_GENERATOR, OPTIONAL_DAMAGE_BLOCK, OPTIONAL_HEAVY_GENERATOR, OPTIONAL_ATTACK_RANGE_GENERATOR);
    Set<RandomSoldierPropertyGenerator<?>> BOSS_WEIGHTLESS = Set.of(BOUNCE_GENERATOR, GLOW_GENERATOR, GLOW_OUTLINE_GENERATOR, OPTIONAL_BREATH_HOLD, OPTIONAL_EXPLOSION_RESISTANCE);

    static SoldierPropertyMap generateRandom(RandomSource random, int maxWeight, Collection<RandomSoldierPropertyGenerator<?>> generators) {
        if (generators.isEmpty()) {
            return SoldierPropertyMap.EMPTY_MAP;
        }
        SoldierPropertyCombinedMap map = new SoldierPropertyCombinedMap();

        for (var gen : generators) {
            SoldierProperty<?> res = gen.generate(random);
            if (res != null) {
                maxWeight--;
                map.combineProperty(res);
                if (maxWeight <= 0) {
                    return map;
                }
            }
        }
        return map;
    }

    private static RandomSoldierPropertyGenerator<Float> createFloatRange(SoldierPropertyType<Float> type, float min, float max) {
        return random -> new SoldierProperty<>(type,  floatRange(random, min, max));
    }

    private static RandomSoldierPropertyGenerator<Float> createOptionalFloatRange(SoldierPropertyType<Float> type, float min, float max) {
        return random -> random.nextBoolean() ? createFloatRange(type, min, max).generate(random) : null;
    }

    private static RandomSoldierPropertyGenerator<UnitProperty> createUnit(SoldierPropertyType<UnitProperty> type) {
        return random -> random.nextBoolean() ? SoldierProperty.unit(type) : null;
    }

    private static RandomSoldierPropertyGenerator<UnitProperty> createConstant(SoldierPropertyType<UnitProperty> type) {
        return random -> SoldierProperty.unit(type);
    }

    private static RandomSoldierPropertyGenerator<Integer> createOptionalIntRange(SoldierPropertyType<Integer> type, int min, int max) {
        return random -> new SoldierProperty<>(type, random.nextIntBetweenInclusive(min, max));
    }

    private static float floatRange(RandomSource random, float min, float max) {
        return min + ((random.nextFloat() * max) - min);
    }

    @Nullable SoldierProperty<T> generate(RandomSource random);
}
