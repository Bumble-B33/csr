package net.bumblebee.claysoldiers.blueprint.templates;

import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class BlueprintBlockInfo {
    private static final int MIN_ATTEMPT_TIME = 24000;
    /**
     * The Relative position to the Structures Base.
     */
    private final BlockPos pos;
    private final BlockState state;
    @Nullable
    private final CompoundTag nbt;
    @Nullable
    private BlueprintRequest lastRequest;

    public BlueprintBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag nbt) {
        this.pos = pos;
        this.state = state;
        this.nbt = nbt;
    }

    public static BlueprintBlockInfo fromInfo(StructureTemplate.StructureBlockInfo info) {
        return new BlueprintBlockInfo(info.pos(), info.state(), info.nbt() != null ? info.nbt().copy() : null);
    }

    public boolean isFor(Block block) {
        return state.is(block);
    }

    /**
     * @return the relative position of this BlockInfo.
     */
    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public @Nullable CompoundTag getNbt() {
        return nbt;
    }

    /**
     * Returns whether this BlockInfo has an available {@code BlueprintRequest}.
     * A {@code BlueprintRequest} is available, when no previous {@code BlueprintRequest} was created
     * or the last {@code BlueprintRequest} got cancelled or was not fulfilled with in {@value #MIN_ATTEMPT_TIME} ticks.
     * @param gameTime the current game time.
     * @return whether this BlockInfo has an available {@code BlueprintRequest}
     */
    public boolean hasAvailableRequest(long gameTime) {
        return lastRequest == null || lastRequest.isCancelled() || lastRequest.getStart() + MIN_ATTEMPT_TIME < gameTime;
    }

    /**
     * Creates a new {@code BlueprintRequest} for this BlockInfo, if an old {@code BlueprintRequest} was already created it will be canceled.
     * @param gameTime the current game time.
     * @param destinationPos the position in the world where this request will be placed.
     * @return a new {@code BlueprintRequest}.
     */
    public BlueprintRequest getRequest(long gameTime, BlockPos destinationPos) {
        var req = new BlueprintRequest(state.getBlock().asItem(), destinationPos, gameTime);

        if (lastRequest != null) {
            lastRequest.cancel();
        }
        lastRequest = req;

        return req;
    }

    /**
     * Marks this BlockInfo as placed.
     */
    public void markDone() {
        if (lastRequest != null) {
            lastRequest.setFinished();
        }
    }

    @Override
    public String toString() {
        return "BlockInfo{%s, %s, [%s]}".formatted(pos, state, lastRequest);
    }
}
