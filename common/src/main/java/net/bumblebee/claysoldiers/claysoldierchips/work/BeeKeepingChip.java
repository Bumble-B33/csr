package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.BeeKeepingGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class BeeKeepingChip extends WorkGoalChip<BeeKeepingGoal> {
    private static final Identifier ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "bee_keeping");
    public static final AddonInfo ADDON_INFO = AddonInfo.NO_BREAK_AND_RANGE;
    private static final SearchRange DEFAULT = new SearchRange(8, 2);
    private static final BeeKeepingChip NO_ADDON = new BeeKeepingChip(List.of());

    private BeeKeepingChip(List<ClaySoldierChipAddon> addons) {
        super(addons, ASSET, 0xFABF29, 0x0F0F0F);
    }

    public static BeeKeepingChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new BeeKeepingChip(addons);
    }

    public static BeeKeepingChip create() {
        return NO_ADDON;
    }

    @Override
    protected BeeKeepingGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BeeKeepingGoal(soldier, workAccess, scaleRange(DEFAULT));
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.BEEKEEPING_TYPE.get();
    }
}
