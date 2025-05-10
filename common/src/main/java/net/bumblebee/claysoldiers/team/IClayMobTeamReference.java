package net.bumblebee.claysoldiers.team;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IClayMobTeamReference {

    /**
     * @return the value of this Reference.
     */
    @NotNull ClayMobTeam value();

    /**
     * @return the key of this Reference.
     */
    @NotNull ResourceLocation key();

    /**
     * Returns whether the given key is for this registry.
     *
     * @param location the key.
     * @return whether the given key is for this registry.
     */
    default boolean isValidForKey(ResourceLocation location) {
        return key().equals(location);
    }
}
