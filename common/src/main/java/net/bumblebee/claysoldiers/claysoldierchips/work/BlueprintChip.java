package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.BuildBlueprintGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.resources.Identifier;

import java.util.List;

public class BlueprintChip extends WorkGoalChip<SearchRange, BuildBlueprintGoal> {
    private static final Identifier ASSET_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint");
    public static final AddonInfo ADDON_INFO = AddonInfo.EMPTY;

    public BlueprintChip(SearchRange data, List<ClaySoldierChipAddon> addons) {
        super(data, addons, ASSET_ID, 0x2A4DA0, 0xB6CDED);
    }

    public static BlueprintChip create(SearchRange searchRange) {
        return new BlueprintChip(searchRange, List.of());
    }

    @Override
    public Type<SearchRange> getType() {
        return ClaySoldierChips.BUILD_BLUEPRINT_TYPE.get();
    }

    @Override
    protected BuildBlueprintGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BuildBlueprintGoal(soldier, workAccess, scaleRange(data));
    }
}
