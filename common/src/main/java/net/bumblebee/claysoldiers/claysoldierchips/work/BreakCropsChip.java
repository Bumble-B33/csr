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

public class BreakCropsChip extends WorkGoalChip<BreakCropGoal> {
    private static final Identifier BREAK_CROPS_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "harvest");
    private static final SearchRange DEFAULT = new SearchRange(16, 2);
    private static final BreakCropsChip NO_ADDON = new BreakCropsChip(List.of());

    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    private BreakCropsChip(List<ClaySoldierChipAddon> addons) {
        super(addons, BREAK_CROPS_ASSET, 0x0E3708, 0xFFA61B);
    }

    public static BreakCropsChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new BreakCropsChip(addons);
    }

    public static BreakCropsChip create() {
        return NO_ADDON;
    }

    @Override
    protected BreakCropGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BreakCropGoal(soldier, workAccess, 1, scaleRange(DEFAULT));
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.BREAK_CROPS_TYPE.get();
    }
}
