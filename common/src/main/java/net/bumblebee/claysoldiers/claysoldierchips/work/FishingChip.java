package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.ClaySoldierFishGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class FishingChip extends WorkGoalChip<ClaySoldierFishGoal> {
    private static final Identifier FISHING_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "fishing");
    private static final SearchRange DEFAULT = new SearchRange(16, 2);
    private static final FishingChip NO_ADDON = new FishingChip(List.of());
    public static final AddonInfo ADDON_INFO = new AddonInfo() {
        @Override
        public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
            if (addon == ClaySoldierChipAddons.NO_BREAK_ADDON
            || addon == ClaySoldierChipAddons.RANGE_ADDON
            || addon == ClaySoldierChipAddons.FISH_TREASURE_ADDON) {
                return !presentAddons.contains(addon);
            }
            return addon == ClaySoldierChipAddons.ACCELERATION_ADDON || addon == ClaySoldierChipAddons.UPGRADED_ACCELERATION_ADDON;
        }

        @Override
        public int getAllowedAddonsCount() {
            return 4;
        }
    };

    private FishingChip(List<ClaySoldierChipAddon> addons) {
        super(addons, FISHING_ASSET, 0x104e4e, 0xF9F9F9);
    }

    public static FishingChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new FishingChip(addons);
    }

    public static FishingChip create(ClaySoldierChipAddon... addons) {
        if (addons.length > ADDON_INFO.getAllowedAddonsCount()) {
            throw new IllegalStateException("Too many Addons");
        }
        return create(List.of(addons));
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.FISHING_TYPE.get();
    }

    @Override
    protected ClaySoldierFishGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new ClaySoldierFishGoal(soldier, workAccess, scaleRange(DEFAULT));
    }
}
