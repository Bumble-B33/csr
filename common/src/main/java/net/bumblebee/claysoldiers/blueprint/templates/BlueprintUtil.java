package net.bumblebee.claysoldiers.blueprint.templates;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BlueprintUtil {
    public static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private static final FileToIdConverter RESOURCE_LISTER = new FileToIdConverter("structure", ".nbt");

    public static List<ItemStack> itemMapToList(Map<Item, Integer> itemMap) {
        List<ItemStack> itemsSorted = new ArrayList<>(itemMap.size());
        itemMap.forEach((item, count) -> addItemStackToList(itemsSorted, item, count));
        return itemsSorted;
    }

    public static void addItemStackToList(List<ItemStack> itemsCounted, Item item, int count) {
        int maxStackSize = item.getDefaultMaxStackSize();
        if (count <= maxStackSize) {
            itemsCounted.add(new ItemStack(item, count));
        } else {
            itemsCounted.add(new ItemStack(item, maxStackSize));
            addItemStackToList(itemsCounted, item, count - maxStackSize);
        }
    }

    public static Vec3i getSizeFromTag(CompoundTag tag) {
        if (!tag.contains(StructureTemplate.SIZE_TAG)) {
            return Vec3i.ZERO;
        }
        ListTag sizeVec = tag.getList(StructureTemplate.SIZE_TAG, Tag.TAG_INT);
        return new Vec3i(sizeVec.getInt(0), sizeVec.getInt(1), sizeVec.getInt(2));
    }

    public static <T> Map<Item, Integer> getNeededItemsFromInfo(List<T> blockInfoList, Function<T, BlockState> stateGetter) {
        Map<Item, Integer> map = new HashMap<>();
        blockInfoList.stream().map(stateGetter)
                .map(state -> state.getBlock().asItem()).filter(item -> item != Items.AIR).forEach(item -> {
                    Integer count = map.get(item);
                    if (count == null) {
                        map.put(item, 1);
                    } else {
                        map.put(item, ++count);
                    }
                });
        return map;
    }

    public static Function<ResourceLocation, Optional<ImmutableTemplate>> createBlueprintLoader(ResourceManager resourceManager, HolderLookup<Block> blockLookup, Collection<Holder<Block>> blackListedBlocks) {
        return new BlueprintLoader(resourceManager, blockLookup, blackListedBlocks)::loadFromResource;
    }

    private record BlueprintLoader(ResourceManager resourceManager, HolderLookup<Block> blockLookup,
                                   Collection<Holder<Block>> blackListedBlocks) {

        public Optional<ImmutableTemplate> loadFromResource(ResourceLocation location) {
            ResourceLocation resourcelocation = RESOURCE_LISTER.idToFile(location);
            return this.load(() -> this.resourceManager.open(resourcelocation), error -> LOGGER.error("CSR: Couldn't load structure {}", location, error));
        }

        private Optional<ImmutableTemplate> load(InputStreamOpener pInputStream, Consumer<Throwable> pOnError) {
            try {
                Optional<ImmutableTemplate> optional;
                try (
                        InputStream inputstream = pInputStream.open();
                        InputStream inputstream1 = new FastBufferedInputStream(inputstream);
                ) {
                    optional = Optional.of(this.readStructure(inputstream1));
                }

                return optional;
            } catch (FileNotFoundException filenotfoundexception) {
                return Optional.empty();
            } catch (Throwable throwable1) {
                pOnError.accept(throwable1);
                return Optional.empty();
            }
        }

        private ImmutableTemplate readStructure(InputStream pStream) throws IOException {
            return ImmutableTemplate.create(blockLookup, NbtIo.readCompressed(pStream, NbtAccounter.unlimitedHeap()), blackListedBlocks);
        }
    }

    @FunctionalInterface
    private interface InputStreamOpener {
        InputStream open() throws IOException;
    }

    public static class SimplePalette implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper<>(16);
        private int lastId;

        public int idFor(BlockState pState) {
            int i = this.ids.getId(pState);
            if (i == -1) {
                i = this.lastId++;
                this.ids.addMapping(pState, i);
            }

            return i;
        }

        @Nullable
        public BlockState stateFor(int pId) {
            BlockState blockstate = this.ids.byId(pId);
            return blockstate == null ? DEFAULT_BLOCK_STATE : blockstate;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState pState, int pId) {
            this.ids.addMapping(pState, pId);
        }
    }
}
