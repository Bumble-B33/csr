package net.bumblebee.claysoldiers.block.chargingpad;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.BlockEntityWithEnergy;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.particles.ChargingParticleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import javax.swing.*;

public class SoldierChargingPadBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.column(14.0F, 0.0F, 1.0F);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final String CHARGING_TOOLTIP_LANG = "block.csr.charging_pad.tranfser_rate";

    public SoldierChargingPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return Block.simpleCodec(SoldierChargingPadBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (!(level.getBlockEntity(pos) instanceof SoldierChargingPadBlockEntity chargingPad)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }


        int inserted = 0;

        if (entity instanceof ProgrammableClaySoldierEntity soldier) {
            int toExtract = chargingPad.getEnergyForCharging(soldier.getEnergyForCharging());
            if (toExtract > 0) {
                inserted = soldier.insertEnergy(toExtract);
                if (inserted > 0) {
                    soldier.updateCarriedStack();
                }
            }
        } else if (entity instanceof ServerPlayer serverPlayer) {
            int toExtract = chargingPad.getEnergyForCharging(ClaySoldiersCommon.CONFIG.getCommonConfig().chargingPadRatePlayerInventory());
            if (toExtract > 0) {
                inserted = toExtract - chargePlayerInventory(serverPlayer, toExtract);
            }
        }

        if (inserted > 0) {
            sendChargingParticles(serverLevel, entity);
            chargingPad.removeEnergyForCharging(inserted);
        }

    }

    private static int chargePlayerInventory(ServerPlayer serverPlayer, int energyToInsert) {
        int energyRemaining = energyToInsert;
        for (ItemStack stack : serverPlayer.getInventory()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (energyRemaining <= 0) {
                break;
            }
            energyRemaining -= ClaySoldiersCommon.ENERGY_HELPER.insert(stack, energyRemaining);
        }

        return energyRemaining;
    }

    private static void sendChargingParticles(ServerLevel level, Entity entity) {
        if (level.getGameTime() % 7 != 0) {
            return;
        }
        AABB boundingBox = entity.getBoundingBox();
        Position pos = entity.position();

        float dia = (float) boundingBox.getXsize();

        level.sendParticles(new ChargingParticleOption(10, dia, (float) boundingBox.getYsize()),
                pos.x(), pos.y(), pos.z(),
                0,
                0, 0, 0,
                0
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.getItemInHand(hand).is(ModTags.Items.WRENCH)) {
            level.setBlock(pos, rotate(state, Rotation.CLOCKWISE_90), 3);
            return InteractionResult.SUCCESS;
        }
        if (itemStack.is(ModTags.Items.BATTERY)) {
            return BlockEntityWithEnergy.fillFromBattery(itemStack, level, pos);
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoldierChargingPadBlockEntity(pos, state);
    }
}
