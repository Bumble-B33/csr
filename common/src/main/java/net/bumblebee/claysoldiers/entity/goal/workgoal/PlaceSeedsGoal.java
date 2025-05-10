package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlaceSeedsGoal extends AbstractWorkGoal {
    public static final String PLACING_SEEDS_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "placing_seeds");
    private static final TagKey<Item> SEEDS = ItemTags.VILLAGER_PLANTABLE_SEEDS;
    @Nullable
    private BlockPos farmLandPos = null;
    private final int searchRange;
    private final int verticalSearchRange;
    protected int verticalSearchStart;
    protected int tryTicks;
    private int maxStayTicks;

    public PlaceSeedsGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector, int searchRange) {
        super(soldier, workSelector);
        this.searchRange = searchRange;
        this.verticalSearchRange = 2;
    }

    @Override
    public boolean canUse() {
        if (isOnBreak()) {
            return false;
        }
        return getPoiPos() != null && getCapCacheResetIfInvalid() != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (isOnBreak()) {
            return false;
        }
        if (!soldier.getCarriedStack().isEmpty()) {
            return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.soldier.level(), this.farmLandPos);
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (soldier.getCarriedStack().isEmpty()) {
            getSeedFromChest();
            setStatus(CARRYING_ID);
        } else {
            setStatus(SEARCHING_ID);
            if (farmLandPos == null) {
                findNearestBlock();
                moveMobToBlock();
                this.tryTicks = 0;
                this.maxStayTicks = this.soldier.getRandom().nextInt(this.soldier.getRandom().nextInt(1200) + 1200) + 1200;
                return;
            }
            BlockPos blockpos = this.getMoveToTarget();
            if (!blockpos.closerToCenterThan(this.soldier.position(), 2)) {
                this.tryTicks++;
                if (this.shouldRecalculatePath()) {
                    this.soldier.getNavigation().moveTo(blockpos.getX() + 0.5, blockpos.getY(), blockpos.getZ() + 0.5, 1);
                }
            } else {
                plantSeed();
                this.tryTicks--;
            }

        }
    }

    private void getSeedFromChest() {
        if (moveToPoi()) {
            if (getCapCacheResetIfInvalid() != null) {
                assert getCapCache() != null;
                var cap = getCapCache().getCapability();
                if (cap == null) {
                    return;
                }
                soldier.setCarriedStack(cap.tryExtracting(stack -> stack.is(SEEDS), 1));
            }
        }
        if (soldier.getCarriedStack().isEmpty()) {
            takeAShortBreak();
        }
    }

    private void plantSeed() {
        ItemStack seed = soldier.getCarriedStack();
        Level level = soldier.level();
        if (farmLandPos != null && isValidTarget(level, farmLandPos)) {
            BlockPos aboveFarmlandPos = farmLandPos.above();
            if (!seed.isEmpty() && seed.is(SEEDS) && seed.getItem() instanceof BlockItem blockitem) {

                BlockState blockState = blockitem.getBlock().defaultBlockState();
                level.setBlockAndUpdate(aboveFarmlandPos, blockState);
                level.gameEvent(GameEvent.BLOCK_PLACE, aboveFarmlandPos, GameEvent.Context.of(soldier, blockState));
                soldier.setCarriedStack(ItemStack.EMPTY);
                farmLandPos = null;
            } else {
                soldier.dropItemStack(soldier.getCarriedStack().copy());
                soldier.setCarriedStack(ItemStack.EMPTY);
            }
        } else {
            findNearestBlock();
        }
    }

    protected boolean findNearestBlock() {
        BlockPos center = this.soldier.blockPosition();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int searchIndex = this.verticalSearchStart; searchIndex <= this.verticalSearchRange; searchIndex = searchIndex > 0 ? -searchIndex : 1 - searchIndex) {
            for (int rangeIndex = 0; rangeIndex < this.searchRange; rangeIndex++) {
                for (int i1 = 0; i1 <= rangeIndex; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < rangeIndex && i1 > -rangeIndex ? rangeIndex : 0; j1 <= rangeIndex; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        blockpos$mutableblockpos.setWithOffset(center, i1, searchIndex - 1, j1);
                        if (this.soldier.isWithinRestriction(blockpos$mutableblockpos) && this.isValidTarget(this.soldier.level(), blockpos$mutableblockpos)) {
                            this.farmLandPos = blockpos$mutableblockpos;
                            return true;
                        }
                    }
                }
            }
        }
        takeAShortBreak();
        return false;
    }

    protected void moveMobToBlock() {
        if (farmLandPos == null) {
            return;
        }
        this.soldier.getNavigation().moveTo(this.farmLandPos.getX() + 0.5, (this.farmLandPos.getY() + 1), this.farmLandPos.getZ() + 0.5, 1);
    }

    protected boolean isValidTarget(LevelReader pLevel, @Nullable BlockPos pPos) {
        return pPos != null && pLevel.getBlockState(pPos).getBlock() instanceof FarmBlock
                && pLevel.getBlockState(pPos.above()).isAir();
    }

    protected BlockPos getMoveToTarget() {
        return this.farmLandPos.above();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    @Override
    public boolean workRequiresItemCarrying() {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(PLACING_SEEDS_LANG);
    }

}
