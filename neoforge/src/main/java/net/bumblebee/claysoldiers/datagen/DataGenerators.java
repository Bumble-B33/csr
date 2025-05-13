package net.bumblebee.claysoldiers.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.datagen.tags.ModBlockTagsProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModItemTagProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModTagProvider;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DataGenerators {
    public static final Logger LOGGER = LoggerFactory.getLogger("CSR DataGeneration");

    public static void gatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();
        final ExistingFileHelper helper = event.getExistingFileHelper();

        helper.trackGenerated(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_holdable"), PackType.SERVER_DATA, ".json", "tags/item");
        helper.trackGenerated(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_hurt"), PackType.SERVER_DATA, ".json", "damage_type");
        helper.trackGenerated(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_on_fire"), PackType.SERVER_DATA, ".json", "damage_type");


        PackOutput neoforge = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "neoforge")));
        PackOutput fabric = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "fabric")));

        LOGGER.info("Common PackOut: {}", packOutput.getOutputFolder());
        LOGGER.info("Fabric PackOut: {}", fabric.getOutputFolder());
        LOGGER.info("NeoForge PackOut: {}", neoforge.getOutputFolder());

        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), ModDatapackProvider.builtin(packOutput, lookupProvider));


        ModTagProvider.getTagProviders(packOutput, lookupProvider, helper).forEach(tag -> generator.addProvider(event.includeServer(), tag));
        var blockProvider = new ModBlockTagsProvider(packOutput, lookupProvider, helper);
        generator.addProvider(event.includeServer(), blockProvider);
        generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockProvider.contentsGetter(), helper));


        generator.addProvider(event.includeClient(), new ModRecipeProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, helper));

        generator.addProvider(event.includeClient(), new ModLangProvider(packOutput));
        generator.addProvider(event.includeClient(), new ModParticleProvider(packOutput, helper));
        generator.addProvider(event.includeServer(), new ModBlockStateProvider(packOutput, helper));
        generator.addProvider(event.includeServer(), new ModLoottableProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(), new ModCuriosProvider(neoforge, helper, lookupProvider));

        DataGenerator.PackGenerator featurePack = generator.getBuiltinDatapack(event.includeServer(), ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.BLUEPRINT_PACK_PATH);

        featurePack.addProvider(packOut -> PackMetadataGenerator.forFeaturePack(
                new PackOutput(neoforge.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(ClaySoldiersCommon.MOD_ID).resolve("datapacks").resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH)),
                Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION),
                FeatureFlagSet.of(ClaySoldiersNeoForge.BLUEPRINT_FLAG)
        ));

        generator.addProvider(event.includeServer(), new PackMetadataGenerator(
                new PackOutput(fabric.getOutputFolder().resolve(ClaySoldiersCommon.CSR_DEFAULT_PACK_LOCATION).resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH))).add(
                PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
        ));

        itemsDataPack(packOutput.getOutputFolder(), event);
    }

    private static void itemsDataPack(Path output, GatherDataEvent event) {
        PackOutput path = new PackOutput(output.resolve(ClaySoldiersCommon.CSR_DEFAULT_PACK_LOCATION).resolve(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH));
        final DataGenerator generator = event.getGenerator();
        final ExistingFileHelper helper = event.getExistingFileHelper();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new CustomPackMetadataProvider(path)
                .add(
                        PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.CSR_DEFAULT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
                )
        );

        generator.addProvider(event.includeServer(), ModDatapackProvider.datapack(path, lookupProvider));
        generator.addProvider(event.includeServer(), new ModDataMapAndTagProvider(path, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(path, lookupProvider));

    }

    private static class CustomPackMetadataProvider implements DataProvider {
        private final PackOutput output;
        private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

        public CustomPackMetadataProvider(PackOutput output) {
            this.output = output;
        }

        public <T> CustomPackMetadataProvider add(MetadataSectionType<T> type, T value) {
            this.elements.put(type.getMetadataSectionName(), () -> type.toJson(value));
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