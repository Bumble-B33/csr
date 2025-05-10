package net.bumblebee.claysoldiers.entity.goal.workgoal.dig;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.goal.workgoal.AbstractWorkGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.WorkSelectorGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DigHoleGoal extends AbstractWorkGoal {
    public static final String DIG_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig");
    public static final String BREAKING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig.breaking");
    public static final String UNBREAKABLE_BLOCK = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig.unbreakable_block");

    private static final byte BREAKING_ID = 2;
    private static final byte UNBREAKABLE_ID = 4;

    private final Level level;
    @Nullable
    private BlockPos currentPos;
    @Nullable
    private BlockPos lastPoiPos;
    @Nullable
    private StairPosMap stairPosMap;
    private final DigBreakManger digBreakManger;

    public DigHoleGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector) {
        super(soldier, workSelector, List.of(BREAK_LANG, SEARCHING_LANG, BREAKING_LANG, REQUIRES_POI_LANG, UNBREAKABLE_BLOCK));
        this.level = soldier.level();
        if (!level.isClientSide) {
            this.digBreakManger = DigBreakManger.get();
        } else {
            this.digBreakManger = null;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(DIG_LANG);
    }


    @Override
    public void tick() {
        if (lastPoiPos == null) {
            lastPoiPos = getPoiPos();
            return;
        }

        if (stairPosMap == null || currentPos == null) {
            setDigging();
            return;
        }

        if (!lastPoiPos.equals(getPoiPos())) {
            resetBreakProgress();
            setDigging();
            return;
        }


        boolean requiresStair = stairPosMap.requiresStair(currentPos);
        if (isBlockConsideredBroken(currentPos, requiresStair)) {
            currentPos = getNextPos(currentPos instanceof BlockPos.MutableBlockPos mut ? mut : currentPos.mutable());
            return;
        }
        if (moveToCurrentPos()) {
            if (requiresStair && level.getBlockState(currentPos).isAir()) {
                stairPosMap.placeStair(currentPos, level);
            } else {
                breakBlock(currentPos);
            }
        } else {
            setStatus(SEARCHING_ID);
        }

    }

    private BlockPos getNextPos(BlockPos.MutableBlockPos current) {
        int xStart = lastPoiPos.getX() - 2;
        int zStart = lastPoiPos.getZ() - 2;
        int nextIndex = (current.getX() - xStart) * 5 + (current.getZ() - zStart);

        nextIndex++;

        if (nextIndex >= 25) {
            return lastPoiPos.offset(-2, current.getY() - lastPoiPos.getY() - 1, -2);
        }
        if (nextIndex <= 0) {
            ErrorHandler.INSTANCE.hide("Something went wrong while trying to find the next block to break");
        }

        int xRelative = nextIndex / 5;
        int zRelative = nextIndex % 5;
        current.set(xRelative + xStart, current.getY(), zRelative + zStart);
        return current;
    }

    @Override
    public boolean canUse() {
        if (getPoiPos() == null) {
            setStatus(REQUIRES_POI_ID);
            return false;
        }
        setStatus(SEARCHING_ID);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (lastPoiPos == null) {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    public void start() {
        setDigging();
    }

    @Override
    public void stop() {
        resetBreakProgress();
    }


    private void breakBlock(BlockPos pos) {
        digBreakManger.registerPos(pos, soldier);

        switch (digBreakManger.increaseBreakProgress(pos, level)) {
            case -2: lastPoiPos = null;
            case -1: level.destroyBlock(pos, true, soldier);
            case 0: setStatus(BREAKING_ID);
            default: setStatus(UNBREAKABLE_ID);
        }
    }

    private boolean isBlockConsideredBroken(BlockPos pos, boolean stair) {
        var state = level.getBlockState(pos);
        if (state.getDestroySpeed(level, pos) < 0) {
            return true;
        }

        if (stair) {
            return state.is(Blocks.COBBLESTONE_SLAB) || state.is(Blocks.COBBLESTONE_STAIRS);
        }
        return state.isAir();
    }

    private void setDigging() {
        lastPoiPos = getPoiPos();
        if (lastPoiPos != null) {
            currentPos = lastPoiPos.offset(-2, 0, -2).mutable();
            stairPosMap = new StairPosMap(lastPoiPos);
        } else {
            ErrorHandler.INSTANCE.hide("Tried to start digging but has not poi");
        }
    }

    private void resetBreakProgress() {
        if (currentPos != null) {
            digBreakManger.unregisterPos(currentPos, soldier);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean moveToCurrentPos() {
        if (currentPos != null) {
            if (currentPos.closerToCenterThan(soldier.position(), 2f)) {
                this.soldier.getNavigation().stop();
                return true;
            } else {
                this.soldier.getNavigation().moveTo(currentPos.getX() + 0.5, currentPos.getY() + 1, currentPos.getZ() + 0.5, 1);
                return false;
            }
        }
        return false;
    }
}
