package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * This interface represents any Block that a {@code ClayMob} can interact with when its is set as the {@code ClayMobs} Poi.
 */
public interface AssignableWorksiteCapability {
    /**
     * @return whether the given {@code ClayMob} can use this Poi
     */
    boolean canUse(ClayMobEntity clayMob);

    /**
     * The give {@code ClayMob} uses this Poi.
     * @param clayMob to use this capability
     * @return the amount of times the soldier used the poi,
     * @throws IllegalArgumentException when the given {@code ClayMob} cannot use this Poi.
     */
    int onUse(ClayMobEntity clayMob);

    default void useWorksite(ClayMobEntity clayMob) {
        int used = onUse(clayMob);
        if (clayMob.getClayTeamOwner() instanceof ServerPlayer serverPlayer) {
            ModCriterions.USE_ASSIGNED_POI_TRIGGER.get().trigger(serverPlayer, clayMob, this, used);
        }
    }

    /**
     * @return whether this poi should be used one time only or continuously.
     */
    default boolean isOneTimeUse() {
        return true;
    }

    /**
     * @return the ID of this POI.
     */
    ResourceLocation descriptionId();
}
