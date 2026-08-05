package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.function.Predicate;

public abstract class AbstractWorkGoal extends Goal implements IWorkGoal {
    private static final int MAX_BREAK_TIME = 60;

    protected final ProgrammableClaySoldierEntity soldier;
    private IBlockCache<IBlockStorageAccess> capCache;
    private int breakTime = 0;
    protected final ClayMobWorkAccess workStatus;


    protected AbstractWorkGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        this.soldier = soldier;
        this.workStatus = workAccess;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Nullable
    protected BlockPos getPoiPos() {
        return soldier.getPoiPos();
    }

    protected @NonNull PoiPosInfo getPoiInfo() {
        return soldier.getPoiInfo();
    }

    protected void setCapCache() {
        if (getPoiPos() != null) {
            capCache = ClaySoldiersCommon.CAPABILITY_MANGER.createStorageCache((ServerLevel) soldier.level(), getPoiPos());
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
    protected boolean moveToPoi(double distance) {
        BlockPos pos = getPoiPos();
        return moveToPos(pos, distance);
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
     * Lets the soldier take a short break, if it is allowed to.
     * @param force forces the break
     */
    protected void takeAShortBreak(boolean force) {
        if (force || soldier.isAllowedBreak()) {
            breakTime = MAX_BREAK_TIME + (getRandom().nextInt(1, 4) * 7);
        }
    }

    protected RandomSource getRandom() {
        return soldier.getRandom();
    }

    @Override
    public Component getWorkStatus() {
        return workStatus.getWorkStatus();
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

    /**
     *
     * @param tools the possible tools
     * @return whether the soldier has reached to poi
     */
    protected boolean acquireJobItem(Collection<JobItemRequest> tools) {
        if (!moveToPoi(2d)) {
            return false;
        }
        if (tools.isEmpty() || !soldier.getCarriedStack().isEmpty()) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Required Tools is empty");
            soldier.clearPoiInfo();
            takeAShortBreak(true);
            return true;
        }

        IBlockCache<IBlockStorageAccess> cap = getCapCacheResetIfInvalid();
        if (cap != null) {
            IBlockStorageAccess storage = cap.getCapability();
            if (storage != null) {
                ItemStack res = ItemStack.EMPTY;
                for (var req : tools) {
                    res = storage.tryExtracting(req.test, req.amount);
                    if (!res.isEmpty()) {
                        break;
                    }
                }
                soldier.setCarriedStack(res);
            }
        }
        return true;
    }

    protected record JobItemRequest(Predicate<ItemStack> test, int amount) {}

    protected void pushToWardsPosition(double x, double z) {
        double xDif = x - soldier.getX();
        double zDif = z - soldier.getZ();
        double absMax = Mth.absMax(xDif, zDif);
        if (absMax >= 0.01F) {
            absMax = Math.sqrt(absMax);
            xDif /= absMax;
            zDif /= absMax;
            double invertedAbsMax = 1.0 / absMax;
            if (invertedAbsMax > 1.0) {
                invertedAbsMax = 1.0;
            }

            xDif *= invertedAbsMax;
            zDif *= invertedAbsMax;
            xDif *= 0.05F;
            zDif *= 0.05F;
            soldier.setDeltaMovement(soldier.getDeltaMovement().add(xDif, 0, zDif));
        }
    }
}
