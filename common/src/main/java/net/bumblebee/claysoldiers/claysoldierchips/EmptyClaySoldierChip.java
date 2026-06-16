package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.function.BiConsumer;

public class EmptyClaySoldierChip extends ClaySoldierChip<Unit> {
    public static final String EMPTY_CHIP_DATA_LANG = LANG_PREFIX + ".data.empty";
    public static final EmptyClaySoldierChip EMPTY = new EmptyClaySoldierChip();

    private EmptyClaySoldierChip() {
        super(Unit.INSTANCE, List.of());
    }

    @Override
    public void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
    }

    @Override
    public Type<Unit> getType() {
        return ClaySoldierChips.EMPTY_TYPE.get();
    }
}
