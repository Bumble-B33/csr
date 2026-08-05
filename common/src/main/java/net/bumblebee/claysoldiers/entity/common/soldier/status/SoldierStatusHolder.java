package net.bumblebee.claysoldiers.entity.common.soldier.status;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface SoldierStatusHolder {
    String FOLLOW_OWNER_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".follow_owner";
    String IGNORING_OWNER_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".ignoring_owner";
    String SITTING_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".sitting";
    String USING_POI_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".using_work_poi";


    /**
     * Returns the display name of the Status of this StatusHolder.
     * May be {@code null} to indicate this StatusHolder has currently no Status.
     */
    @Nullable
    Component getStatusDisplayName();

    static @Nullable Component clayMobWorkStatus(ClayMobEntity clayMob) {

        if (clayMob.isInSittingPose()) {
            return Component.translatable(SITTING_LANG);
        }
        if (clayMob.ignoresOwner()) {
            return Component.translatable(IGNORING_OWNER_LANG);
        }
        return clayMob.usingPoi() ? Component.literal(USING_POI_LANG) : null;

    }

    static SoldierStatusHolder initDefault(AbstractClaySoldierEntity soldier) {
        return () -> {
            if (soldier.isInSittingPose()) {
                return Component.translatable(SITTING_LANG);
            }
            if (soldier.ignoresOwner()) {
                return Component.translatable(IGNORING_OWNER_LANG);
            }
            if (soldier.usingPoi()) {
                return Component.translatable(USING_POI_LANG);
            }
            if (!soldier.fightsBack() || soldier.getClayTeamOwnerUUID() == null) {
                return null;
            }
            return soldier.getCombatDisplayName();
        };
    }

    static SoldierStatusHolder initProgrammable(ProgrammableClaySoldierEntity soldier) {
        return () -> {
            if (soldier.isInSittingPose()) {
                return Component.translatable(FOLLOW_OWNER_LANG);
            }
            return soldier.getInstalledChip().getWorkStatusDisplayName(soldier);
        };
    }
}
