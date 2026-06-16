package net.bumblebee.claysoldiers.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datagen.advancements.ModAdvancements;
import net.bumblebee.claysoldiers.datagen.recipe.ModRecipeProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModBlockTagsProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModItemTagProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModTagProvider;
import net.bumblebee.claysoldiers.init.ModArmorMaterials;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosDataProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record PlatformGatherDataEvent(GatherDataEvent event, PackOutput packOutput) {
    public PlatformGatherDataEvent(GatherDataEvent event, Path path) {
        this(event, new PackOutput(path));
    }

    public <T extends DataProvider> T createProvider(GatherDataEvent.DataProviderFromOutputLookup<T> builder) {
        return event.addProvider(builder.create(packOutput, event.getLookupProvider()));
    }

    public <T extends DataProvider> void createProvider(GatherDataEvent.DataProviderFromOutput<T> builder) {
        event.addProvider(builder.create(packOutput));
    }

    public void createDatapackRegistryObjects(RegistrySetBuilder datapackEntriesBuilder) {
        event.createDatapackRegistryObjects(datapackEntriesBuilder);
    }

    public void createBlockAndItemTags(GatherDataEvent.DataProviderFromOutputLookup<TagsProvider<Block>> blockTagsProvider, GatherDataEvent.ItemTagsProvider itemTagsProvider) {
        var blockTags = createProvider(blockTagsProvider);
        event.addProvider(itemTagsProvider.create(packOutput, event.getLookupProvider(), blockTags.contentsGetter()));
    }

    public static void generateCommon(PlatformGatherDataEvent event) {
        event.createDatapackRegistryObjects(ModDatapackProvider.BUILDER_BUILTIN);

        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModRecipeProvider::create);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLangProvider::new);
        event.createProvider(ModParticleProvider::new);

        event.createProvider(p -> new EquipmentAssetProvider(p) {
            @Override
            protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
                output.accept(ModArmorMaterials.CLAY_GOGGLES_ID,
                        EquipmentClientInfo.builder().addMainHumanoidLayer(ModArmorMaterials.CLAY_GOGGLES_ID.identifier(), false).build()
                );
                output.accept(ModArmorMaterials.MUSHROOM_HELMET_ID,
                        EquipmentClientInfo.builder().addMainHumanoidLayer(ModArmorMaterials.MUSHROOM_HELMET_ID.identifier(), false).build()
                );
                output.accept(ModArmorMaterials.SHULKER_HELMET_ID,
                        EquipmentClientInfo.builder().addMainHumanoidLayer(ModArmorMaterials.SHULKER_HELMET_ID.identifier(), false).build()
                );
            }
        });

        event.createProvider(ModAdvancements::createProvider);


        ModTagProvider.getTagProviders().forEach(event::createProvider);
        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagProvider::new);

        event.createProvider(ModCuriosDataProvider::new);
    }

    public static void defaultDataPack(PlatformGatherDataEvent event) {
        event.createProvider(p -> new CustomPackMetadataProvider(p)
                .add(
                        PackMetadataSection.SERVER_TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA).minorRange())
                ));

        event.createProvider(ModDatapackProvider::datapack);
        event.createProvider(ModDataMapAndTagProvider::new);
        event.createProvider(ModDataMapProvider::new);
    }

    private static class CustomPackMetadataProvider implements DataProvider {
        private final PackOutput output;
        private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

        public CustomPackMetadataProvider(PackOutput output) {
            this.output = output;
        }

        public <T> CustomPackMetadataProvider add(MetadataSectionType<T> type, T value) {
            this.elements.put(
                    type.name(),
                    () -> type.codec().encodeStart(JsonOps.INSTANCE, value)
                            .ifError(err -> LOGGER.error("Error encoding PackMetadata for {}: {}", output.getOutputFolder(), err.message()))
                            .result().orElseThrow()
            );
            return this;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            JsonObject jsonobject = new JsonObject();
            this.elements.forEach((p_249290_, p_251317_) -> jsonobject.add(p_249290_, p_251317_.get()));
            return DataProvider.saveStable(output, jsonobject, this.output.getOutputFolder().resolve("pack.mcmeta"));
        }

        @Override
        public String getName() {
            return "Custom Pack Metadata";
        }
    }

}
