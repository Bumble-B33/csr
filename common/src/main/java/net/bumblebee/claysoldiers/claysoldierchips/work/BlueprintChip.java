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

public class BlueprintChip extends WorkGoalChip<BuildBlueprintGoal> {
    private static final Identifier ASSET_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint");
    private static final SearchRange DEFAULT = new SearchRange(16, 2);
    private static final BlueprintChip NO_ADDON = new BlueprintChip(List.of());

    public static final AddonInfo ADDON_INFO = AddonInfo.EMPTY;

    private BlueprintChip(List<ClaySoldierChipAddon> addons) {
        super(addons, ASSET_ID, 0x2A4DA0, 0xB6CDED);
    }

    public static BlueprintChip create() {
        return new BlueprintChip(List.of());
    }

    public static BlueprintChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new BlueprintChip(addons);
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.BUILD_BLUEPRINT_TYPE.get();
    }

    @Override
    protected BuildBlueprintGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new BuildBlueprintGoal(soldier, workAccess, scaleRange(DEFAULT));
    }
}
