package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClaySoldierFishGoal extends AbstractWorkGoal {
    public static final String FISH_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish");
    public static final String IS_ANKER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.anker");
    public static final String SEARCHING_FOR_WATER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.searching_water");
    public static final String SEARCHING_ANKER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.searching_anker");
    public static final String FISHING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.fishing");

    public static final byte IS_ANKER_ID = 1;
    public static final byte SEARCHING_FOR_WATER_ID = 2;
    public static final byte SEARCHING_ANKER_ID = 3;
    public static final byte FISHING_ID = 4;

    private final int horizontalSearchRange;
    private final int verticalSearchRange;

    @Nullable
    private BlockPos waterPos;

    public ClaySoldierFishGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess, SearchRange searchRange) {
        super(soldier, workAccess, List.of(
                BREAK_LANG,
                IS_ANKER_LANG,
                SEARCHING_FOR_WATER_LANG,
                SEARCHING_ANKER_LANG,
                FISHING_LANG
        ));
        this.verticalSearchRange = searchRange.verticalRange();
        this.horizontalSearchRange = searchRange.horizontalRange();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(FISH_LANG);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        boolean fishingAnkerState = false;

        if (hasFishingRod(soldier)) {
            if (waterPos != null && isNearbyWater(waterPos)) {
                fishingAnkerState = true;
                setStatus(IS_ANKER_ID);
                soldier.lookAt(EntityAnchorArgument.Anchor.FEET, new Vec3(waterPos));
            } else {
                if (waterPos != null) {
                    if (isWaterAt(waterPos)) {
                        moveToPos(findNearestSolidBlock(waterPos), 3);
                    } else {
                        waterPos = null;
                    }
                } else {
                    waterPos = findNearbyWater(true);
                }
                setStatus(SEARCHING_FOR_WATER_ID);
            }
        } else {
            var anker = findClaySoldierAnker();
            setStatus(SEARCHING_ANKER_ID);
            if (anker != null) {
                if (soldier.distanceTo(anker) > 2) {
                    soldier.getNavigation().moveTo(anker, 1.3f);
                } else if (!anker.isFishing()) {
                    waterPos = findNearbyWater(false);
                    if (waterPos != null && isWaterAt(waterPos)) {
                        setStatus(FISHING_ID);
                        anker.setFishingIfPossible(waterPos);
                    }
                }
            }
        }


        soldier.setFishingAnker(fishingAnkerState);
    }

    @Nullable
    private ProgrammableClaySoldierEntity findClaySoldierAnker() {
        return getServerLevel(soldier).getNearestEntity(
                ProgrammableClaySoldierEntity.class,
                TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting().selector(this::isValidAnker),
                soldier,
                soldier.getX(), soldier.getY(), soldier.getZ(),
                soldier.getBoundingBox().inflate(horizontalSearchRange, verticalSearchRange, horizontalSearchRange)
        );
    }

    private boolean isValidAnker(LivingEntity entity, ServerLevel serverLevel) {
        if (entity instanceof ProgrammableClaySoldierEntity soldier) {
            return soldier.isFishingAnker() && !soldier.isFishing();
        }
        return false;
    }

    private boolean wantsToHoldFishingRod() {
        return true;
    }

    public static boolean hasFishingRod(ProgrammableClaySoldierEntity clayMob) {
        return clayMob.getCarriedStack().is(Items.FISHING_ROD);
    }

    private boolean isNearbyWater(@NotNull BlockPos waterPos) {
        if (soldier.isInWater()) {
            return false;
        }
        if (!isWaterAt(waterPos)) {
            return false;
        }
        if (!soldier.blockPosition().closerThan(waterPos, 10)) {
            return false;
        }
        if (!soldier.getNavigation().isDone()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean workRequiresItemCarrying(ItemStack stack) {
        if (wantsToHoldFishingRod()) {
            return stack.is(Items.FISHING_ROD);
        }
        return false;
    }

    @Override
    public boolean workRequiresItemPickUp(ItemStack stack) {
        if (wantsToHoldFishingRod()) {
            return stack.is(Items.FISHING_ROD);
        }
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isWaterAt(BlockPos pos) {
        return soldier.level().getBlockState(pos).is(Blocks.WATER) && soldier.level().getBlockState(pos.above()).isAir();
    }

    private @Nullable BlockPos findNearbyWater(boolean useCloset) {
        if (useCloset) {
            return findNearestValidPos(soldier.blockPosition(), 0, verticalSearchRange, horizontalSearchRange, this::isWaterAt);
        }
        final BlockPos[] first = {null};
        BlockPos random = findNearestValidPos(soldier.blockPosition(), 0, verticalSearchRange, horizontalSearchRange, p -> {
            if (!isWaterAt(p)) {
                return false;
            }
            if (first[0] == null) {
                first[0] = p;
            }
            return soldier.getRandom().nextFloat() < 0.3;
        });

        return random == null ? first[0] : random;

    }

    private @Nullable BlockPos findNearestSolidBlock(BlockPos waterPos) {
        return findNearestValidPos(waterPos, 0, verticalSearchRange, horizontalSearchRange, p -> {
            BlockState state = soldier.level().getBlockState(p);

            if (state.isFaceSturdy(soldier.level(), p, Direction.UP)) {

                BlockPos above = p.above();
                BlockState aboveState = soldier.level().getBlockState(above);

                if (!aboveState.isAir() && !aboveState.getFluidState().isEmpty()) {
                    return false;
                }
                return true;
            }
            return false;
        });
    }
}
