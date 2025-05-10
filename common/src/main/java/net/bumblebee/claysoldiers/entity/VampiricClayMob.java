package net.bumblebee.claysoldiers.entity;

import net.minecraft.world.level.LevelAccessor;

/**
 * This Interface represents any {@code ClayMob} that is more powerful during the night.
 */
public interface VampiricClayMob {
    /**
     * @return the power of this {@code VampiricClayMob}.
     */
    default float getPowerMultiplier() {
        if (isNightForVampire()) {
            return getNightPower();
        }
        return 0.5f;
    }

    /**
     * @return the power of this {@code VampiricClayMob} during the night.
     */
    float getNightPower();

    private boolean isNightForVampire(long dayTime) {
        int time = (int) (dayTime % 24000);
        return 12542 < time && time < 23460;
    }

    default boolean isNightForVampire() {
        return isNightForVampire(level().dayTime());
    }

    LevelAccessor level();
}
