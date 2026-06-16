package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.PickUpItemsGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class PickUpItemsChip extends WorkGoalChip<SearchRange, PickUpItemsGoal> {
    private static final Identifier PICK_UP_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "pick_up");
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    public PickUpItemsChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, PICK_UP_ASSET, 0x866525, 0xF9F9F9);
    }

    public static PickUpItemsChip create(SearchRange searchRange) {
        return new PickUpItemsChip(searchRange, List.of());
    }

    @Override
    protected PickUpItemsGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new PickUpItemsGoal(soldier, workAccess, scaleRange(data));
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.PICK_UP_ITEMS_TYPE.get();
    }

}
