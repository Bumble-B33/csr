package net.bumblebee.claysoldiers.entity.goal.workgoal.dig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StairPosMap {
    private static final StairPos BASE = new StairPos(2, 2, Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
    private static final StairPos[] STAIRS = {
            new StairPos(0, 1, Blocks.COBBLESTONE_STAIRS.defaultBlockState()),
            new StairPos(0, 2, Blocks.COBBLESTONE_STAIRS.defaultBlockState()),
            new StairPos(0, 3, Blocks.COBBLESTONE_STAIRS.defaultBlockState()),

            new StairPos(1, 4, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.COUNTERCLOCKWISE_90)),
            new StairPos(2, 4, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.COUNTERCLOCKWISE_90)),
            new StairPos(3, 4, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.COUNTERCLOCKWISE_90)),

            new StairPos(4, 3, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_180)),
            new StairPos(4, 2, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_180)),
            new StairPos(4, 1, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_180)),

            new StairPos(3, 0, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_90)),
            new StairPos(2, 0, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_90)),
            new StairPos(1, 0, Blocks.COBBLESTONE_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_90)),
    };
    private static final StairPos[] CORNERS = {
            new StairPos(0, 0, Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)), new StairPos(0, 4, Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)),
            new StairPos(4, 4, Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)), new StairPos(4, 0, Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)),
    };

    private final BlockPos poiPos;

    public StairPosMap(@NotNull BlockPos poiPos) {
        this.poiPos = poiPos;
    }

    /**
     * Place a Stair at this position if required.
     */
    public void placeStair(BlockPos pos, Level level) {
        getStairPos(pos).ifPresent(stairPos -> level.setBlock(pos, stairPos.state, 3));
    }

    /**
     * Returns weather this position requires a Stair.
     */
    public boolean requiresStair(BlockPos pos) {
        return getStairPos(pos).isPresent();
    }

    private StairPos getStairPosForHeight(int y) {
        return STAIRS[y % STAIRS.length];
    }

    private @Nullable StairPos getCornerPosForHeight(int y) {
        if (y % 3 != 0) {
            return null;
        }
        return CORNERS[(y / 3) % CORNERS.length];
    }


    private Optional<StairPos> getStairPos(BlockPos pos) {
        if (pos.equals(poiPos)) {
            return Optional.of(BASE);
        }

        int y = getYDiff(pos);
        var stairPos = getCorner(y);
        if (stairPos != null && stairPos.toBlockPos(poiPos, y).equals(pos)) {
            return Optional.of(stairPos);
        }
        stairPos = getStairPosForHeight(y);
        if (stairPos.toBlockPos(poiPos, y).equals(pos)) {
            return  Optional.of(stairPos);
        }
        return Optional.empty();
    }

    private int getYDiff(BlockPos pos) {
        return poiPos.getY() - pos.getY();
    }

    private StairPos getCorner(int y) {
        return getCornerPosForHeight(y);
    }

    private record StairPos(int x, int z, BlockState state) {
        public BlockPos toBlockPos(BlockPos poiPos, int y) {
            return poiPos.offset(-2 + x, -y, -2 + z);
        }
    }
}
