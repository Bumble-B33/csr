package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class HamsterWheelBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<HamsterWheelBatteryProperty> BATTERY_PROPERTY = EnumProperty.create("battery", HamsterWheelBatteryProperty.class);
    public static final Supplier<Item> BATTERY_ITEM = ModItems.SMALL_BATTERY;
    public static final Supplier<Item> DOUBLE_BATTERY_ITEM = ModItems.LARGE_BATTERY;


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
        this.registerDefaultState(this.stateDefinition.any().setValue(BATTERY_PROPERTY, HamsterWheelBatteryProperty.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BATTERY_PROPERTY);
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

    private static void insertEnergyFromBattery(ItemStack stack, Level level, BlockPos pos) {
        int energy = ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(stack);
        if (energy > 0 && !level.isClientSide() && level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity hamsterWheelBlockEntity) {
            hamsterWheelBlockEntity.setStartingEnergy(energy);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(BATTERY_ITEM.get())) {
            if (state.getValue(BATTERY_PROPERTY) == HamsterWheelBatteryProperty.NONE) {
                level.setBlock(pos, state.setValue(BATTERY_PROPERTY, HamsterWheelBatteryProperty.SINGLE), 3);
                stack.consume(1, player);
                insertEnergyFromBattery(stack, level, pos);
                return InteractionResult.SUCCESS;
            }

            if (state.getValue(BATTERY_PROPERTY) == HamsterWheelBatteryProperty.SINGLE) {
                level.setBlock(pos, state.setValue(BATTERY_PROPERTY, HamsterWheelBatteryProperty.DUAL), 3);
                stack.consume(1, player);
                insertEnergyFromBattery(stack, level, pos);
                return InteractionResult.SUCCESS;
            }
        }
        if (stack.is(DOUBLE_BATTERY_ITEM.get())) {
            if (state.getValue(BATTERY_PROPERTY) == HamsterWheelBatteryProperty.NONE) {
                level.setBlock(pos, state.setValue(BATTERY_PROPERTY, HamsterWheelBatteryProperty.DUAL), 3);
                stack.consume(1, player);
                insertEnergyFromBattery(stack, level, pos);
                return InteractionResult.SUCCESS;
            }
        }

        if (stack.is(ModTags.Items.WRENCH)) {
            level.setBlock(pos, rotate(state, Rotation.CLOCKWISE_90), 3);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity hamsterWheel) {
            hamsterWheel.spawnSoldier(7);
            hamsterWheel.setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static boolean hasPowerConnection(BlockState state) {
        return state.getValue(BATTERY_PROPERTY) != HamsterWheelBatteryProperty.NONE;
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
        return level.isClientSide()
                ? createTickerHelper(blockEntityType, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), (_, _, _, hamsterWheelBlockEntity) -> hamsterWheelBlockEntity.clientTick())
                : createTickerHelper(blockEntityType, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), ((_, _, _, hamsterWheelBlockEntity) -> hamsterWheelBlockEntity.serverTick()));
    }
}
