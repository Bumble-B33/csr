package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AdditionalSoldierData;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchContent;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.disruptor.DisruptorKillRange;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class ModDataComponents {
    public static final Supplier<DataComponentType<ResourceLocation>> CLAY_MOB_TEAM_COMPONENT = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_mob_team",
            () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build()
    );
    public static final Supplier<DataComponentType<AdditionalSoldierData>> CLAY_SOLDIER_ADDITIONAL_DATA = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_soldier_additional",
            () -> DataComponentType.<AdditionalSoldierData>builder()
                    .persistent(AdditionalSoldierData.CODEC)
                    .build()
    );
    public static final Supplier<DataComponentType<ClayBrushItem.Mode>> CLAY_BRUSH_MODE = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_brush_mode",
            () -> DataComponentType.<ClayBrushItem.Mode>builder()
                    .persistent(ClayBrushItem.Mode.CODEC)
                    .networkSynchronized(ClayBrushItem.Mode.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<ClayBrushItem.PoiPos>> CLAY_BRUSH_POI = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_brush_poi",
            () -> DataComponentType.<ClayBrushItem.PoiPos>builder()
                    .persistent(ClayBrushItem.PoiPos.CODEC)
                    .networkSynchronized(ClayBrushItem.PoiPos.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<ResourceLocation>> BLUEPRINT_DATA = ClaySoldiersCommon.PLATFORM.registerDataComponent("blueprint_data",
            () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<BlueprintItem.BlueprintItemData>> BLUEPRINT_ITEM_DATA = ClaySoldiersCommon.PLATFORM.registerDataComponent("blueprint_item_data",
            () -> DataComponentType.<BlueprintItem.BlueprintItemData>builder()
                    .persistent(BlueprintItem.BlueprintItemData.CODEC)
                    .networkSynchronized(BlueprintItem.BlueprintItemData.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<DisruptorKillRange>> DISRUPTOR_KILL_RANGE = ClaySoldiersCommon.PLATFORM.registerDataComponent("disruptor_range",
            () -> DataComponentType.<DisruptorKillRange>builder()
                    .persistent(DisruptorKillRange.CODEC)
                    .networkSynchronized(DisruptorKillRange.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<ClayPouchContent>> CLAY_POUCH_CONTENT = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_pouch_content",
            () -> DataComponentType.<ClayPouchContent>builder()
                    .persistent(ClayPouchContent.CODEC)
                    .networkSynchronized(ClayPouchContent.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<TestItem.Mode>> DEBUG_ITEM_MODE = ClaySoldiersCommon.PLATFORM.ifDevEv(() -> ClaySoldiersCommon.PLATFORM.registerDataComponent("debug_mode",
            () -> DataComponentType.<TestItem.Mode>builder()
                    .persistent(TestItem.Mode.CODEC)
                    .networkSynchronized(TestItem.Mode.STREAM_CODEC)
                    .build()), null);

    public static void init() {
    }

    private ModDataComponents() {
    }
}
