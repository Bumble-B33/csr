package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractWorkGoal extends Goal implements IWorkGoal {
    public static final String BREAK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "on_break");
    public static final String STUCK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "stuck");
    public static final String SEARCHING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "searching_item");
    public static final String CARRYING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "carrying");
    public static final String REQUIRES_POI_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "requires_poi");
    public static final String RETURNING_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "returning");
    protected static final byte BREAK_ID = 0;
    protected static final byte SEARCHING_ID = 1;
    protected static final byte CARRYING_ID = 2;
    protected static final byte REQUIRES_POI_ID = 3;
    protected static final byte RETURNING_ID = 4;
    private static final int MAX_BREAK_TIME = 20;

    protected final AbstractClaySoldierEntity soldier;
    private final Supplier<WorkSelectorGoal> workSelector;
    private IBlockCache<IBlockStorageAccess> capCache;
    private int breakTime = 0;
    private byte status = 0;
    private final List<? extends Component> statuses;

    public AbstractWorkGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector) {
        this(soldier, workSelector, List.of(BREAK_LANG, SEARCHING_LANG, CARRYING_LANG, REQUIRES_POI_LANG, RETURNING_LANG));
    }
    protected AbstractWorkGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector, List<String> statusesKey) {
        this.soldier = soldier;
        this.workSelector = workSelector;
        if (statusesKey.size() >= 7) {
            throw new IllegalArgumentException("Cannot have more than 7 Statuses per WorkGoal");
        }
        this.statuses = statusesKey.stream().map(Component::translatable).toList();
    }

    @Nullable
    protected BlockPos getPoiPos() {
        return workSelector.get().getSoldierPoiPos();
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
        if (pos != null) {
            if (pos.closerToCenterThan(soldier.position(), 2f)) {
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
        this.status = status;
        workSelector.get().onWorkStatusChange();
    }
    protected void takeAShortBreak() {
        breakTime = MAX_BREAK_TIME;
        setStatus(BREAK_ID);
    }

    @Override
    public byte getStatus() {
        return status;
    }

    @Override
    public Component decodeStatus(byte id) {
        if (statuses.size() <= id || id < 0) {
            return IWorkGoal.super.decodeStatus(id);
        }
        return statuses.get(id);
    }
}
