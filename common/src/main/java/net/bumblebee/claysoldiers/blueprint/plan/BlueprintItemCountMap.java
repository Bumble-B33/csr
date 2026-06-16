package net.bumblebee.claysoldiers.blueprint.plan;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.blueprint.BlueprintUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class BlueprintItemCountMap {
    public static final Codec<BlueprintItemCountMap.Mutable> CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT).xmap(
            BlueprintItemCountMap::mutable, s -> s.itemCountMap
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Item, Integer>> MAP_STREAM_CODEC = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.ITEM), ByteBufCodecs.VAR_INT);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintItemCountMap.Immutable> STREAM_CODEC = MAP_STREAM_CODEC.map(BlueprintItemCountMap::immutable, s -> s.itemCountMap);

    protected final Map<Item, Integer> itemCountMap;

    public static BlueprintItemCountMap.Immutable immutable(Map<Item, Integer> map) {
        return new BlueprintItemCountMap.Immutable(map);
    }

    public static BlueprintItemCountMap.Mutable mutable(Map<Item, Integer> map) {
        return new BlueprintItemCountMap.Mutable(map);
    }

    private BlueprintItemCountMap(Map<Item, Integer> itemCountMap) {
        this.itemCountMap = itemCountMap;
    }

    public BlueprintItemCountMap.Mutable mutable() {
        return mutable(this.itemCountMap);
    }

    public boolean isEmpty() {
        return itemCountMap.isEmpty();
    }

    public abstract int getNumberOfItems();

    @NonNull
    public abstract List<ItemStack> asList();

    public static class Immutable extends BlueprintItemCountMap {
        private final int numberOfItems;
        @Nullable
        private List<ItemStack> asList;

        private Immutable(Map<Item, Integer> map) {
            super(map);
            this.numberOfItems = itemCountMap.values().stream().reduce(0, Integer::sum);
        }

        @Override
        public int getNumberOfItems() {
            return numberOfItems;
        }

        @Override
        public @NonNull List<ItemStack> asList() {
            if (asList == null) {
                asList = BlueprintUtil.itemMapToList(itemCountMap);
            }
            return asList;
        }
    }

    public static class Mutable extends BlueprintItemCountMap {
        public static final BlueprintItemCountMap.Mutable EMPTY = new Mutable(new HashMap<>());

        private Mutable(Map<Item, Integer> itemCountMap) {
            super(new HashMap<>(itemCountMap));
        }

        @Override
        public Mutable mutable() {
            return this;
        }

        @Override
        public @NonNull List<ItemStack> asList() {
            return BlueprintUtil.itemMapToList(itemCountMap);
        }

        @Override
        public int getNumberOfItems() {
            return itemCountMap.values().stream().reduce(0, Integer::sum);
        }

        public void forEach(BiConsumer<Item, Integer> action) {
            itemCountMap.forEach(action);
        }

        public boolean hasItemAndShrink(Item item) {
            if (!itemCountMap.containsKey(item)) {
                return false;
            }
            Integer count = itemCountMap.get(item);
            if (count == null || count <= 0) {
                return false;
            }
            count--;
            if (count <= 0) {
                itemCountMap.remove(item);
            } else {
                itemCountMap.put(item, count);
            }

            return true;
        }
    }
}
