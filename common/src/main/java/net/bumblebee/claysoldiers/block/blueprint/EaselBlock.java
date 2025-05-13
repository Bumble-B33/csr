package net.bumblebee.claysoldiers.block.blueprint;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.item.TestItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EaselBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);
    private static final MapCodec<EaselBlock> CODEC = simpleCodec(EaselBlock::new);

    public EaselBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(enabledFeatures);
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
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        var easeBlockEntity = ((EaselBlockEntity) Objects.requireNonNull(pLevel.getBlockEntity(pPos)));
        if ((ModItems.TEST_ITEM.is(pStack))) {
            TestItem.log((EaselBlockEntity) pLevel.getBlockEntity(pPos), ((EaselBlockEntity) pLevel.getBlockEntity(pPos)).getInfoState());
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        if (pStack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlueprintData bluePrintData = pLevel.registryAccess().registryOrThrow(ModRegistries.BLUEPRINTS).get(pStack.get(ModDataComponents.BLUEPRINT_DATA.get()));

        if (bluePrintData != null && bluePrintData.isValid()) {
            easeBlockEntity.setBlueprintData(bluePrintData);
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        if (easeBlockEntity.hasBlueprintData()) {
            if (!pLevel.isClientSide()) {
                var placeResult = easeBlockEntity.tryPlacingSoldier(pStack);
                if (placeResult.isSuccess()) {
                    if (!pPlayer.isCreative()) {
                        pStack.shrink(1);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
        }
        return ItemInteractionResult.FAIL;
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

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
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


}
