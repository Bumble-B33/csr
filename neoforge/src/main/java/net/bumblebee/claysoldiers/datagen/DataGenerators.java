package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.ClaySoldiersNeoForge;
import net.bumblebee.claysoldiers.datagen.tags.ModBlockTagsProvider;
import net.bumblebee.claysoldiers.datagen.tags.ModTagProvider;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static final Logger LOGGER = LoggerFactory.getLogger("CSR DataGeneration");

    public static void gatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();
        final ExistingFileHelper helper = event.getExistingFileHelper();

        PackOutput neoforge = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "neoforge")));
        PackOutput fabric = new PackOutput(Path.of(packOutput.getOutputFolder().toString().replace("common", "fabric")));

        LOGGER.info("Common PackOut: {}", packOutput.getOutputFolder());
        LOGGER.info("Fabric PackOut: {}", fabric.getOutputFolder());
        LOGGER.info("NeoForge PackOut: {}", neoforge.getOutputFolder());

        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(), new ModDataMapAndTagProvider(packOutput, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(packOutput, lookupProvider));

        ModTagProvider.getTagProviders(packOutput, lookupProvider, helper).forEach(tag -> generator.addProvider(event.includeServer(), tag));

        generator.addProvider(event.includeClient(), new ModRecipeProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, helper));

        generator.addProvider(event.includeClient(), new ModLangProvider(packOutput));
        generator.addProvider(event.includeClient(), new ModParticleProvider(packOutput, helper));
        generator.addProvider(event.includeServer(), new ModBlockStateProvider(packOutput, helper));
        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new ModLoottableProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(), new ModCuriosProvider(neoforge, helper, lookupProvider));

        DataGenerator.PackGenerator featurePack = generator.getBuiltinDatapack(event.includeServer(), ClaySoldiersCommon.MOD_ID, ClaySoldiersCommon.BLUEPRINT_PACK_PATH);

        featurePack.addProvider(packOut -> PackMetadataGenerator.forFeaturePack(
                new PackOutput(neoforge.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(ClaySoldiersCommon.MOD_ID).resolve("datapacks").resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH)),
                Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION),
                FeatureFlagSet.of(ClaySoldiersNeoForge.BLUEPRINT_FLAG)
        ));

        generator.addProvider(event.includeServer(), new PackMetadataGenerator(
                new PackOutput(fabric.getOutputFolder().resolve("resourcepacks").resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH))).add(
                PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION), DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
        ));
        generator.addProvider(event.includeServer(), new DummyGenerator(fabric.getOutputFolder().toAbsolutePath().resolve("resourcepacks").resolve(ClaySoldiersCommon.BLUEPRINT_PACK_PATH).resolve("data"), "Needed so Fabric Recognizes this DataPack"));

    }
}