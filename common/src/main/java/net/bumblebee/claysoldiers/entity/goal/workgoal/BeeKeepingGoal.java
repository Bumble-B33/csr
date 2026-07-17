package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class BeeKeepingGoal extends AbstractWorkGoal {
    private static final Predicate<ItemStack> SHEARS = s -> s.is(Items.SHEARS);
    private static final Predicate<ItemStack> GLASS_BOTTLE = s -> s.is(Items.GLASS_BOTTLE);

    private static final List<JobItemRequest> REQUIRED_TOOLS = List.of(
      new JobItemRequest(SHEARS, 1),
      new JobItemRequest(GLASS_BOTTLE, Items.GLASS_BOTTLE.getDefaultMaxStackSize())
    );
    @Nullable
    private BlockPos beehivePos;

    private final int horizontalSearchRange;
    private final int verticalSearchRange;

    public BeeKeepingGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess, SearchRange searchRange) {
        super(soldier, workAccess, List.of(BREAK_LANG, SEARCHING_LANG, CARRYING_LANG, REQUIRES_POI_LANG));

        this.verticalSearchRange = searchRange.verticalRange();
        this.horizontalSearchRange = searchRange.horizontalRange();
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public boolean canUse() {
        return !isOnBreak();
    }

    @Override
    public void tick() {
        if (hasBottle() || hasShears()) {
            if (beehivePos != null) {
                if (!isValidHive(beehivePos)) {
                    beehivePos = null;
                    takeAShortBreak(false);
                } else {
                    if (moveToPos(beehivePos, 1)) {
                        harvestBeehive(beehivePos);
                    }
                }
            } else {
                beehivePos = findNearestValidPos(soldier.blockPosition(), 0, verticalSearchRange, horizontalSearchRange, this::isValidHive);
                setStatus(SEARCHING_ID);
            }
        } else {
            if (acquireJobItem(REQUIRED_TOOLS)) {
                takeAShortBreak(false);
            }
        }
    }


    @Override
    public boolean workRequiresItemCarrying(ItemStack stack) {
        return SHEARS.test(stack) || GLASS_BOTTLE.test(stack);
    }

    @Override
    public boolean workRequiresItemPickUp(ItemStack stack) {
        return SHEARS.test(stack) || GLASS_BOTTLE.test(stack);
    }

    private boolean hasBottle() {
        return GLASS_BOTTLE.test(soldier.getCarriedStack());
    }

    private boolean hasShears() {
        return SHEARS.test(soldier.getCarriedStack());
    }

    private void harvestBeehive(@NotNull BlockPos beehivePos) {
        ServerLevel level = getServerLevel(soldier);
        BlockState state = level.getBlockState(beehivePos);

        if (hasShears()) {
            BeehiveBlock.dropHoneycomb(
                    level,
                    soldier.getCarriedStack(),
                    state,
                    level.getBlockEntity(beehivePos),
                    soldier,
                    beehivePos
            );
            soldier.getCarriedStack().hurtAndBreak(1, level, null, s -> {
            });
            level.gameEvent(soldier, GameEvent.SHEAR, beehivePos);
        } else if (hasBottle()) {
            soldier.spawnAtLocation(level, new ItemStack(Items.HONEY_BOTTLE));
            soldier.getCarriedStack().shrink(1);

            level.gameEvent(soldier, GameEvent.FLUID_PICKUP, beehivePos);
        }

        resetHive(state, beehivePos, shouldAngerBees());
    }

    private void resetHive(BlockState state, BlockPos pos, boolean angerBees) {
        Level level = soldier.level();
        if (state.getBlock() instanceof BeehiveBlock block) {
            if (angerBees) {
                block.releaseBeesAndResetHoneyLevel(level, state, pos, null, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            } else {
                block.resetHoneyLevel(level, state, pos);
            }
        } else {
            level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 0), 3);
        }
    }

    private boolean shouldAngerBees() {
        return false;
    }

    private boolean isValidHive(BlockPos pos) {
        BlockState state = soldier.level().getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            return state.getValue(BlockStateProperties.LEVEL_HONEY) >= BeehiveBlock.MAX_HONEY_LEVELS;
        }


        return false;
    }
}
