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

public class PickUpItemsChip extends WorkGoalChip<PickUpItemsGoal> {
    private static final Identifier PICK_UP_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "pick_up");
    private static final PickUpItemsChip NO_ADDON = new PickUpItemsChip(List.of());
    private static final SearchRange DEFAULT_SEARCH_RANGE = new SearchRange(8);

    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;

    private PickUpItemsChip(List<ClaySoldierChipAddon> addons) {
        super(addons, PICK_UP_ASSET, 0x866525, 0xF9F9F9);
    }

    public static PickUpItemsChip create() {
        return NO_ADDON;
    }

    public static PickUpItemsChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new PickUpItemsChip(addons);
    }

    @Override
    protected PickUpItemsGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new PickUpItemsGoal(soldier, workAccess, scaleRange(DEFAULT_SEARCH_RANGE));
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.PICK_UP_ITEMS_TYPE.get();
    }

}
