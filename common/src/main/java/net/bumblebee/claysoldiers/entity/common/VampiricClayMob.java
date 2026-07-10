package net.bumblebee.claysoldiers.entity.common;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;

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
        return getLevel()
                .registryAccess()
                .get(getNightTimeLine())
                .map(timeline -> timeline.value().getCurrentTicks(getLevel().clockManager()))
                .map(this::isNightForVampire)
                .orElse(false);
    }

   default ResourceKey<Timeline> getNightTimeLine() {
        return Timelines.OVERWORLD_DAY;
   }


    Level getLevel();

    static float getPowerForMoonPhase(MoonPhase moonPhase) {
        return switch (moonPhase) {
            case FULL_MOON -> 2.5f;
            case WANING_GIBBOUS, WAXING_GIBBOUS -> 2.25F;
            case THIRD_QUARTER, FIRST_QUARTER -> 2F;
            case WANING_CRESCENT, WAXING_CRESCENT -> 1.75f;
            case NEW_MOON -> 1.5f;
        };
    }
}
