package net.bumblebee.claysoldiers.entity.soldier.status;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SoldierStatusManager implements SoldierStatusHolder {
    public static final String SITTING_LANG = "clay_mob_status." + ClaySoldiersCommon.MOD_ID + ".sitting";
    public static final String USING_POI_LANG = "clay_mob_status." + ClaySoldiersCommon.MOD_ID + ".using_poi";

    private final List<Supplier<SoldierStatusHolder>> statuses;

    public SoldierStatusManager(List<Supplier<SoldierStatusHolder>> statuses) {
        this.statuses = statuses;
    }

    public static SoldierStatusManager initDefault(AbstractClaySoldierEntity soldier, Supplier<SoldierStatusHolder> workStatus) {
        return new SoldierStatusManager(List.of(
                () -> createSittingStatus(soldier),
                workStatus,
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

                if (!soldier.getAttackType().fightsBack() || soldier.getClayTeamOwnerUUID() == null) {
                    return null;
                }
                return soldier.getCombatDisplayName();
            }
        };
    }
}
