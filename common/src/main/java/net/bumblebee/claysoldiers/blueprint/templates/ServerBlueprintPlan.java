package net.bumblebee.claysoldiers.blueprint.templates;

import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerBlueprintPlan extends BlueprintPlan {
    private final List<BlueprintBlockInfo> blockInfoList;

    public ServerBlueprintPlan(Vec3i size, List<StructureTemplate.StructureBlockInfo> blockInfoList) {
        super(BlueprintUtil.getNeededItemsFromInfo(blockInfoList, StructureTemplate.StructureBlockInfo::state), size);
        this.blockInfoList = fromBlockInfoList(blockInfoList);
    }

    public static ServerBlueprintPlan load(CompoundTag tag, HolderLookup.Provider registries) {
        ServerBlueprintPlan serverBlueprintPlan = new ServerBlueprintPlan(BlueprintUtil.getSizeFromTag(tag), ImmutableTemplate.loadPallet(registries.lookupOrThrow(Registries.BLOCK), tag.getList("palette", 10), tag.getList(StructureTemplate.BLOCKS_TAG, 10), List.of()));
        serverBlueprintPlan.loadHasStarted(tag);
        return serverBlueprintPlan;
    }

    public CompoundTag save(CompoundTag pTag) {
        if (this.blockInfoList.isEmpty()) {
            pTag.put("blocks", new ListTag());
            pTag.put("palette", new ListTag());
        } else {
            BlueprintUtil.SimplePalette simplePalette = new BlueprintUtil.SimplePalette();
            ListTag blockInfoAsPallet = new ListTag();

            blockInfoList.forEach(blockInfo -> {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.put(
                        "pos",
                        this.newIntegerList(
                                blockInfo.getPos().getX(),
                                blockInfo.getPos().getY(),
                                blockInfo.getPos().getZ()
                        )
                );
                int stateId = simplePalette.idFor(blockInfo.getState());
                compoundtag.putInt("state", stateId);
                if (blockInfo.getNbt() != null) {
                    compoundtag.put("nbt", blockInfo.getNbt());
                }

                blockInfoAsPallet.add(compoundtag);
            });

            pTag.put("blocks", blockInfoAsPallet);
            ListTag palletTag = new ListTag();

            for (BlockState blockstate : simplePalette) {
                palletTag.add(NbtUtils.writeBlockState(blockstate));
            }

            pTag.put("palette", palletTag);
        }

        saveSize(pTag);
        saveHasStarted(pTag);
        return pTag;
    }

    @Override
    public PlaceResult tryPlacing(Level level, ItemStack itemStack, BlockPos base, BlueprintTemplateSettings settings) {
        var res = tryPlacingAll(((BlockItem) itemStack.getItem()).getBlock(), itemStack, level, base, settings);
        if (res == PlaceResult.SUCCESS) {
            if (!hasItemAndShrink(itemStack.getItem())) {
                throw new IllegalStateException("Successfully placed a Block which was not needed");
            }
        }

        return res;
    }

    private PlaceResult tryPlacingAll(Block block, ItemStack stack, Level level, BlockPos base, BlueprintTemplateSettings settings) {
        var iterator = blockInfoList.iterator();
        while (iterator.hasNext()) {
            var blockInfo = iterator.next();
            if (!blockInfo.isFor(block)) {
                continue;
            }
            var updatedBlockInfo = processBlockInfos(settings, blockInfo);

            if (placeBlock(stack, level, updatedBlockInfo.pos().offset(base), updatedBlockInfo.state().mirror(settings.mirror()).rotate(settings.rotation()))) {
                iterator.remove();
                blockInfo.markDone();
                return PlaceResult.SUCCESS;
            }
        }
        return PlaceResult.CANT_PLACE;
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
        return "ServerBlueprintTemplate{%s Blocks(%s) Items(%s): %s}".formatted(
                getSize(),
                blockInfoList.size(),
                getNumberOfItems(),
                getNeededItems());
    }

    /**
     * Returns the best suitable request for this template
     * @param base the base position from which this structure is build
     */
    public @Nullable BlueprintRequest getRequest(ServerLevel level, BlockPos base, BlueprintTemplateSettings settings, Predicate<BlockPos> canReach) {
        Supplier<BlueprintRequest> fallbackRequest = null;

        for (BlueprintBlockInfo blockInfo : blockInfoList) {
            var infoAbsolutePos = base.offset(adjustForStructureSettings(settings, blockInfo.getPos()));
            if (canReach.test(infoAbsolutePos) && canPlaceBlock(level, infoAbsolutePos)) {

                if (blockInfo.hasAvailableRequest(level.getGameTime())) {
                    return blockInfo.getRequest(level.getGameTime(), infoAbsolutePos);
                }
                if (fallbackRequest == null) {
                    fallbackRequest = () -> blockInfo.getRequest(level.getGameTime(), infoAbsolutePos);
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

    /**
     * Maps the given {@code List<StructureTemplate.StructureBlockInfo>} to a deep copied {@code List<BlueprintBlockInfo>}.
     * @param infoList the list to copy.
     * @return deep copy {@code List<StructureTemplate.StructureBlockInfo>}
     */
    private static List<BlueprintBlockInfo> fromBlockInfoList(List<StructureTemplate.StructureBlockInfo> infoList) {
        return infoList.stream().map(BlueprintBlockInfo::fromInfo).collect(Collectors.toCollection(LinkedList::new));
    }
}
