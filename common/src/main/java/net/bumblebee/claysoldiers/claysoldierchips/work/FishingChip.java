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
import java.util.Set;

public class FishingChip extends WorkGoalChip<SearchRange, ClaySoldierFishGoal> {
    private static final Identifier FISHING_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "fishing");
    public static final AddonInfo ADDON_INFO = new AddonInfo() {
        @Override
        public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
            if (addon == ClaySoldierChipAddons.NO_BREAK_ADDON
            || addon == ClaySoldierChipAddons.RANGE_ADDON
            || addon == ClaySoldierChipAddons.FISH_TREASURE_ADDON) {
                return !presentAddons.contains(addon);
            }
            return addon == ClaySoldierChipAddons.ACCELERATION_ADDON;
        }

        @Override
        public int getAllowedAddonsCount() {
            return 4;
        }
    };

    public FishingChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, FISHING_ASSET, 0x104e4e, 0xF9F9F9);
    }

    public static FishingChip create(SearchRange searchRange) {
        return new FishingChip(searchRange, List.of());
    }

    public static FishingChip create(ClaySoldierChipAddon... addons) {
        if (addons.length > ADDON_INFO.getAllowedAddonsCount()) {
            throw new IllegalStateException("Too many Addons");
        }
        return new FishingChip(new SearchRange(8), List.of(addons));
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.FISHING_TYPE.get();
    }

    @Override
    protected ClaySoldierFishGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new ClaySoldierFishGoal(soldier, workAccess, scaleRange(data));
    }
}
