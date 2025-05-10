package net.bumblebee.claysoldiers.entity.soldier;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModEffects;
import org.jetbrains.annotations.Nullable;

/**
 * This Interface represents any {@code ClayMob} that can be converted to a {@code Vampire}
 */
public interface VampireSubjugate {
    String VAMPIRIC_OWNER_TAG = "VampiricOnwer";
    String SUBJUGATE_TRANSLATION_KEY = ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "vampire.subjugate";

    /**
     * Returns whether this {@code ClayMob} has the {@link ModEffects#VAMPIRE_CONVERSION} effect
     */
    boolean hasVampiricConversionEffect();

    /**
     * Applies the {@link ModEffects#VAMPIRE_CONVERSION} effect
     */
    void applyConversionEffect(ClayMobEntity source);

    @Nullable
    ClayMobEntity getVampOwner();

    void setVampOwner(@Nullable ClayMobEntity pOwner);

    /**
     * Returns whether this is a subjugate of the given vampire.
     */
    default boolean isSubjugateOf(ClayMobEntity vampire) {
        if (hasVampiricConversionEffect()) {
            return false;
        }
        var owner = getVampOwner();
        return owner != null && owner.equals(vampire);
    }

    /**
     * Called when this Subjugate dies under the {@link ModEffects#VAMPIRE_CONVERSION VampiricConversionEffect} .
     * Spawns a new Vampire in its stead.
     */
    void convertToVampire();
}
