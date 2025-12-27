package net.bumblebee.claysoldiers.datamap;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.networking.DataMapPayloadBuilder;
import net.bumblebee.claysoldiers.platform.services.IDataMapGetter;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class FabricDataMapLoader extends SimpleJsonResourceReloadListener<JsonElement> implements IdentifiableResourceReloadListener {
    private static final String PATH = "data_maps";
    private static final String HOLDABLE_PATH = "item/soldier_holdable";
    private static final String WEARABLE_PATH = "item/soldier_wearable";
    private static final String ITEM_POI_PATH = "item/soldier_poi";
    private static final String BLOCK_POI_PATH = "block/soldier_poi";
    private static final String ENTITY_VEHICLE_PROPERTIES_PATH = "entity_type/soldier_vehicle_properties";

    public static final ResourceLocation FABRIC_ITEM_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "csr_items");
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;

    public static final Map<Holder<Item>, SoldierHoldableEffect> SOLDIER_HOLDABLE_MAP = new HashMap<>();
    public static final Map<Holder<Item>, SoldierMultiWearable> SOLDIER_WEARABLE_MAP = new HashMap<>();
    public static final Map<Holder<Item>, SoldierPoi> SOLDIER_ITEM_POI_MAP = new HashMap<>();
    public static final Map<Holder<Block>, SoldierPoi> SOLDIER_BLOCK_POI_MAP = new HashMap<>();
    public static final Map<Holder<EntityType<?>>, SoldierVehicleProperties> SOLDIER_VEHICLE_PROPERTIES_MAP = new HashMap<>();


    public final static Codec<Map<ExtraCodecs.TagOrElementLocation, SoldierHoldableEffect>> ITEM_SOLDIER_HOLDABLE_CODEC = Codec.unboundedMap(ExtraCodecs.TAG_OR_ELEMENT_ID, SoldierHoldableEffect.CODEC);
    public final static Codec<Map<ExtraCodecs.TagOrElementLocation, SoldierMultiWearable>> ITEM_SOLDIER_WEARABLE_CODEC = Codec.unboundedMap(ExtraCodecs.TAG_OR_ELEMENT_ID, SoldierMultiWearable.CODEC);
    public final static Codec<Map<ExtraCodecs.TagOrElementLocation, SoldierPoi>> ITEM_SOLDIER_POI_CODEC = Codec.unboundedMap(ExtraCodecs.TAG_OR_ELEMENT_ID, SoldierPoi.CODEC);

    public final static Codec<Map<ExtraCodecs.TagOrElementLocation, SoldierVehicleProperties>> ENTITY_SOLDIER_PROPRTIES_CODEC = Codec.unboundedMap(ExtraCodecs.TAG_OR_ELEMENT_ID, SoldierVehicleProperties.CODEC);

    private final static Codec<DataMap<Item, SoldierHoldableEffect>> DATA_MAP_HOLDABLE_CODEC = createDataMapCodec(ITEM_SOLDIER_HOLDABLE_CODEC, BuiltInRegistries.ITEM);
    private final static Codec<DataMap<Item, SoldierMultiWearable>> DATA_MAP_WEARABLE_CODEC = createDataMapCodec(ITEM_SOLDIER_WEARABLE_CODEC, BuiltInRegistries.ITEM);
    private final static Codec<DataMap<Item, SoldierPoi>> DATA_MAP_ITEM_POI_CODEC = createDataMapCodec(ITEM_SOLDIER_POI_CODEC, BuiltInRegistries.ITEM);
    private final static Codec<DataMap<Block, SoldierPoi>> DATA_MAP_BLOCK_POI_CODEC = createDataMapCodec(ITEM_SOLDIER_POI_CODEC, BuiltInRegistries.BLOCK);
    private final static Codec<DataMap<EntityType<?>, SoldierVehicleProperties>> DATA_MAP_VEHICLE_PROPERTIES_CODEC = createDataMapCodec(ENTITY_SOLDIER_PROPRTIES_CODEC, BuiltInRegistries.ENTITY_TYPE);

    private final HolderLookup.Provider provider;

    public FabricDataMapLoader(HolderLookup.Provider provider) {
        super(ExtraCodecs.JSON, FileToIdConverter.json(PATH));
        this.provider = provider;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (provider == null) {
            LOGGER.error("FabricDataMapGetter load Error: Lookup is null");
            return;
        }

        loadItems(object, resourceManager);
        var removed = removeEmpty(SOLDIER_WEARABLE_MAP, SoldierMultiWearable::isEmpty);
        if (!removed.isEmpty()) {
            ErrorHandler.INSTANCE.error("Removed %s Wearable Properties, because they where empty.".formatted(removed));
        }
        SoldierItemType.setTagLoadCallback(() -> {
            provider.lookupOrThrow(ModRegistries.SOLDIER_ITEM_TYPES).listElements().forEach(h -> h.value().afterDataMapLoad());
        });

        TagLoader<Holder<Item>> itemTagLoader = new TagLoader<>((resourceLocation, bl) -> BuiltInRegistries.ITEM.get(resourceLocation), Registries.tagsDirPath(Registries.ITEM));
        var mappedItemTagLoader = itemTagLoader.build(itemTagLoader.load(resourceManager));


        IDataMapGetter.warnHoldable(SOLDIER_HOLDABLE_MAP, (holder, tagKey) -> {
            var tag = mappedItemTagLoader.get(tagKey.location());
            if (tag == null) {
                return false;
            }
            return tag.contains(holder);
        });

        SOLDIER_WEARABLE_MAP.values().forEach(multiWearable -> {
            multiWearable.forEachWearableEffect(wearable -> wearable.buildTrims(provider));
        });
    }

    private void loadItems(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager) {
        TagLoader<Holder<Item>> itemTagLoader = new TagLoader<>(TagLoader.ElementLookup.fromFrozenRegistry(BuiltInRegistries.ITEM), Registries.tagsDirPath(Registries.ITEM));
        Map<ResourceLocation, List<Holder<Item>>> mappedItemTagLoader = itemTagLoader.build(itemTagLoader.load(resourceManager));

        TagLoader<Holder<Block>> blockTagLoader = new TagLoader<>(TagLoader.ElementLookup.fromFrozenRegistry(BuiltInRegistries.BLOCK), Registries.tagsDirPath(Registries.BLOCK));
        var mappedBlockTagLoader = blockTagLoader.build(blockTagLoader.load(resourceManager));

        TagLoader<Holder<EntityType<?>>> entityTypeTagLoader = new TagLoader<>(TagLoader.ElementLookup.fromFrozenRegistry(BuiltInRegistries.ENTITY_TYPE), Registries.tagsDirPath(Registries.ENTITY_TYPE));
        var mappedEntityTypeTagLoader = entityTypeTagLoader.build(entityTypeTagLoader.load(resourceManager));

        object.forEach((key, value) -> {
            switch (key.getPath()) {
                case HOLDABLE_PATH ->
                        loadMap(value, DATA_MAP_HOLDABLE_CODEC, mappedItemTagLoader, SOLDIER_HOLDABLE_MAP, key);
                case WEARABLE_PATH ->
                        loadMap(value, DATA_MAP_WEARABLE_CODEC, mappedItemTagLoader, SOLDIER_WEARABLE_MAP, key);
                case ITEM_POI_PATH ->
                        loadMap(value, DATA_MAP_ITEM_POI_CODEC, mappedItemTagLoader, SOLDIER_ITEM_POI_MAP, key);
                case BLOCK_POI_PATH ->
                        loadMap(value, DATA_MAP_BLOCK_POI_CODEC, mappedBlockTagLoader, SOLDIER_BLOCK_POI_MAP, key);
                case ENTITY_VEHICLE_PROPERTIES_PATH ->
                        loadMap(value, DATA_MAP_VEHICLE_PROPERTIES_CODEC, mappedEntityTypeTagLoader, SOLDIER_VEHICLE_PROPERTIES_MAP, key);
            }
        });
        IDataMapGetter.createBySlotMap(SOLDIER_HOLDABLE_MAP, Holder::value);
    }

    private static <T, H> void loadMap(JsonElement jsonElement, Codec<DataMap<H, T>> dataMapCodec,
                                       Map<ResourceLocation, List<Holder<H>>> tagLoader, Map<Holder<H>, T> staticMap, ResourceLocation name) {
        Map<Holder<H>, T> replaceMap = new HashMap<>();
        staticMap.clear();

        dataMapCodec.decode(JsonOps.INSTANCE, jsonElement).ifSuccess(dataMapPartial -> dataMapPartial.getFirst().forEach(tagLoader, staticMap::put, replaceMap::put, name)).ifError(err -> LOGGER.error("DataMap from {} loaded with error: {}", name, err.error()));
        staticMap.putAll(replaceMap);
        LOGGER.info("Loaded {} Entries for {}.", staticMap.size(), name);
        LOGGER.debug("Loaded Entries of {}  for {}", name, staticMap.keySet());
    }

    private static <K, V> List<K> removeEmpty(Map<K, V> map, Predicate<V> emptyPredicate) {
        List<K> keysToRemove = map.entrySet().stream().filter(e -> emptyPredicate.test(e.getValue())).map(Map.Entry::getKey).toList();
        keysToRemove.forEach(map::remove);
        return keysToRemove;
    }

    public static List<String> getLoadedDataMapsWithSize() {
        var list = new ArrayList<String>();
        if (!SOLDIER_HOLDABLE_MAP.isEmpty()) {
            list.add(HOLDABLE_PATH + ": " + SOLDIER_HOLDABLE_MAP.size());
        }
        if (!SOLDIER_WEARABLE_MAP.isEmpty()) {
            list.add(WEARABLE_PATH + ": " + SOLDIER_WEARABLE_MAP.size());
        }
        if (!SOLDIER_ITEM_POI_MAP.isEmpty()) {
            list.add(ITEM_POI_PATH + ": " + SOLDIER_ITEM_POI_MAP.size());
        }
        if (!SOLDIER_BLOCK_POI_MAP.isEmpty()) {
            list.add(BLOCK_POI_PATH + ": " + SOLDIER_BLOCK_POI_MAP.size());
        }
        return list;
    }

    @Override
    public ResourceLocation getFabricId() {
        return FABRIC_ITEM_ID;
    }

    private record DataMap<H, T>(Map<ExtraCodecs.TagOrElementLocation, T> data, boolean replace, Registry<H> registry) {
        public void forEach(Map<ResourceLocation, List<Holder<H>>> tagLookup, BiConsumer<Holder<H>, T> addMap, BiConsumer<Holder<H>, T> replaceMap, ResourceLocation description) {
            data.forEach((key, value) -> {
                var entry = replace ? replaceMap : addMap;
                if (key.tag()) {
                    Optional.ofNullable(tagLookup.get(key.id())).ifPresentOrElse(
                            (holders) -> holders.forEach(holder -> entry.accept(holder, value)),
                            () -> LOGGER.error("Cannot parse Tag with Id for {}: {}", description, key.id())
                    );
                } else {
                    registry.get(key.id()).ifPresentOrElse(
                            (holder) -> entry.accept(holder, value),
                            () -> LOGGER.error("Cannot parse Holder with Id for {}}: {}", description, key.id())
                    );
                }
            });
        }
    }

    private static <T, H> Codec<DataMap<H, T>> createDataMapCodec(Codec<Map<ExtraCodecs.TagOrElementLocation, T>> dataCodec, Registry<H> registry) {
        return RecordCodecBuilder.create(in -> in.group(
                dataCodec.fieldOf("values").forGetter(DataMap::data),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataMap::replace)
        ).apply(in, (s, r) -> new DataMap<>(s, r, registry)));
    }

    public static void sentPayloadsToClient(ServerPlayer serverPlayer) {
        LOGGER.info("Sending DataMap payloads to {}", serverPlayer.getScoreboardName());
        ServerPlayNetworking.send(serverPlayer, DataMapPayloadBuilder.HOLDABLE.create(SOLDIER_HOLDABLE_MAP));
        ServerPlayNetworking.send(serverPlayer, DataMapPayloadBuilder.WEARABLE.create(SOLDIER_WEARABLE_MAP));
        ServerPlayNetworking.send(serverPlayer, DataMapPayloadBuilder.ITEM_POI.create(SOLDIER_ITEM_POI_MAP));
        ServerPlayNetworking.send(serverPlayer, DataMapPayloadBuilder.BLOCK_POI.create(SOLDIER_BLOCK_POI_MAP));
        ServerPlayNetworking.send(serverPlayer, DataMapPayloadBuilder.VEHICLE_PROPERTIES.create(SOLDIER_VEHICLE_PROPERTIES_MAP));
    }

    public static void updateSoldierHoldable(Map<Holder<Item>, SoldierHoldableEffect> map) {
        updateClientMap(SOLDIER_HOLDABLE_MAP, map, "soldier_holdable");
    }

    public static void updateSoldierWearable(Map<Holder<Item>, SoldierMultiWearable> map, RegistryAccess registryAccess) {
        updateClientMap(SOLDIER_WEARABLE_MAP, map, "soldier_wearable");
        SOLDIER_WEARABLE_MAP.values().forEach(w -> w.forEachWearableEffect(e -> e.buildTrims(registryAccess)));
    }

    public static void updateSoldierItemPoi(Map<Holder<Item>, SoldierPoi> map) {
        updateClientMap(SOLDIER_ITEM_POI_MAP, map, "soldier_item_poi");
    }

    public static void updateSoldierBlockPoi(Map<Holder<Block>, SoldierPoi> map) {
        updateClientMap(SOLDIER_BLOCK_POI_MAP, map, "soldier_block_poi");
    }

    public static void updateVehicleProperties(Map<Holder<EntityType<?>>, SoldierVehicleProperties> map) {
        updateClientMap(SOLDIER_VEHICLE_PROPERTIES_MAP, map, "soldier_vehicle_properties");
    }

    private static <H, T> void updateClientMap(Map<Holder<H>, T> old, Map<Holder<H>, T> newMap, String name) {
        LOGGER.info("Syncing {} had previously {} entries now {}", name, old.size(), newMap.size());
        old.clear();
        old.putAll(newMap);
    }
}