package net.bumblebee.claysoldiers.blueprint;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintBlockInfoList;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintItemCountMap;
import net.bumblebee.claysoldiers.blueprint.templates.ImmutableTemplate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
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
    public static final BlueprintStateFilter FILTER = BlueprintStateFilter.createFilter();
    public static final MapCodec<Vec3i> SIZE_MAP_CODEC = Vec3i.CODEC.fieldOf(StructureTemplate.SIZE_TAG);


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
        return tag.read(SIZE_MAP_CODEC).orElse(Vec3i.ZERO);
    }


    public static BlueprintItemCountMap.Immutable getNeededItemsFromInfo(List<StructureTemplate.StructureBlockInfo> blockInfoList) {
        return getNeededItemsFromInfo(blockInfoList, s -> itemFromBlockState(s.state()));
    }

    public static BlueprintItemCountMap.Immutable getNeededItemsFromInfo(BlueprintBlockInfoList blockInfoList) {
        return getNeededItemsFromInfo(blockInfoList.getList(), s -> itemFromBlockState(s.getState()));
    }

    private static <T> BlueprintItemCountMap.Immutable getNeededItemsFromInfo(List<T> list, Function<T, Item> asItem) {
        Map<Item, Integer> map = new HashMap<>();
        list.stream().map(asItem)
                .filter(item -> item != Items.AIR).forEach(item -> {
                    Integer count = map.get(item);
                    if (count == null) {
                        map.put(item, 1);
                    } else {
                        map.put(item, ++count);
                    }
                });

        return BlueprintItemCountMap.immutable(map);
    }

    public static Function<Identifier, Optional<ImmutableTemplate>> createBlueprintLoader(ResourceManager resourceManager, HolderLookup.Provider registries) {
        return new BlueprintLoader(resourceManager, registries)::loadFromResource;
    }

    private record BlueprintLoader(ResourceManager resourceManager, HolderLookup.Provider registries) {

        public Optional<ImmutableTemplate> loadFromResource(Identifier location) {
            Identifier Identifier = RESOURCE_LISTER.idToFile(location);
            return this.load(() -> this.resourceManager.open(Identifier), error -> LOGGER.error("CSR: Couldn't load structure {}", location, error));
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
            return ImmutableTemplate.create(registries, NbtIo.readCompressed(pStream, NbtAccounter.unlimitedHeap()));
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


    @NotNull
    public static Item itemFromBlockState(BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().getType().getBucket();
        }

        return state.getBlock().asItem();
    }

    public static boolean isItemForState(ItemStack item, BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().getType().getBucket() == item.getItem();
        }

        return state.getBlock().asItem() == item.getItem();
    }

    @NotNull
    public static ItemStack getPlaceRemainder(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem) {
            return new ItemStack(Items.BUCKET);
        }
        return ItemStack.EMPTY;
    }
}
