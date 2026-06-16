package net.bumblebee.claysoldiers.blueprint.plan;

import com.mojang.datafixers.util.Either;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequestResult;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.bumblebee.claysoldiers.blueprint.BlueprintUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ServerBlueprintPlan extends BlueprintPlan {
    private final BlueprintBlockInfoList blockInfoList;

    public ServerBlueprintPlan(Vec3i size, BlueprintBlockInfoList blockInfoList, HolderGetter.Provider biomes) {
        super(BlueprintUtil.getNeededItemsFromInfo(blockInfoList), size);
        this.blockInfoList = blockInfoList;
    }

    @Override
    public BlueprintRequestResult tryPlacing(Level level, ItemStack itemStack, BlockPos base, BlueprintTemplateSettings settings) {
        var res = tryPlacingAll(itemStack, level, base, settings);
        if (res.isSuccess()) {
            if (!hasItemAndShrink(itemStack.getItem())) {
                ClaySoldiersCommon.ERROR_HANDLER.warn("Successfully placed a Block which was not needed");
            }
        }

        return res;
    }

    private BlueprintRequestResult tryPlacingAll(ItemStack stack, Level level, BlockPos base, BlueprintTemplateSettings settings) {
        var iterator = blockInfoList.iterator();
        while (iterator.hasNext()) {
            var blockInfo = iterator.next();
            if (!blockInfo.isFor(stack)) {
                continue;
            }
            var updatedBlockInfo = processBlockInfos(settings, blockInfo);

            if (placeBlock(stack, level, updatedBlockInfo.pos().offset(base), updatedBlockInfo.state().mirror(settings.mirror()).rotate(settings.rotation()))) {
                iterator.remove();
                blockInfo.markDone();
                return BlueprintRequestResult.success(BlueprintUtil.getPlaceRemainder(stack));
            }
        }
        return BlueprintRequestResult.fail();
    }

    private boolean placeBlock(ItemStack stack, Level level, BlockPos pos, BlockState state) {
        if (!canPlaceBlock(level, pos)) {
            return false;
        }
        if (level.setBlock(pos, state, 3)) {
            state.getBlock().setPlacedBy(level, pos, state, null, stack);
            return true;
        }
        return false;
    }

    private boolean canPlaceBlock(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    private static StructureTemplate.StructureBlockInfo processBlockInfos(BlueprintTemplateSettings pSettings, BlueprintBlockInfo pBlockInfo) {
        return new StructureTemplate.StructureBlockInfo(
                adjustForStructureSettings(pSettings, pBlockInfo.getPos()),
                pBlockInfo.getState(),
                pBlockInfo.getNbt() != null ? pBlockInfo.getNbt().copy() : null
        );
    }

    @Override
    public String toString() {
        return "ServerBlueprintTemplate{%s Blocks(%s) Items(%s): %s%s}".formatted(
                getSize(),
                blockInfoList.size(),
                getNumberOfItems(),
                getNeededItems(),
                hasStarted ? " started" : ""
        );
    }

    @Override
    public Builder asBuilder(boolean client) {
        if (client) {
            return new Builder(hasStarted, Either.left(itemCountMap), getSize(), true);
        }
        return new Builder(hasStarted, Either.right(blockInfoList), getSize(), false);
    }

    /**
     * Returns the best suitable request for this template
     *
     * @param base the base position from which this structure is build
     */
    public @Nullable BlueprintRequest getRequest(ServerLevel level, BlockPos base, BlueprintTemplateSettings settings, Predicate<BlockPos> canReach) {
        Supplier<BlueprintRequest> fallbackRequest = null;

        for (BlueprintBlockInfo blockInfo : blockInfoList) {
            var infoAbsolutePos = base.offset(adjustForStructureSettings(settings, blockInfo.getPos()));
            if (canReach.test(infoAbsolutePos) && canPlaceBlock(level, infoAbsolutePos)) {

                if (blockInfo.hasAvailableRequest(level.getGameTime())) {
                    return blockInfo.getRequest(level.getGameTime(), infoAbsolutePos, false);
                }
                if (fallbackRequest == null) {
                    fallbackRequest = () -> blockInfo.getRequest(level.getGameTime(), infoAbsolutePos, true);
                }
            }
        }
        return fallbackRequest != null ? fallbackRequest.get() : null;
    }

    @Override
    public boolean isFinished() {
        return blockInfoList.isEmpty();
    }

    private static BlockPos adjustForStructureSettings(BlueprintTemplateSettings settings, BlockPos pos) {
        return StructureTemplate.calculateRelativePosition(settings.convertTo(), pos.offset(BlockPos.ZERO));
    }
}
