package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.dig.DigHoleGoal;
import net.minecraft.resources.Identifier;

import java.util.List;

public class DigChip extends WorkGoalChip<DigHoleGoal> {
    private static final Identifier DIG_ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "dig");
    private static final WorkGoalChip<DigHoleGoal> DIG_HOLE_GOAL = new DigChip();

    private DigChip() {
        super(List.of(), DIG_ASSET, 0x777777, 0x191919);
    }

    public static WorkGoalChip<DigHoleGoal> create() {
        return DIG_HOLE_GOAL;
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.DIG_TYPE.get();
    }

    @Override
    protected DigHoleGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new DigHoleGoal(soldier, workAccess);
    }
}
