package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.BeeKeepingGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;

import java.util.List;

public class BeeKeepingChip extends WorkGoalChip<SearchRange, BeeKeepingGoal> {
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    public BeeKeepingChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, null, 0xFABF29, 0x0F0F0F);
    }

    public static BeeKeepingChip create(SearchRange searchRange) {
        return new BeeKeepingChip(searchRange, List.of());
    }

    @Override
    protected BeeKeepingGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BeeKeepingGoal(soldier, workAccess, scaleRange(data));
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.BEEKEEPING_TYPE.get();
    }
}
