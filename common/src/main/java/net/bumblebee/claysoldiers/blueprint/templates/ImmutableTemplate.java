package net.bumblebee.claysoldiers.blueprint.templates;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ImmutableTemplate extends BaseImmutableTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<StructureTemplate.StructureBlockInfo> blockInfoList;

    protected ImmutableTemplate(List<StructureTemplate.StructureBlockInfo> blockInfoList, Map<Item, Integer> itemCountMap, Vec3i size, VoxelShape shape) {
        super(itemCountMap, size, shape);
        this.blockInfoList = blockInfoList;
    }

    @Override
    public String toString() {
        return "ImmutableTemplate{%s Blocks(%s): %s}".formatted(size, blockInfoList.size(), blockInfoList);
    }

    @Override
    public String toShortString() {
        return "ImmutableTemplate{%s Items(%s) Blocks(%s)}".formatted(size, totalNeededItems(), blockInfoList.size());
    }

    /**
     * Creates a new {@link ServerBlueprintPlan}. The template should not modify {@link #blockInfoList} in any way.
     */
    @Override
    public Optional<ServerBlueprintPlan> createServer() {
        return Optional.of(new ServerBlueprintPlan(size, blockInfoList));
    }

    @Override
    public Optional<BlueprintPlan> createClient() {
        return Optional.empty();
    }

    public static ImmutableTemplate create(HolderLookup<Block> pBlockGetter, CompoundTag pTag, Collection<Holder<Block>> blackListedBlocks) {
        ListTag blockListTag = pTag.getList(StructureTemplate.BLOCKS_TAG, 10);
        List<StructureTemplate.StructureBlockInfo> blockInfoList;
        if (pTag.contains("palettes", 9)) {
            ListTag palettesListTag = pTag.getList("palettes", 9);

            blockInfoList = loadPallet(pBlockGetter, palettesListTag.getList(0), blockListTag, blackListedBlocks);

            LOGGER.warn("Clay Soldiers: Structure containing more than one Pallet. Using the first one");
        } else {
            blockInfoList = loadPallet(pBlockGetter, pTag.getList("palette", 10), blockListTag, blackListedBlocks);
        }


        return new ImmutableTemplate(blockInfoList, BlueprintUtil.getNeededItemsFromInfo(blockInfoList, StructureTemplate.StructureBlockInfo::state), BlueprintUtil.getSizeFromTag(pTag), buildShape(blockInfoList));
    }


    public static List<StructureTemplate.StructureBlockInfo> loadPallet(HolderLookup<Block> pBlockGetter, ListTag pPaletteTag, ListTag pBlocksTag, Collection<Holder<Block>> blackListedBlocks) {
        BlueprintUtil.SimplePalette structuretemplate$simplepalette = new BlueprintUtil.SimplePalette();

        for (int i = 0; i < pPaletteTag.size(); i++) {
            structuretemplate$simplepalette.addMapping(NbtUtils.readBlockState(pBlockGetter, pPaletteTag.getCompound(i)), i);
        }

        List<StructureTemplate.StructureBlockInfo> normalBlocks = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> blockWithNbt = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> blockWithSpecialShape = Lists.newArrayList();

        for (int j = 0; j < pBlocksTag.size(); j++) {
            CompoundTag compoundtag = pBlocksTag.getCompound(j);
            ListTag listtag = compoundtag.getList("pos", 3);
            BlockPos blockpos = new BlockPos(listtag.getInt(0), listtag.getInt(1), listtag.getInt(2));
            BlockState blockState = structuretemplate$simplepalette.stateFor(compoundtag.getInt("state"));
            CompoundTag nbt = null;

            if (compoundtag.contains("nbt")) {
                nbt = compoundtag.getCompound("nbt");
                if (nbt.contains(JigsawBlockEntity.FINAL_STATE)) {
                    String finalState = nbt.getString(JigsawBlockEntity.FINAL_STATE);
                    try {
                        blockState = BlockStateParser.parseForBlock(pBlockGetter, finalState, true).blockState();
                    } catch (CommandSyntaxException e) {
                        LOGGER.error("Error while parsing BlockState {} in Blueprint, has an invalid final state of {}. ", blockState, finalState);
                    }
                    nbt.remove(JigsawBlockEntity.FINAL_STATE);
                    if (nbt.isEmpty()) {
                        nbt = null;
                    }
                }
            }

            if (blockState == null) {
                LOGGER.error("Error while parsing BlockState in Blueprint");
                continue;
            }

            StructureTemplate.StructureBlockInfo blockInfo = new StructureTemplate.StructureBlockInfo(
                    blockpos, blockState, nbt
            );
            addToLists(blockInfo, normalBlocks, blockWithNbt, blockWithSpecialShape, pBlockGetter, blackListedBlocks);
        }

        return buildInfoList(normalBlocks, blockWithNbt, blockWithSpecialShape);
    }



    private static void addToLists(StructureTemplate.StructureBlockInfo blockInfo, List<StructureTemplate.StructureBlockInfo> pNormalBlocks,
                            List<StructureTemplate.StructureBlockInfo> pBlocksWithNbt, List<StructureTemplate.StructureBlockInfo> pBlocksWithSpecialShape,
                                   HolderLookup<Block> lookup, Collection<Holder<Block>> blackListedBlocks
    ) {
        var updatedBlockInfo = updateBlockInfo(blockInfo, lookup);
        if (updatedBlockInfo == null) {
            return;
        }

        if (blackListedBlocks.stream().anyMatch(blockHolder -> updatedBlockInfo.state().is(blockHolder))) {
            return;
        }

        if (updatedBlockInfo.nbt() != null) {
            pBlocksWithNbt.add(updatedBlockInfo);
        } else if (!updatedBlockInfo.state().getBlock().hasDynamicShape() && updatedBlockInfo.state().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            pNormalBlocks.add(updatedBlockInfo);
        } else {
            pBlocksWithSpecialShape.add(updatedBlockInfo);
        }
    }


    @Nullable
    private static StructureTemplate.StructureBlockInfo updateBlockInfo(StructureTemplate.StructureBlockInfo info, HolderLookup<Block> pBlockGetter) {
        return filterIllegalStates(setFinalState(info, pBlockGetter));
    }

    private static StructureTemplate.StructureBlockInfo setFinalState(StructureTemplate.StructureBlockInfo info, HolderLookup<Block> pBlockGetter) {
        if (info.nbt() == null) {
            return info;
        }

        String finalState = info.nbt().getString("final_state");
        if (finalState.isEmpty()) {
            return info;
        }
        try {
            return new StructureTemplate.StructureBlockInfo(
                    info.pos(),
                    BlockStateParser.parseForBlock(pBlockGetter, finalState, true).blockState(),
                    info.nbt()
            );
        } catch (CommandSyntaxException commandsyntaxexception) {
            LOGGER.error("Error while parsing blockstate {} in for original block {}", finalState, info.state());
            return info;
        }
    }

    private static VoxelShape buildShape(List<StructureTemplate.StructureBlockInfo> list) {
        return list.stream().map(ImmutableTemplate::shapeFromBlockInfo).reduce(Shapes.empty(), ((voxelShape1, voxelShape2) -> Shapes.joinUnoptimized(voxelShape1, voxelShape2, BooleanOp.OR))).optimize();
    }

    private static VoxelShape shapeFromBlockInfo(StructureTemplate.StructureBlockInfo blockInfo) {
        var state = blockInfo.state();
        var shape = getShapeFromState(state, blockInfo.pos()).move(blockInfo.pos().getX(), blockInfo.pos().getY(), blockInfo.pos().getZ());
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            var shapeUpper = getShapeFromState(state, blockInfo.pos()).move(blockInfo.pos().getX(), blockInfo.pos().getY() + 1, blockInfo.pos().getZ());
            state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
            return Shapes.joinUnoptimized(shape, shapeUpper, BooleanOp.OR);
        }
        if (state.hasProperty(BlockStateProperties.BED_PART)) {
            state.setValue(BlockStateProperties.BED_PART, BedPart.HEAD);
            var posBedHead = blockInfo.pos().relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            var shapeHead = getShapeFromState(state, blockInfo.pos()).move(posBedHead.getX(), posBedHead.getY(), posBedHead.getZ());
            state.setValue(BlockStateProperties.BED_PART, BedPart.FOOT);
            return Shapes.joinUnoptimized(shape, shapeHead, BooleanOp.OR);
        }

        return shape;
    }

    private static VoxelShape getShapeFromState(BlockState state, BlockPos pos) {
        return state.getCollisionShape(EmptyBlockGetter.INSTANCE, pos, CollisionContext.empty()).singleEncompassing();
    }

    @Nullable
    private static StructureTemplate.StructureBlockInfo filterIllegalStates(StructureTemplate.StructureBlockInfo info) {
        var state = info.state();
        if (isIllegalSate(state, BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)) return null;
        if (isIllegalSate(state, BlockStateProperties.BED_PART, BedPart.HEAD)) return null;

        return info;
    }

    private static <T extends Comparable<T>> boolean isIllegalSate(BlockState state, Property<T> property, T when) {
        return when.equals(state.getOptionalValue(property).orElse(null));
    }

    private static List<StructureTemplate.StructureBlockInfo> buildInfoList(List<StructureTemplate.StructureBlockInfo> pNormalBlocks,
                                                                            List<StructureTemplate.StructureBlockInfo> pBlocksWithNbt, List<StructureTemplate.StructureBlockInfo> pBlocksWithSpecialShape
    ) {
        Comparator<StructureTemplate.StructureBlockInfo> comparator = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt(
                        p_74641_ -> p_74641_.pos().getY()
                )
                .thenComparingInt(p_74637_ -> p_74637_.pos().getX())
                .thenComparingInt(p_74572_ -> p_74572_.pos().getZ());
        pNormalBlocks.sort(comparator);
        pBlocksWithSpecialShape.sort(comparator);
        pBlocksWithNbt.sort(comparator);
        List<StructureTemplate.StructureBlockInfo> list = new ArrayList<>();
        list.addAll(pNormalBlocks);
        list.addAll(pBlocksWithSpecialShape);
        list.addAll(pBlocksWithNbt);
        list = list.stream().filter(blockInfo -> !blockInfo.state().isAir()).toList();
        return list;
    }
}
