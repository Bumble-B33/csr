package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WorkSelectorGoal extends Goal implements SoldierStatusHolder {
    public static final String WORK_STATUS_PAIR_LANG = IWorkGoal.STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "work_pair");
    public static final String WORK_STATUS_SOMETHING_LANG = IWorkGoal.STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "something");
    public static final String WORK_STATUS_RESTING_LANG = IWorkGoal.STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "resting");

    private static final int MAX_INDEX_SIZE = 15;
    private static final String WORK_MODE_TAG = "WorkMode";
    public static final int RESTING_INDEX = -1;
    private final AbstractClaySoldierEntity soldier;
    private final Goal[] availableWorkGoals;
    private int workIndex = RESTING_INDEX;
    @Nullable
    private Goal activeGoal;

    public WorkSelectorGoal(AbstractClaySoldierEntity soldier, List<Goal> goals) {
        this.soldier = soldier;
        if (goals.stream().anyMatch(goal -> !(goal instanceof IWorkGoal))) {
            ClaySoldiersCommon.LOGGER.warn("ClaySoldier has Work that does not extend {}", IWorkGoal.class.getSimpleName());
        }

        if (goals.size() > MAX_INDEX_SIZE) {
            this.availableWorkGoals = goals.subList(0, 15).toArray(new Goal[0]);
            ErrorHandler.INSTANCE.error("Cannot have more than 15 available work goals");
        } else {
            this.availableWorkGoals = goals.toArray(new Goal[0]);
        }
    }

    public boolean isWorking() {
        return workIndex >= 0;
    }

    private void setWorkMode(int mode) {
        if (mode < availableWorkGoals.length) {
            workIndex = mode;
        }

        if (workIndex >= 0) {
            activeGoal = availableWorkGoals[workIndex];
        } else {
            activeGoal = null;
        }
    }

    /**
     * Returns whether the soldier should pick up items.
     */
    public boolean workRequiresItemPickUp() {
        return !soldier.isInSittingPose() && activeGoal instanceof IWorkGoal iWorkGoal && iWorkGoal.workRequiresItemPickUp();
    }
    /**
     Returns whether the soldier should carry up items.
     */
    public boolean workRequiresItemCarrying() {
        return !soldier.isInSittingPose() && activeGoal instanceof IWorkGoal workGoal && workGoal.workRequiresItemCarrying();
    }

    /**
     * Cycles through the available und suitable work goals.
     */
    public void cycleWorkMode() {
        if (workIndex < availableWorkGoals.length - 1) {
            workIndex++;
        } else {
            workIndex = RESTING_INDEX;
        }
        resetGoal();
        setWorkMode(workIndex);
        onWorkStatusChange();
    }

    /**
     * Returns the display name of the currently active goal
     */
    public Component getWorkDisplayName() {
        if (activeGoal instanceof IWorkGoal workGoal) {
            return workGoal.getDisplayName();
        }
        if (workIndex == RESTING_INDEX) {
            return Component.translatable(WORK_STATUS_RESTING_LANG);
        }
        return Component.translatable(WORK_STATUS_SOMETHING_LANG);
    }

    /**
     * Returns whether the Soldier should not leave the current area.
     */
    public boolean shouldStayAtWork() {
        return workIndex > RESTING_INDEX || getSoldierPoiPos() != null;
    }

    @Override
    public boolean canUse() {
        return mayUse(Goal::canUse);
    }

    @Override
    public boolean canContinueToUse() {
        return mayUse(Goal::canContinueToUse);
    }

    private boolean mayUse(Predicate<Goal> predicate) {
        if (soldier.isOrderedToSit()) {
            return false;
        }
        if (soldier.getClayTeamOwnerUUID() == null) {
            return false;
        }
        if (workIndex >= 0 && soldier.getAttackType().canWork() && checkGoal(predicate)) {
            return true;
        }
        moveToPoi(getSoldierPoiPos());
        return false;
    }
    private void moveToPoi(BlockPos poi) {
        if (poi != null && !poi.closerToCenterThan(soldier.position(), 12)) {
            soldier.getNavigation().moveTo(poi.getX() + 0.5, (poi.getY()), poi.getZ() + 0.5, 1);
        }
    }

    @Override
    public void start() {
        doGoalEffect(Goal::start);
    }

    @Override
    public void stop() {
        doGoalEffect(Goal::stop);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return checkGoal(Goal::requiresUpdateEveryTick);
    }

    @Override
    public void tick() {
        doGoalEffect(Goal::tick);
    }

    private void doGoalEffect(Consumer<Goal> action) {
        if (activeGoal != null) {
            action.accept(activeGoal);
        }
    }

    private boolean checkGoal(Predicate<Goal> predicate) {
        return activeGoal != null && predicate.test(activeGoal);
    }

    @Nullable
    protected BlockPos getSoldierPoiPos() {
        return soldier.getPoiPos();
    }

    protected void onWorkStatusChange() {
        byte goalStatus = activeGoal instanceof IWorkGoal goal ? goal.getStatus() : 0;
        soldier.setDataWorkStatus(encodeWorkStatusToByte(workIndex, goalStatus));
    }

    /**
     * Encodes the given index and status in to a byte.
     * The first 5 bit represent the index so it ranges between [-16, {@value MAX_INDEX_SIZE}].
     * The last 3 bits represent the status [0, 7]
     */
    public static byte encodeWorkStatusToByte(int workIndex, int status) {
        return (byte) ((workIndex << 3) | (status & 7));
    }

    /**
     * Returns the display name of the active goal and its status from the give byte.
     * The first 5 bit represent the index, ranging between [-16, {@value MAX_INDEX_SIZE}].
     * The last 3 bits represent the status, ranging between [0, 7]
     */
    @Nullable
    private Component decodeWorkStatus(byte data) {
        int rebuiltWorkIndex = (data >> 3) & 0x1F;
        rebuiltWorkIndex = (rebuiltWorkIndex << 27) >> 27;
        byte goalStatus = (byte) (data & 7);
        if (rebuiltWorkIndex <= RESTING_INDEX) {
            return null;
        }

        if (availableWorkGoals[rebuiltWorkIndex] instanceof IWorkGoal workGoal) {
            return Component.translatable(WORK_STATUS_PAIR_LANG, workGoal.getDisplayName(), workGoal.decodeStatus(goalStatus));
        } else {
            return Component.translatable(IWorkGoal.DEFAULT_STATUS_LANG);
        }
    }
    @Override
    public @Nullable Component getStatusDisplayName() {
        return soldier.getAttackType().canWork() ? decodeWorkStatus(soldier.getDataWorkStatus()) : null;
    }

    /**
     * Save this {@code WorkSelector} to the given {@code CompoundTag}
     */
    public void saveToTag(CompoundTag tag) {
        tag.putByte(WORK_MODE_TAG, (byte) workIndex);
    }

    /**
     * Initialise this {@code WorkSelector} from the given {@code CompoundTag}
     */
    public void readFromTag(CompoundTag tag) {
        setWorkMode(tag.getByte(WORK_MODE_TAG));
        onWorkStatusChange();
    }

    /**
     * Called when the active Goal is stopped. Including by entity death or unloading
     */
    public void resetGoal() {
        if (activeGoal != null) {
            activeGoal.stop();
        }
    }

    @Override
    public String toString() {
        return "WorkSelectorGoal{%s/%s [%s]}".formatted(
                workIndex,
                availableWorkGoals.length - 1,
                activeGoal == null ? "Null" : (activeGoal instanceof IWorkGoal workGoal ? workGoal.asString() : activeGoal.getClass().getSimpleName())
        );
    }
}
