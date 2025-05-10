package net.bumblebee.claysoldiers.soldierpoi;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class FindNearestPoiGoal extends Goal {
    private static final int DEFAULT_RANDOM_INTERVAL = 60;

    private static final int SEARCH_RANGE = 10;
    private static final int VERTICAL_SEARCH_RANGE = 4;
    private static final int VERTICAL_SEARCH_START = 0;
    protected final int randomInterval;
    private final AbstractClaySoldierEntity clayMob;
    @Nullable
    private SoldierPoiWithSource<?> target = null;

    public FindNearestPoiGoal(AbstractClaySoldierEntity clayMob, int randomInterval) {
        this.clayMob = clayMob;
        this.randomInterval = randomInterval;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public FindNearestPoiGoal(AbstractClaySoldierEntity clayMob) {
        this(clayMob, DEFAULT_RANDOM_INTERVAL);
    }

    @Override
    public boolean canUse() {
        if (clayMob.hasControllingPassenger()) {
            return false;
        }

        if (this.randomInterval > 0 && clayMob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        findNearestPoi();
        return this.target != null;

    }
    @Override
    public boolean canContinueToUse() {
        return !clayMob.getNavigation().isDone() && !clayMob.hasControllingPassenger();
    }

    @Override
    public void start() {
        assert target != null;
        target.startPath(clayMob.getNavigation());

    }
    @Override
    public void stop() {
        assert target != null;
        target.performEffect(clayMob);
        target = null;

        clayMob.getNavigation().stop();
        super.stop();
    }

    private void findNearestPoi() {
        SoldierPoiWithSource<ItemEntity> itemPoi = clayMob.level().getEntitiesOfClass(ItemEntity.class, getTargetSearchArea(getFollowDistance())).stream()
                .map(this::getPoiWithPos).filter(this::canUsePoi).min(this.sortDistance()).orElse(null);
        if (itemPoi != null) {
            target = itemPoi;
            return;
        }
        target = findNearestBlock();
    }

    private SoldierPoiWithSource<ItemEntity> getPoiWithPos(ItemEntity entity) {
        return SoldierPoiWithSource.getPoiFromItem(entity);
    }

    protected AABB getTargetSearchArea(double pTargetDistance) {
        return this.clayMob.getBoundingBox().inflate(pTargetDistance, 4.0, pTargetDistance);
    }

    private Comparator<SoldierPoiWithSource<ItemEntity>> sortDistance() {
        return (p1, p2) -> (int) (clayMob.distanceToSqr(p1.getSource()) - clayMob.distanceToSqr(p2.getSource()));
    }

    private double getFollowDistance() {
        return clayMob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
    private boolean canUsePoi(SoldierPoiWithSource<?> poi) {
        return poi.canPerformEffect(clayMob);
    }

    @Nullable
    protected SoldierPoiWithBlock findNearestBlock() {
        BlockPos blockpos = this.clayMob.blockPosition();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int ySearch = VERTICAL_SEARCH_START; ySearch <= VERTICAL_SEARCH_RANGE; ySearch = (ySearch > 0 ? -ySearch : 1 - ySearch)) {
            for (int l = 0; l < SEARCH_RANGE; l++) {
                for (int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        blockpos$mutableblockpos.setWithOffset(blockpos, i1, ySearch - 1, j1);
                        var state = clayMob.level().getBlockState(blockpos$mutableblockpos);
                        if (!state.isAir() && this.clayMob.isWithinRestriction(blockpos$mutableblockpos)) {
                            var poi = new SoldierPoiWithBlock(state.getBlock(), blockpos$mutableblockpos);
                            if (canUsePoi(poi)) {
                                return poi;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
