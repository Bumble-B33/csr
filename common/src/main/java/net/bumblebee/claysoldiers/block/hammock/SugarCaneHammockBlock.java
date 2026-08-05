package net.bumblebee.claysoldiers.block.hammock;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SugarCaneHammockBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final MapCodec<SugarCaneHammockBlock> CODEC = simpleCodec(SugarCaneHammockBlock::new);

    public SugarCaneHammockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (!state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState stateBelow = level.getBlockState(pos.below());
        if (stateBelow.is(Blocks.SUGAR_CANE)) {
            return true;
        } else {
            TriState soilDecision = ClaySoldiersCommon.COMMON_HOOKS.canSustainPlant(level, pos.below(), Direction.UP, state);
            if (soilDecision != TriState.DEFAULT) return soilDecision == TriState.TRUE;
            if (stateBelow.is(BlockTags.SUPPORTS_SUGAR_CANE)) {
                BlockPos below = pos.below();

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState blockState = level.getBlockState(below.relative(direction));
                    FluidState fluidState = level.getFluidState(below.relative(direction));
                    if (fluidState.is(FluidTags.SUPPORTS_SUGAR_CANE_ADJACENTLY) || blockState.is(BlockTags.SUPPORTS_SUGAR_CANE_ADJACENTLY) || ClaySoldiersCommon.COMMON_HOOKS.stateCanHydrate(state, level, pos, fluidState, below.relative(direction))) {
                        return true;
                    }

                }
            }

            return false;
        }
    }

    public static Optional<InteractionResult> onSugarCaneUse(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.SUGAR_CANE)) {
            return Optional.empty();
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        if (!itemInHand.is(Items.RABBIT_HIDE)) {
            return Optional.empty();
        }
        BlockState state = ModBlocks.SUGAR_CANE_HAMMOCK.get().defaultBlockState().trySetValue(FACING, player.getDirection().getOpposite());
        level.setBlock(pos, state, 3);
        return Optional.of(InteractionResult.SUCCESS);
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation rotation) {
        return pState.setValue(FACING, rotation.rotate(pState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror mirror) {
        return pState.rotate(mirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SugarCaneHammockBlockEntity(blockPos, blockState);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(Items.SUGAR_CANE);
    }
}
