package net.bumblebee.claysoldiers.soldierproperties.combined;

import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;

/**
 * A specialized map for combining soldier properties. It extends the SoldierPropertyMap class.
 */
public class SoldierPropertyCombinedMap extends SoldierPropertyMap {
    /**
     * Constructs a new SoldierPropertyCombinedMap.
     */
    public SoldierPropertyCombinedMap() {
    }

    /**
     * Combines the given property with the existing property of the same type in this map,
     * if present; otherwise, adds the property to the map.
     *
     * @param type  the type of the property
     * @param value the value of the property
     * @param <T>   the type of the property value
     */
    public <T> void combineProperty(SoldierPropertyType<T> type, T value) {
        combineProperty(new SoldierProperty<>(type, value));
    }

    /**
     * Combines the given property with the existing property of the same type in this map,
     * if present; otherwise, adds the property to the map.
     *
     * @param property the property to combine
     * @param <T>      the type of the property value
     */
    public <T> void combineProperty(SoldierProperty<T> property) {
        var result = getProperty(property.type());
        if (result != null) {
            addPropertyForce(result.createWithCombinedValues(property.value()));
        } else {
            addPropertyForce(property);
        }
    }
    /**
     * Adds all properties from the given map to this map, combining properties of the same type
     * if they are already present in this map.
     *
     * @param map the property map to combine
     */
    public void combineMap(SoldierPropertyMapReader map) {
        for (SoldierProperty<?> property : map) {
            combineProperty(property);
        }
    }
}
