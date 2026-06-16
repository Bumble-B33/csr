package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractWorkGoal extends Goal implements IWorkGoal {
    public static final String BREAK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "on_break");
    public static final String STUCK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "stuck");
    public static final String SEARCHING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "searching_item");
    public static final String CARRYING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "carrying");
    public static final String REQUIRES_POI_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "requires_poi");
    public static final String RETURNING_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "returning");
    public static final String CANNOT_FIND_ITEM_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "cannot_find_item");

    protected static final byte BREAK_ID = 0;
    protected static final byte SEARCHING_ID = 1;
    protected static final byte CARRYING_ID = 2;
    protected static final byte REQUIRES_POI_ID = 3;
    protected static final byte RETURNING_ID = 4;
    protected static final byte CANNOT_FIND_ITEM_ID = 5;

    private static final int MAX_BREAK_TIME = 60;
    private static final byte MAX_STATUSES = Byte.MAX_VALUE;

    protected final ProgrammableClaySoldierEntity soldier;
    protected final ClayMobWorkAccess workAccess;
    private IBlockCache<IBlockStorageAccess> capCache;
    private int breakTime = 0;
    private final List<? extends Component> statuses;

    public AbstractWorkGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        this(soldier, workAccess, List.of(BREAK_LANG, SEARCHING_LANG, CARRYING_LANG, REQUIRES_POI_LANG, RETURNING_LANG, CANNOT_FIND_ITEM_LANG));
    }
    protected AbstractWorkGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess, List<String> statusesKey) {
        this.soldier = soldier;
        this.workAccess = workAccess;
        if (statusesKey.size() >= MAX_STATUSES) {
            throw new IllegalArgumentException("Cannot have more than %s Statuses per WorkGoal".formatted(MAX_STATUSES));
        }
        this.statuses = statusesKey.stream().map(Component::translatable).toList();
    }

    @Nullable
    protected BlockPos getPoiPos() {
        return soldier.getPoiPos();
    }

    protected void setCapCache() {
        if (getPoiPos() != null) {
            capCache = ClaySoldiersCommon.CAPABILITY_MANGER.create((ServerLevel) soldier.level(), getPoiPos());
        } else {
            capCache = null;
        }
    }
    @Nullable
    protected IBlockCache<IBlockStorageAccess> getCapCache() {
        return capCache;
    }


    protected IBlockCache<IBlockStorageAccess> getCapCacheResetIfInvalid() {
        if (capCache == null) {
            setCapCache();
        } else if (!capCache.pos().equals(getPoiPos())) {
            setCapCache();
        }
        return capCache;
    }

    /**
     * Returns whether this soldier is on a break.
     */
    protected boolean isOnBreak() {
        if (breakTime > 0) {
            breakTime--;
            return true;
        }
        return false;
    }

    /**
     * Move this soldier to the poi, if there is one.
     * @return whether it has reached the poi.
     */
    protected boolean moveToPoi() {
        BlockPos pos = getPoiPos();
        return moveToPos(pos, 2d);
    }

    /**
     * Move this soldier to the given pos, if there is one.
     * @return whether it has reached the given pos.
     */
    protected boolean moveToPos(@Nullable BlockPos pos, double distance) {
        if (pos != null) {
            if (pos.closerToCenterThan(soldier.position(), distance)) {
                this.soldier.getNavigation().stop();
                return true;
            } else {
                this.soldier.getNavigation().moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1.2);
                return false;
            }
        }
        return false;
    }

    /**
     * Sets the status id of this work.
     */
    public void setStatus(byte status) {
        workAccess.setDataWorkStatus(status);
    }

    /**
     * Lets the soldier take a short break, if it is allowed to.
     * @param force forces the break
     */
    protected void takeAShortBreak(boolean force) {
        if (force || soldier.isAllowedBreak()) {
            breakTime = MAX_BREAK_TIME + (getRandom().nextInt(1, 4) * 7);
            setStatus(BREAK_ID);
        }
    }

    protected RandomSource getRandom() {
        return soldier.getRandom();
    }

    @Override
    public Component decodeStatus(byte id) {
        if (statuses.size() <= id || id < 0) {
            return IWorkGoal.super.decodeStatus(id);
        }
        return statuses.get(id);
    }

    @Nullable
    protected BlockPos findNearestValidPos(BlockPos center, final int verticalSearchStart, final int verticalSearchRange, final int horizontalSearchRange, Predicate<BlockPos> isValid) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int searchIndex = verticalSearchStart; searchIndex <= verticalSearchRange; searchIndex = searchIndex > 0 ? -searchIndex : 1 - searchIndex) {
            for (int rangeIndex = 0; rangeIndex < horizontalSearchRange; rangeIndex++) {
                for (int i1 = 0; i1 <= rangeIndex; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < rangeIndex && i1 > -rangeIndex ? rangeIndex : 0; j1 <= rangeIndex; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        mutableBlockPos.setWithOffset(center, i1, searchIndex - 1, j1);
                        if (this.soldier.isWithinHome(mutableBlockPos) && isValid.test(mutableBlockPos)) {
                            return mutableBlockPos.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}
