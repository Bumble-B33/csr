package net.bumblebee.claysoldiers.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.datagen.advancements.ModAdvancements;
import net.bumblebee.claysoldiers.datagen.advancements.ModBlueprintAdvancementProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModBlockTagsProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModItemTagProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModTagProvider;
import net.bumblebee.claysoldiers.init.ModArmorMaterials;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosDataProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DataGenerators {
    public static final Logger LOGGER = LoggerFactory.getLogger("CSR DataGeneration");

    public static void gatherData(final GatherDataEvent.Client event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();

        PackOutput neoforge = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "neoforge")));
        PackOutput fabric = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "fabric")));

        LOGGER.info("Common PackOut: {}", packOutput.getOutputFolder());
        LOGGER.info("Fabric PackOut: {}", fabric.getOutputFolder());
        LOGGER.info("NeoForge PackOut: {}", neoforge.getOutputFolder());

        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.createProvider(ModDatapackProvider::builtin);

        ModTagProvider.getTagProviders().forEach(event::createProvider);
        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagProvider::new);

        event.createProvider(ModLoottableProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLangProvider::new);
        event.createProvider(ModParticleProvider::new);

        event.addProvider(new EquipmentAssetProvider(packOutput) {
            @Override
            protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
                output.accept(ModArmorMaterials.CLAY_GOGGLES_ID,
                        EquipmentClientInfo.builder().addMainHumanoidLayer(ModArmorMaterials.CLAY_GOGGLES_ID.location(), false).build()
                );
            }
        });

        event.createProvider((p, l) -> new AdvancementProvider(
                p, l, List.of(new ModAdvancements())
        ));

        event.addProvider(new ModCuriosDataProvider(neoforge, lookupProvider));

        DataGenerator.PackGenerator featurePack = generator.getBuiltinDatapack(true, ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.BLUEPRINT_PACK_PATH);

        PackOutput neoBlueprintPackOut = new PackOutput(neoforge.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(ClaySoldiersCommon.MOD_ID).resolve("datapacks").resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH));
        featurePack.addProvider(packOut -> PackMetadataGenerator.forFeaturePack(
                neoBlueprintPackOut,
                Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION),
                FeatureFlagSet.of(ClaySoldiersNeoForge.BLUEPRINT_FLAG)
        ));
        event.addProvider(new ModBlueprintAdvancementProvider("NeoForge", neoBlueprintPackOut, lookupProvider));


        PackOutput fabricBluePrintPackOut = new PackOutput(fabric.getOutputFolder().resolve(ClaySoldiersCommon.CSR_DATA_PACK_LOCATION).resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH));
        event.addProvider(new PackMetadataGenerator(
                fabricBluePrintPackOut).add(
                PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
        ));
        event.addProvider(new ModBlueprintAdvancementProvider("Fabric", fabricBluePrintPackOut, lookupProvider));

        itemsDataPack(packOutput.getOutputFolder(), event);

    }

    private static void itemsDataPack(Path output, GatherDataEvent.Client event) {
        PackOutput path = new PackOutput(output.resolve(ClaySoldiersCommon.CSR_DATA_PACK_LOCATION).resolve(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH));
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.addProvider(new CustomPackMetadataProvider(path)
                .add(
                        PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
                ));

        event.addProvider(ModDatapackProvider.datapack(path, lookupProvider));
        event.addProvider(new ModDataMapAndTagProvider(path, lookupProvider));
        event.addProvider(new ModDataMapProvider(path, lookupProvider));
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