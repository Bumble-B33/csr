package net.bumblebee.claysoldiers.init;

import com.google.common.collect.ImmutableSet;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Comparator;
import java.util.function.Supplier;

public class ModPoiTypes {
    public static final ResourceKey<PoiType> SINGLE_SOLDIER_CONTAINER_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel_poi"));
    public static final ResourceKey<PoiType> CLAY_CACTUS_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "cactus_house"));


    public static final Supplier<PoiType> SINGLE_SOLDIER_CONTAINER = ClaySoldiersCommon.PLATFORM.registerPoiType(SINGLE_SOLDIER_CONTAINER_POI_KEY,
            () -> new PoiType(ImmutableSet.<BlockState>builder()
                    .addAll(ModBlocks.HAMSTER_WHEEL_BLOCK.get().getStateDefinition().getPossibleStates())
                    .addAll(ModBlocks.SUGAR_CANE_HAMMOCK.get().getStateDefinition().getPossibleStates())
                    .build(), 1, 1)
    );

    public static final Supplier<PoiType> CLAY_CACTUS = ClaySoldiersCommon.PLATFORM.registerPoiType(CLAY_CACTUS_POI_KEY,
            () -> new PoiType(ImmutableSet.<BlockState>builder()
                    .addAll(ModBlocks.CACTUS_HOUSE.get().getStateDefinition().getPossibleStates())
                    .build(), ClayCactusBlock.COUNT.getPossibleValues().stream().max(Integer::compareTo).orElse(0), 1)
    );

    public static void init() {

    }

    private ModPoiTypes() {
    }
}
