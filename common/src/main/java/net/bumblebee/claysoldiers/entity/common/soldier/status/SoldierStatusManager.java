package net.bumblebee.claysoldiers.entity.common.soldier.status;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SoldierStatusManager implements SoldierStatusHolder {
    public static final String FOLLOW_OWNER_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".follow_owner";
    public static final String IGNORING_OWNER_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".ignoring_owner";
    public static final String SITTING_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".sitting";
    public static final String USING_POI_LANG = "clay_mob.status." + ClaySoldiersCommon.MOD_ID + ".using_work_poi";

    private final List<Supplier<SoldierStatusHolder>> statuses;

    public SoldierStatusManager(List<Supplier<SoldierStatusHolder>> statuses) {
        this.statuses = statuses;
    }

    public static SoldierStatusManager initDefault(AbstractClaySoldierEntity soldier) {
        return new SoldierStatusManager(List.of(
                () -> createSittingStatus(soldier),
                () -> createCombatOwnerAndPoiStatus(soldier)
        ));
    }

    public static SoldierStatusManager initProgrammable(ProgrammableClaySoldierEntity soldier) {
        return new SoldierStatusManager(List.of(
                () -> createSittingStatus(soldier),
                () -> (() -> soldier.getInstalledChip().getWorkStatusDisplayName(soldier)),
                () -> createCombatOwnerAndPoiStatus(soldier)
        ));
    }

    @Override
    public String toString() {
        var displayName = getStatusDisplayName();
        return "SoldierStatusManager{(%s) %s}".formatted(statuses.size(), displayName == null ? "Null" : displayName.getString());
    }

    @Override
    public Component getStatusDisplayName() {
        for (Supplier<SoldierStatusHolder> soldierStatusHolderSupplier : statuses) {
            var status = soldierStatusHolderSupplier.get().getStatusDisplayName();
            if (status != null) {
                return status;
            }
        }
        return null;
    }

    private static SoldierStatusHolder createSittingStatus(AbstractClaySoldierEntity soldier) {
        return new SoldierStatusHolder() {
            @Override
            public @Nullable Component getStatusDisplayName() {
                if (soldier.ignoresOwner()) {
                    return Component.translatable(IGNORING_OWNER_LANG);
                }

                return soldier.isInSittingPose() ? Component.translatable(SITTING_LANG) : null;
            }
        };
    }

    private static SoldierStatusHolder createCombatOwnerAndPoiStatus(AbstractClaySoldierEntity soldier) {
        return new SoldierStatusHolder() {
            @Override
            public @Nullable Component getStatusDisplayName() {
                if (soldier.usingPoi()) {
                    return Component.translatable(USING_POI_LANG);
                }

                if (!soldier.fightsBack() || soldier.getClayTeamOwnerUUID() == null) {
                    return null;
                }
                return soldier.getCombatDisplayName();
            }
        };
    }
}
