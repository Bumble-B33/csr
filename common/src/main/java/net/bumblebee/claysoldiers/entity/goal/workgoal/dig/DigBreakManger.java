package net.bumblebee.claysoldiers.entity.goal.workgoal.dig;

import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DigBreakManger {
    private static final DigBreakManger INSTANCE = new DigBreakManger();
    private final Map<BlockPos, BreakData> breakPosStateMap;

    private DigBreakManger() {
        this.breakPosStateMap = new HashMap<>();
    }

    public static DigBreakManger get() {
        return INSTANCE;
    }

    /**
     * Register a Clay Soldier for breaking a certain block
     *
     * @param pos    the pos of the block
     * @param entity the Clay Soldier breaking the block
     */
    public void registerPos(BlockPos pos, Entity entity) {
        var breakData = breakPosStateMap.get(pos);
        if (breakData == null) {
            breakPosStateMap.put(pos, new BreakData(entity));
        } else {
            breakData.addEntity(entity);
        }
    }

    /**
     * Unregistered a Clay Soldier for breaking a certain block
     *
     * @param pos    the pos of the block
     * @param entity the Clay Soldier breaking the block
     */
    public void unregisterPos(BlockPos pos, Entity entity) {
        var breakData = breakPosStateMap.get(pos);
        if (breakData != null) {
            breakData.removeEntity(entity);
            if (breakData.isEmpty()) {
                breakPosStateMap.remove(pos);
                entity.level().destroyBlockProgress(entity.getId(), pos, -1);
            }
        } else {
            ErrorHandler.INSTANCE.debug("Trying to unregister an Entity for a Empty BreakData");
        }
    }

    /**
     * Increases the block break progress of the block at the give pos.
     *
     * @return <p>{@code -2} - Error</p>
     * <p>{@code -1} - Destroyed</p>
     * <p>{@code 0} - Breaking</p>
     * <p>{@code 1+} - Needed more Soldiers to break this block</p>
     */
    public int increaseBreakProgress(BlockPos pos, Level level) {
        BreakData breakData = breakPosStateMap.get(pos);
        if (breakData == null) {
            ErrorHandler.INSTANCE.error("Trying to break a block with non existing break data");
            return -2;
        }
        var state = level.getBlockState(pos);
        float destroySpeed = state.getDestroySpeed(level, pos);

        int soldierNeeded = getSoldierNeededForState(state);
        if (soldierNeeded > breakData.size()) {
            return soldierNeeded;
        }

        if (breakData.progress >= destroySpeed * 10 * 2 * soldierNeeded) {
            breakPosStateMap.remove(pos);
            level.destroyBlockProgress(breakData.getAny(), pos, -1);
            return -1;
        } else {
            // Progress between [0,10)
            int currentBreakProgress = (int) (breakData.progress / (destroySpeed * 2 * soldierNeeded));
            if (breakData.lastBreakProgress != currentBreakProgress) {
                level.destroyBlockProgress(breakData.anyEntity, pos, currentBreakProgress);
                breakData.lastBreakProgress = currentBreakProgress;
            }
            breakData.progress++;
            return 0;
        }
    }

    private static int getSoldierNeededForState(BlockState state) {
        if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            return 64;
        }
        if (state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) {
            return 16;
        }
        if (state.is(BlockTags.INCORRECT_FOR_IRON_TOOL)) {
            return 8;
        }
        if (state.is(BlockTags.INCORRECT_FOR_GOLD_TOOL)) {
            return 4;
        }
        if (state.is(BlockTags.INCORRECT_FOR_STONE_TOOL)) {
            return 4;
        }
        if (state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL)) {
            return 2;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "DigBreakManger: " + breakPosStateMap.size() + breakPosStateMap.values();
    }

    private static class BreakData {
        private final Set<Entity> working;
        private int progress = 0;
        private int lastBreakProgress = -1;
        private int anyEntity;

        public BreakData(Entity entity) {
            working = new HashSet<>();
            working.add(entity);
            anyEntity = entity.getId();
        }

        public int getAny() {
            return anyEntity;
        }

        public void addEntity(Entity entity) {
            working.add(entity);
        }

        public void removeEntity(Entity entity) {
            if (!working.remove(entity)) {
                ErrorHandler.INSTANCE.debug("Tried removing non existing entity from BreakData");
            }
            if (anyEntity == entity.getId() && !working.isEmpty()) {
                anyEntity = working.iterator().next().getId();
            }
        }

        public int size() {
            return working.size();
        }

        public boolean isEmpty() {
            return working.isEmpty();
        }

        @Override
        public String toString() {
            return "BD:" + working.size();
        }
    }
}
