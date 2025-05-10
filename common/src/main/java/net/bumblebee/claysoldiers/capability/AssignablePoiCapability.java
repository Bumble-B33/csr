package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;

/**
 * This interface represents any Block that a {@code ClayMob} can interact with when its is set as the {@code ClayMobs} Poi.
 */
public interface AssignablePoiCapability {
    /**
     * @return whether the given {@code ClayMob} can use this Poi
     */
    boolean canUse(ClayMobEntity clayMob);

    /**
     * The give {@code ClayMob} uses this Poi.
     * @param clayMob to use this capability
     * @throws IllegalArgumentException when the given {@code ClayMob} cannot use this Poi.
     */
    void use(ClayMobEntity clayMob);

    /**
     * @return whether this poi should be used one time only or continuously.
     */
    default boolean isOneTimeUse() {
        return true;
    }
}
