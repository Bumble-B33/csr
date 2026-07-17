package net.bumblebee.claysoldiers.block.chipassembler;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ChipAssemblerBlock extends BaseEntityBlock {
    private static final MapCodec<ChipAssemblerBlock> CODEC = simpleCodec(ChipAssemblerBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape INTER_NORTH = makeShape(OctahedralGroup.IDENTITY);
    private static final VoxelShape INTER_EAST = makeShape(OctahedralGroup.BLOCK_ROT_Y_90);
    private static final VoxelShape INTER_SOUTH = makeShape(OctahedralGroup.BLOCK_ROT_Y_180);
    private static final VoxelShape INTER_WEST = makeShape(OctahedralGroup.BLOCK_ROT_Y_270);
    private static final VoxelShape SHAPE = Shapes.create(0, 0, 0, 1, 0.5, 1);
    public static final String CONTAINER_TITLE = ClaySoldiersCommon.MOD_ID +  ".container.chip_assembler";


    public ChipAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> INTER_NORTH;
            case EAST -> INTER_EAST;
            case WEST -> INTER_WEST;
            case SOUTH -> INTER_SOUTH;
            default -> {
                ClaySoldiersCommon.ERROR_HANDLER.warn("Illegal Direction for Shape");
                yield INTER_NORTH;
            }
        };
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.getItemInHand(hand).is(ModTags.Items.WRENCH)) {
            level.setBlock(pos, rotate(state, Rotation.CLOCKWISE_90), 3);
            return InteractionResult.SUCCESS;
        }

        if (hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof ChipAssemblerBlockEntity chipAssembler) {
            if (level instanceof ServerLevel serverLevel) {
                Optional<ItemStack> res;
                res = chipAssembler.insert(player.getItemInHand(hand), hitResult);
                res.ifPresent(s -> player.setItemInHand(hand, s));
                return res.isPresent() ? InteractionResult.SUCCESS_SERVER : InteractionResult.TRY_WITH_EMPTY_HAND;
            }

            return InteractionResult.CONSUME;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChipAssemblerBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide()
                ? createTickerHelper(blockEntityType, ModBlockEntities.CHIP_ASSEMBLER_BLOCK_ENTITY.get(), ((_, _, _, chipAssemblerBlockEntity) -> chipAssemblerBlockEntity.clientTick()))
                : createTickerHelper(blockEntityType, ModBlockEntities.CHIP_ASSEMBLER_BLOCK_ENTITY.get(), ((_, _, _, chipAssemblerBlockEntity) -> chipAssemblerBlockEntity.serverTick()));
    }

    private static VoxelShape makeShape(OctahedralGroup rotation) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.21875, 0.25, 0.6875, 0.28125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.125, 0.8125, 0.25, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.875, 0.75, 0.25, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.9375, 0.6875, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.25, 0.1875, 1, 0.3125, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.25, 0.5, 1, 0.3125, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.25, 0.5, 0.25, 0.3125, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.25, 0.1875, 0.25, 0.3125, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.1875, 0.25, 0.875, 0.3125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.1875, 0.5625, 0.875, 0.3125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.1875, 0.5625, 0.1875, 0.3125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.1875, 0.25, 0.1875, 0.3125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.25, 0.65625, 0.59375, 0.3125, 0.84375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 1, 0.75, 0.75, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.3125, 0.936875, 0.6875, 0.6875, 0.999375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.53125, 0.25, 0.936875, 0.59375, 0.3125, 0.999375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.25, 0.936875, 0.46875, 0.3125, 0.999375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.046875, 0.75, 0.25, 0.109375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.109375, 0.0625, 0.6875, 0.171875, 0.125), BooleanOp.OR);

        return Shapes.rotate(shape, rotation).optimize();
    }
}
