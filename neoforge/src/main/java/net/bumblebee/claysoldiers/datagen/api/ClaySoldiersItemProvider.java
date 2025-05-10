package net.bumblebee.claysoldiers.datagen.api;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.armor.SoldierWearableEffect;
import net.bumblebee.claysoldiers.init.ModDataMaps;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class ClaySoldiersItemProvider implements DataProvider {
    private final DataMapProvider dataMapProvider;
    private final DataMapProvider.Builder<SoldierHoldableEffect, Item> holdableBuilder;
    private final DataMapProvider.Builder<SoldierMultiWearable, Item> armorBuilder;
    private final DataMapProvider.Builder<SoldierPoi, Item> poiItemBuilder;
    private final DataMapProvider.Builder<SoldierPoi, Block> poiBlockBuilder;
    private final CustomItemTagsProvider itemTagsProvider;
    private final Set<Item> allItems;
    private final Set<TagKey<Item>> allTags;

    protected ClaySoldiersItemProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, ExistingFileHelper helper) {
        this.dataMapProvider = new DataMapProvider(packOutput, lookupProvider) {
            @Override
            protected void gather(HolderLookup.Provider provider) {}
        };
        this.itemTagsProvider = new CustomItemTagsProvider(packOutput, lookupProvider, modid, helper);
        this.holdableBuilder = dataMapProvider.builder(ModDataMaps.SOLDIER_HOLDABLE);
        this.poiItemBuilder = dataMapProvider.builder(ModDataMaps.SOLDIER_ITEM_POI);
        this.armorBuilder = dataMapProvider.builder(ModDataMaps.SOLDIER_ARMOR);
        this.poiBlockBuilder = dataMapProvider.builder(ModDataMaps.SOLDIER_BLOCK_POI);
        this.allItems = new HashSet<>();
        this.allTags = new HashSet<>();
    }

    private void registerAddedItem(ItemLike itemLike) {
        if (!allItems.add(itemLike.asItem())) {
            LOGGER.error("Added {} to DataMap twice", itemLike.asItem());
        }
    }
    private void registerAddedItemTag(TagKey<Item> tag) {
        if (!allTags.add(tag)) {
            LOGGER.error("Added {} to DataMap twice", tag);
        }
    }

    abstract protected void gather();

    protected void addHoldable(ItemLike item, SoldierHoldableEffect holdableEffect, ItemTagHolder... itemTypes) {
        addHoldable(item, holdableEffect, TagType.HOLDABLE, itemTypes);
    }
    protected void addHoldable(TagKey<Item> tagKey, SoldierHoldableEffect holdableEffect, ItemTagHolder... itemTypes) {
        addHoldable(tagKey, holdableEffect, TagType.HOLDABLE, itemTypes);
    }
    protected void addHoldable(ItemLike item, SoldierHoldableEffect holdableEffect, TagType type, ItemTagHolder... itemTypes) {
        holdableBuilder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), holdableEffect, false);
        itemTagsProvider.getTag(type).add(item.asItem());
        addItemToItemTypes(item.asItem(), itemTypes);
        warnHoldableEffect(item.asItem().toString(), holdableEffect, itemTypes);

        registerAddedItem(item);
    }

    protected void addHoldable(TagKey<Item> tagKey, SoldierHoldableEffect holdableEffect, TagType type, ItemTagHolder... itemTypes) {
        holdableBuilder.add(tagKey, holdableEffect, false);
        itemTagsProvider.getTag(type).addTag(tagKey);
        addItemToItemTypes(tagKey, itemTypes);
        warnHoldableEffect(tagKey.toString(), holdableEffect, itemTypes);

        registerAddedItemTag(tagKey);
    }

    protected void addWearable(ItemLike item, SoldierHoldableEffect holdableEffect, SoldierWearableEffect wearableEffect, ItemTagHolder... itemTypes) {
        addHoldable(item, holdableEffect, TagType.ARMOR, itemTypes);
        var builder = SoldierMultiWearable.of();
        for (var slot : holdableEffect.slots()) {
            builder.put(slot, wearableEffect);
        }
        if (builder.wearableSize() >= 1) {
            armorBuilder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), builder.build(), false);
        }
    }
    protected void addWearable(ItemLike item, SoldierHoldableEffect holdableEffect, SoldierMultiWearable wearableEffect, ItemTagHolder... itemTypes) {
        armorBuilder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), wearableEffect, false);
        addHoldable(item, holdableEffect, TagType.ARMOR, itemTypes);
    }
    protected void addWearable(TagKey<Item> item, SoldierHoldableEffect holdableEffect, SoldierMultiWearable wearableEffect, ItemTagHolder... itemTypes) {
        addHoldable(item, holdableEffect, TagType.ARMOR, itemTypes);
        armorBuilder.add(item, wearableEffect, false);
    }
    protected void addSoldierItemPoi(ItemLike item, SoldierPoi soldierPoi) {
        poiItemBuilder.add(BuiltInRegistries.ITEM.getKey(item.asItem()), soldierPoi, false);
        itemTagsProvider.getTag(ModTags.Items.SOLDIER_POI).add(item.asItem());

        registerAddedItem(item);
    }
    protected void addSoldierItemPoi(TagKey<Item> itemTag, SoldierPoi soldierPoi) {
        poiItemBuilder.add(itemTag, soldierPoi, false);
        itemTagsProvider.getTag(ModTags.Items.SOLDIER_POI).addTag(itemTag);

        registerAddedItemTag(itemTag);
    }
    protected void addSoldierBlockPoi(Block poi, SoldierPoi soldierPoi) {
        poiBlockBuilder.add(BuiltInRegistries.BLOCK.getKey(poi), soldierPoi, false);
        itemTagsProvider.getTag(ModTags.Items.SOLDIER_POI).add(poi.asItem());
    }

    private void warnHoldableEffect(String name, SoldierHoldableEffect effect, ItemTagHolder[] types) {
        effect.getRemovalConditions().forEach(r -> {
            if (r.getChance() <= 0) {
                ClaySoldiersCommon.LOGGER.warn("DataMap: Building {} with a RemovalCondition with chance 0", name);
            }
        });
        if (!effect.slots().isEmpty() && types.length == 0) {
            ClaySoldiersCommon.LOGGER.warn("DataMap: {} has no SoldierItemTypes but can be equipped.", name);

        }
    }

    private void addItemToItemTypes(Item item, ItemTagHolder... itemTypes) {
        for (ItemTagHolder type : itemTypes) {
            itemTagsProvider.getTag(type.getTag()).add(item);
        }
    }
    private void addItemToItemTypes(TagKey<Item> item, ItemTagHolder... itemTypes) {
        for (ItemTagHolder type : itemTypes) {
            itemTagsProvider.getTag(type.getTag()).addTag(item);
        }
    }

    protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(TagKey<Item> tag) {
        return itemTagsProvider.getTag(tag);
    }
    protected <T, R> DataMapProvider.Builder<T, R> builder(DataMapType<R, T> type) {
        return dataMapProvider.builder(type);
    }
    protected <T, R, VR extends DataMapValueRemover<R, T>> DataMapProvider.AdvancedBuilder<T, R, VR> builder(AdvancedDataMapType<R, T, VR> type) {
        return dataMapProvider.builder(type);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        gather();
        return CompletableFuture.allOf(itemTagsProvider.run(pOutput), dataMapProvider.run(pOutput));
    }

    @Override
    public String getName() {
        return "Clay Soldier Provider";
    }

    private static class CustomItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
        private final CompletableFuture<HolderLookup.Provider> lookupProviderCopy;

        private CustomItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookUpProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput, Registries.ITEM, lookUpProvider, (item -> item.builtInRegistryHolder().key()), modId, existingFileHelper);
            this.lookupProviderCopy = lookUpProvider;
        }

        private IntrinsicTagAppender<Item> getTag(TagType type) {
            return tag(type.tag);
        }

        private IntrinsicTagAppender<Item> getTag(TagKey<Item> key) {
            return tag(key);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {}
        @Override
        protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
            return this.lookupProviderCopy.thenApply(provider -> provider);
        }

    }
    protected enum TagType {
        HOLDABLE(ModTags.Items.SOLDIER_HOLDABLE),
        WEAPON(ModTags.Items.SOLDIER_WEAPON),
        ARMOR(ModTags.Items.SOLDIER_ARMOR);

        private final TagKey<Item> tag;

        TagType(TagKey<Item> tag) {
            this.tag = tag;
        }
    }

    public interface ItemTagHolder {
        TagKey<Item> getTag();
    }
}
