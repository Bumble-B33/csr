package net.bumblebee.claysoldiers.soldierproperties.combined;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicate;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicatePriority;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.soldierproperties.*;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;


public class SoldierHoldablePropertiesCombiner implements SoldierPropertyMapReader {
    private static final String TEAM_SLOT_NAME = "team";
    private static final String VEHICLES_SLOT_NAME = "vehicle";
    private static final String BASE_SLOT_NAME = "base";

    private final Map<SoldierEquipmentSlot, PredicatePropertiesPair> slotPropertyMap;
    private final ClaySoldierInventoryQuery inventory;
    private final AttributeMap attributeMap;
    private PredicatePropertiesPair fromTeam = new PredicatePropertiesPair(ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(), SoldierPropertyMap.EMPTY, TEAM_SLOT_NAME);
    private PredicatePropertiesPair fromVehicle = new PredicatePropertiesPair(ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(), SoldierPropertyMap.EMPTY, VEHICLES_SLOT_NAME);
    private PredicatePropertiesPair baseProperties = new PredicatePropertiesPair(ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(), SoldierPropertyMap.EMPTY, BASE_SLOT_NAME);

    private final SoldierPropertyCombinedMap combinedPropertiesMap;

    public SoldierHoldablePropertiesCombiner(ClaySoldierInventoryQuery inventory, AttributeMap attributeMap) {
        this.slotPropertyMap = new EnumMap<>(SoldierEquipmentSlot.class);
        this.combinedPropertiesMap = new SoldierPropertyCombinedMap();
        this.inventory = inventory;
        this.attributeMap = attributeMap;
    }

    /**
     * Add the Properties of a {@code ClayMobTeam} to the combined properties.
     */
    public void addPropertyFromTeam(ClayMobTeam team) {
        if (fromTeam != null) {
            attributeMap.removeAttributeModifiers(fromTeam.attributesAsMultiMapHolder());
        }
        fromTeam = new PredicatePropertiesPair(null, team.getProperties(), TEAM_SLOT_NAME);
    }

    public void addVehicle(@NotNull SoldierVehicleProperties soldierProperties) {
        attributeMap.removeAttributeModifiers(fromVehicle.attributesAsMultiMapHolder());

        fromVehicle = new PredicatePropertiesPair(soldierProperties.predicate(), soldierProperties.properties(), VEHICLES_SLOT_NAME);
    }

    public void addBaseProperties(@NotNull SoldierPropertyMapReader properties) {
        attributeMap.removeAttributeModifiers(baseProperties.attributesAsMultiMapHolder());

        baseProperties = new PredicatePropertiesPair(ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(), properties, BASE_SLOT_NAME);
    }

    /**
     * Adds the given {@code Properties}  to the combined properties.
     *
     * @param predicate whether the {@code Properties} are active
     * @param property  the {@code Properties} to add
     * @param slot      the {@code Slot} of the {@code Properties}
     */
    public void addProperty(ClayPredicate<?> predicate, SoldierPropertyMap property, SoldierEquipmentSlot slot) {
        var pairPrev = slotPropertyMap.put(slot, new PredicatePropertiesPair(predicate, property, slot.getSerializedName()));
        if (pairPrev != null) {
            attributeMap.removeAttributeModifiers(pairPrev.attributesAsMultiMapHolder());
        }
    }

    /**
     * Removes all {@code Properties} associated with the give {@code Slot}.
     */
    public void removeProperty(SoldierEquipmentSlot slot) {
        var pair = slotPropertyMap.remove(slot);
        if (pair != null) {
            attributeMap.removeAttributeModifiers(pair.attributesAsMultiMapHolder());
        }
    }

    /**
     * Combines all {@code Properties} and checks whether the {@code Predicate} evaluates to {@code true}
     */
    public void combine() {
        reset();
        testAndCombine();
    }

    private void reset() {
        combinedPropertiesMap.clear();

        for (PredicatePropertiesPair pair : slotPropertyMap.values()) {
            attributeMap.removeAttributeModifiers(pair.attributesAsMultiMapHolder());
        }
    }

    private void testAndCombine() {
        List<PredicatePropertiesPair> highPriorityCompleted = getAddedProperties().filter(l -> l.getPriority() == ClayPredicatePriority.HIGH).filter(l -> l.predicate.test(inventory)).toList();
        addTestedProperties(highPriorityCompleted);
        addTestedProperties(List.of(fromTeam, fromVehicle, baseProperties));

        List<PredicatePropertiesPair> restAll = getAddedProperties().filter(p -> p.getPriority() != ClayPredicatePriority.HIGH).toList();
        List<PredicatePropertiesPair> newlyTrue = new ArrayList<>();
        do {
            addTestedProperties(newlyTrue);
            newlyTrue.clear();
            newlyTrue.addAll(restAll.stream().filter(p -> p.predicate.test(inventory)).toList());
            restAll = restAll.stream().filter(p -> !(p.predicate.test(inventory))).toList();
        } while (!newlyTrue.isEmpty());
    }

    private Stream<PredicatePropertiesPair> getAddedProperties() {
        return slotPropertyMap.values().stream();
    }

    private void addTestedProperties(List<PredicatePropertiesPair> allReadyCompleted) {
        for (PredicatePropertiesPair pair : allReadyCompleted) {
            combinedPropertiesMap.combineMap(pair.properties());

            attributeMap.addTransientAttributeModifiers(pair.attributesAsMultiMapHolder());
        }
    }

    @Override
    public <T> SoldierProperty<T> getProperty(SoldierPropertyType<T> type) {
        return combinedPropertiesMap.getProperty(type);
    }

    @Override
    public <T> @Nullable T getValueOrNull(SoldierPropertyType<T> type) {
        return combinedPropertiesMap.getValueOrNull(type);
    }

    @Override
    public <T> boolean hasPropertyType(SoldierPropertyType<T> type) {
        return combinedPropertiesMap.hasPropertyType(type);
    }

    @Override
    public @NotNull Iterator<SoldierProperty<?>> iterator() {
        return combinedPropertiesMap.iterator();
    }

    private record PredicatePropertiesPair(ClayPredicate<?> predicate, SoldierPropertyMapReader properties, String slotName) {
        public ClayPredicatePriority getPriority() {
            return predicate.getPriority();
        }

        public Multimap<Holder<Attribute>, AttributeModifier> attributesAsMultiMapHolder() {
            Map<Holder<Attribute>, List<AttributeModifier>> baseMap = properties.getValueOrDfault(SoldierPropertyTypes.ATTRIBUTES.get());
            Multimap<Holder<Attribute>, AttributeModifier> multiMap = ArrayListMultimap.create(baseMap.size(), 1);
            baseMap.forEach((h, m) -> m.forEach(mod -> {
                multiMap.put(h, new AttributeModifier(mod.id().withSuffix("_" + slotName), mod.amount(), mod.operation()));
            }));

            return multiMap;
        }
    }

    @Override
    public String toString() {
        return combinedPropertiesMap.toString();
    }

    @Override
    public boolean isEmpty() {
        return combinedPropertiesMap.isEmpty();
    }
}
