package net.bumblebee.claysoldiers.init;

import com.google.common.collect.ImmutableSet;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class ModPoiTypes {
    public static final ResourceKey<PoiType> HAMSTER_WHEEL_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel_poi"));


    public static void init() {
        ClaySoldiersCommon.PLATFORM.registerPoiType(HAMSTER_WHEEL_POI_KEY,
                () -> new PoiType(ImmutableSet.copyOf(ModBlocks.HAMSTER_WHEEL_BLOCK.get().getStateDefinition().getPossibleStates()),1, 1)
        );
    }

    private ModPoiTypes() {
    }
}
