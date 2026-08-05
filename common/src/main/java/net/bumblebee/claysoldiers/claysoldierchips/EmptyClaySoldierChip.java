package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.function.BiConsumer;

public class EmptyClaySoldierChip extends ClaySoldierChip {
    public static final EmptyClaySoldierChip EMPTY = new EmptyClaySoldierChip();

    private EmptyClaySoldierChip() {
        super(List.of());
    }

    @Override
    public void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.EMPTY_TYPE.get();
    }
}
