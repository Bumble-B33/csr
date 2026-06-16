package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.PlaceSeedsGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class PlaceSeedsChip extends WorkGoalChip<SearchRange, PlaceSeedsGoal> {
    private static final Identifier PLANT_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "plant");
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    public PlaceSeedsChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, PLANT_ASSET, 0x0E3708, 0xF9F9F9);
    }

    public static PlaceSeedsChip create(SearchRange searchRange) {
        return new PlaceSeedsChip(searchRange, List.of());
    }

    @Override
    protected PlaceSeedsGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new PlaceSeedsGoal(soldier, workAccess, scaleRange(data));
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.PLACE_SEEDS_TYPE.get();
    }
}
