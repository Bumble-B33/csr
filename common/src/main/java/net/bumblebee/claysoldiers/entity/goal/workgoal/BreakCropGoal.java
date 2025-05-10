package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BreakCropGoal extends MoveToBlockGoal implements IWorkGoal {
    public static final String BREAK_CROPS_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "breaking_crops");
    public static final String CROP_BREAK_DISALLOWED = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "breaking_crops.disallowed");
    private int ticksSinceReachedGoal;
    private static final int WAIT_AFTER_BLOCK_FOUND = 20;
    private static final byte CANT_BREAK_BLOCK_STATUS = 1;
    private byte status = 0;
    private final Supplier<WorkSelectorGoal> workSelector;

    public BreakCropGoal(PathfinderMob pMob, double pSpeedModifier, int pSearchRange, Supplier<WorkSelectorGoal> workSelector) {
        super(pMob, pSpeedModifier, pSearchRange, 2);
        this.workSelector = workSelector;
    }

    @Override
    public boolean canUse() {
        if (!ClaySoldiersCommon.COMMON_HOOKS.canEntityGrief(this.mob.level(), this.mob)) {
            if (status != CANT_BREAK_BLOCK_STATUS) {
                status = CANT_BREAK_BLOCK_STATUS;
                workSelector.get().onWorkStatusChange();
            }
            return false;
        }
        if (status != 0) {
            status = 0;
            workSelector.get().onWorkStatusChange();
        }

        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else if (this.findNearestBlock()) {
            this.nextStartTick = reducedTickDelay(WAIT_AFTER_BLOCK_FOUND);
            return true;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            return false;
        }
    }

    @Override
    public double acceptedDistance() {
        return 2f;
    }

    @Override
    protected int nextStartTick(PathfinderMob pCreature) {
        return reducedTickDelay(40 + pCreature.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.fallDistance = 1.0F;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.mob.level();
        BlockPos mobPos = this.mob.blockPosition();
        BlockPos cropPos = this.getPosWithBlock(mobPos, level);

        RandomSource randomsource = this.mob.getRandom();

        if (this.isReachedTarget() && cropPos != null) {
            BlockState toDestroy = level.getBlockState(cropPos);
            if (this.ticksSinceReachedGoal > 0) {

                Vec3 mobMovement = this.mob.getDeltaMovement();
                this.mob.setDeltaMovement(mobMovement.x, 0.3, mobMovement.z);
                if (!level.isClientSide) {
                    double particleOffset = 0.08;
                    ((ServerLevel) level)
                            .sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, toDestroy),
                                    (double) cropPos.getX() + 0.5,
                                    (double) cropPos.getY() + 0.7,
                                    (double) cropPos.getZ() + 0.5,
                                    1,
                                    ((double) randomsource.nextFloat() - 0.5) * particleOffset,
                                    ((double) randomsource.nextFloat() - 0.5) * particleOffset,
                                    ((double) randomsource.nextFloat() - 0.5) * particleOffset,
                                    0.15F
                            );
                }
            }

            if (this.ticksSinceReachedGoal % 2 == 0) {
                Vec3 mobMovement = this.mob.getDeltaMovement();
                this.mob.setDeltaMovement(mobMovement.x, -0.3, mobMovement.z);
                if (this.ticksSinceReachedGoal % 6 == 0) {
                    this.playDestroyProgressSound(level, this.blockPos);
                }
            }

            if (this.ticksSinceReachedGoal > 20) {
                breakCrop(level, cropPos);
                if (!level.isClientSide) {
                    this.playBreakSound(level, cropPos);
                }
            }

            this.ticksSinceReachedGoal++;
        }
    }

    private void breakCrop(Level level, BlockPos pPos) {
        level.destroyBlock(pPos, true, mob);
    }

    public void playDestroyProgressSound(LevelAccessor pLevel, BlockPos pPos) {
    }

    public void playBreakSound(Level pLevel, BlockPos pPos) {
    }

    @Nullable
    private BlockPos getPosWithBlock(BlockPos center, BlockGetter pLevel) {
        if (blockPredicate(pLevel.getBlockState(center))) {
            return center;
        } else {
            BlockPos[] around = new BlockPos[]{
                    center.below(), center.west(), center.east(), center.north(), center.south(), center.above()
            };
            for (BlockPos offsetPos : around) {
                if (blockPredicate(pLevel.getBlockState(offsetPos))) {
                    return offsetPos;
                }
            }
            BlockPos[] aroundAbove = new BlockPos[]{
                    center.west().above(), center.east().above(), center.north().above(), center.south().above()
            };
            for (BlockPos offsetPos : aroundAbove) {
                if (blockPredicate(pLevel.getBlockState(offsetPos))) {
                    return offsetPos;
                }
            }
            return null;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        ChunkAccess chunkaccess = pLevel.getChunk(
                SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ChunkStatus.FULL, false
        );
        return chunkaccess != null && blockPredicate(chunkaccess.getBlockState(pPos));
    }

    private static boolean blockPredicate(BlockState state) {
        return state.getBlock() instanceof CropBlock block && block.isMaxAge(state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(BREAK_CROPS_LANG);
    }

    @Override
    public byte getStatus() {
        return status;
    }

    @Override
    public Component decodeStatus(byte id) {
        if (id == CANT_BREAK_BLOCK_STATUS) {
            return Component.translatable(CROP_BREAK_DISALLOWED);
        }
        return IWorkGoal.super.decodeStatus(id);
    }
}
