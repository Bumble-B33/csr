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

public class PlaceSeedsChip extends WorkGoalChip<PlaceSeedsGoal> {
    private static final Identifier PLANT_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "plant");
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;
    private static final PlaceSeedsChip NO_ADDON = new PlaceSeedsChip(List.of());

    private static final SearchRange DEFAULT = new SearchRange(16, 2);

    private PlaceSeedsChip(List<ClaySoldierChipAddon> addons) {
        super(addons, PLANT_ASSET, 0x0E3708, 0xF9F9F9);
    }

    public static PlaceSeedsChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new PlaceSeedsChip(addons);
    }

    public static PlaceSeedsChip create() {
        return NO_ADDON;
    }

    @Override
    protected PlaceSeedsGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new PlaceSeedsGoal(soldier, workAccess, scaleRange(DEFAULT));
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.PLACE_SEEDS_TYPE.get();
    }
}
