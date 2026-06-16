package net.bumblebee.claysoldiers.block.blueprint;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequestResult;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EaselBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);
    private static final MapCodec<EaselBlock> CODEC = simpleCodec(EaselBlock::new);

    public EaselBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends EaselBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EaselBlockEntity(pPos, pState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand hand, BlockHitResult pHitResult) {
        var easeBlockEntity = ((EaselBlockEntity) Objects.requireNonNull(pLevel.getBlockEntity(pPos)));
        if (ModItems.TEST_ITEM.is(stack)) {
            TestItem.log((EaselBlockEntity) pLevel.getBlockEntity(pPos), ((EaselBlockEntity) pLevel.getBlockEntity(pPos)).getInfoState());
            return InteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        BlueprintData bluePrintData = BlueprintItem.getData(stack, player.registryAccess());

        if (bluePrintData != null && bluePrintData.isValid()) {
            easeBlockEntity.setBlueprintData(bluePrintData);
            return InteractionResult.SUCCESS;
        }

        if (easeBlockEntity.hasBlueprintData()) {
            if (!pLevel.isClientSide()) {
                BlueprintRequestResult placeResult = easeBlockEntity.tryPlacingSoldier(stack, player);
                if (placeResult.isSuccess()) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        if (placeResult.hasRemainder()) {
                            ItemStack remainder = placeResult.getRemainder();
                            if (stack.isEmpty()) {
                                player.setItemInHand(hand, remainder);
                                return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(remainder);
                            } else {
                                player.addItem(remainder);
                            }
                        }

                    }
                }
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        var easeBlockEntity = ((EaselBlockEntity) pLevel.getBlockEntity(pPos));
        if (!easeBlockEntity.hasBlueprintData()) {
            return InteractionResult.FAIL;
        }

        if (pPlayer.isCrouching()) {
            if (!pLevel.isClientSide()) {
                pPlayer.addItem(easeBlockEntity.getBlueprintItem());
            }
            easeBlockEntity.clearBlueprintData();
        } else if (!easeBlockEntity.cycleMirror()) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.SUCCESS_SERVER;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
