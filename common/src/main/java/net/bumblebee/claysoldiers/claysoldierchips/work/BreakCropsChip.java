package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.BreakCropGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class BreakCropsChip extends WorkGoalChip<SearchRange, BreakCropGoal> {
    private static final Identifier BREAK_CROPS_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "harvest");
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    public BreakCropsChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, BREAK_CROPS_ASSET, 0x0E3708, 0xFFA61B);
    }

    public static BreakCropsChip create(SearchRange searchRange) {
        return new BreakCropsChip(searchRange, List.of());
    }

    @Override
    protected BreakCropGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BreakCropGoal(soldier, workAccess, scaleRange(data));
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.BREAK_CROPS_TYPE.get();
    }
}
