package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGenerators {
    public static final Logger LOGGER = LoggerFactory.getLogger("CSR DataGeneration");

    public static void gatherData(final GatherDataEvent.Client event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();

        LOGGER.info("Common PackOut: {}", packOutput.getOutputFolder());

        PlatformGatherDataEvent.generateCommon(new PlatformGatherDataEvent(event, packOutput));

        PlatformGatherDataEvent.defaultDataPack(new PlatformGatherDataEvent(event, packOutput.getOutputFolder().resolve(ClaySoldiersCommon.CSR_DATA_PACK_LOCATION).resolve(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH)));
    }
}