package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HamsterWheelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BatteryProperty> BATTERY_PROPERTY = EnumProperty.create("battery", BatteryProperty.class);

    private static final VoxelShape SHAPE_WEST = Block.box(5, 0, 2, 15, 14, 14);
    private static final VoxelShape SHAPE_EAST = Block.box(1, 0, 2, 11, 14, 14);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2, 0, 1, 14, 14, 11);
    private static final VoxelShape SHAPE_NORTH = Block.box(2, 0, 5, 14, 14, 15);
    private static final VoxelShape SHAPE_POWERED_WEST = Block.box(5, 0, 2, 16, 14, 14);
    private static final VoxelShape SHAPE_POWERED_EAST = Block.box(0, 0, 2, 11, 14, 14);
    private static final VoxelShape SHAPE_POWERED_SOUTH = Block.box(2, 0, 0, 14, 14, 11);
    private static final VoxelShape SHAPE_POWERED_NORTH = Block.box(2, 0, 5, 14, 14, 16);

    private static final MapCodec<HamsterWheelBlock> CODEC = simpleCodec(HamsterWheelBlock::new);

    public HamsterWheelBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BATTERY_PROPERTY, BatteryProperty.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, BATTERY_PROPERTY);
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (hasPowerConnection(pState)) {
            return getVoxelShape(pState, SHAPE_POWERED_NORTH, SHAPE_POWERED_SOUTH, SHAPE_POWERED_EAST, SHAPE_POWERED_WEST);
        }
        return getVoxelShape(pState, SHAPE_NORTH, SHAPE_SOUTH, SHAPE_EAST, SHAPE_WEST);
    }

    private static VoxelShape getVoxelShape(BlockState pState, VoxelShape shapeNorth, VoxelShape shapeSouth, VoxelShape shapeEast, VoxelShape shapeWest) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> shapeNorth;
            case SOUTH -> shapeSouth;
            case EAST -> shapeEast;
            case WEST -> shapeWest;
            default -> throw new IllegalStateException("Direction should never be any other than N,E,S,W");
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pStack.is(Items.REDSTONE)) {
            if (pState.getValue(BATTERY_PROPERTY) == BatteryProperty.NONE) {
                pLevel.setBlock(pPos, pState.setValue(BATTERY_PROPERTY, BatteryProperty.SINGLE), 3);
                pStack.consume(1, pPlayer);
                return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
            }

            if (pState.getValue(BATTERY_PROPERTY) == BatteryProperty.SINGLE) {
                pLevel.setBlock(pPos, pState.setValue(BATTERY_PROPERTY, BatteryProperty.DUAL), 3);
                pStack.consume(1, pPlayer);
                return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
            }
        }
        if (pStack.is(ModTags.Items.WRENCH)) {
            pLevel.setBlock(pPos, rotate(pState, Rotation.CLOCKWISE_90), 3);
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        HamsterWheelBlockEntity hamsterWheelBlockEntity = (HamsterWheelBlockEntity) pLevel.getBlockEntity(pPos);
        if (hamsterWheelBlockEntity.hasSoldier()) {
            hamsterWheelBlockEntity.spawnSoldier(7);
            hamsterWheelBlockEntity.setChanged();
            return InteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        return InteractionResult.PASS;
    }

    public static boolean hasPowerConnection(BlockState state) {
        return state.getValue(BATTERY_PROPERTY) != BatteryProperty.NONE;
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            if (pLevel.getBlockEntity(pPos) instanceof HamsterWheelBlockEntity hamsterWheelBlockEntity) {
                hamsterWheelBlockEntity.spawnSoldier(0);
            }
            if (hasPowerConnection(pState)) {
                Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), Items.REDSTONE.getDefaultInstance());
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected MapCodec<? extends HamsterWheelBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HamsterWheelBlockEntity(pPos, pState);
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), ((level1, blockPos, blockState, hamsterWheelBlockEntity) -> hamsterWheelBlockEntity.serverTick()));
    }
}
