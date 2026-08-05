package net.bumblebee.claysoldiers.init;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.soldier.AdditionalSoldierData;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchContent;
import net.bumblebee.claysoldiers.item.disruptor.DisruptorKillRange;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class ModDataComponents {
    public static final Supplier<DataComponentType<ResourceKey<ClayMobTeam>>> CLAY_MOB_TEAM_COMPONENT = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_mob_team",
            () -> DataComponentType.<ResourceKey<ClayMobTeam>>builder()
                    .persistent(ClayMobTeam.KEY_CODEC)
                    .networkSynchronized(ClayMobTeam.KEY_STREAM_CODE)
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

    public static final Supplier<DataComponentType<PoiPosInfo>> POI_POS = ClaySoldiersCommon.PLATFORM.registerDataComponent("poi_pos",
            () -> DataComponentType.<PoiPosInfo>builder()
                    .persistent(PoiPosInfo.CODEC)
                    .networkSynchronized(PoiPosInfo.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<ResourceKey<BlueprintData>>> BLUEPRINT_DATA = ClaySoldiersCommon.PLATFORM.registerDataComponent("blueprint_data",
            () -> DataComponentType.<ResourceKey<BlueprintData>>builder()
                    .persistent(ResourceKey.codec(ModRegistries.BLUEPRINTS))
                    .networkSynchronized(ResourceKey.streamCodec(ModRegistries.BLUEPRINTS))
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

    public static final Supplier<DataComponentType<ClaySoldierChip>> CLAY_SOLDIER_CHIP = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_soldier_chip",
            () -> DataComponentType.<ClaySoldierChip>builder()
                    .persistent(ClaySoldierChip.CODEC)
                    .networkSynchronized(ClaySoldierChip.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<ClaySoldierChipAddon>> CLAY_SOLDIER_CHIP_ADDON = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_soldier_chip_addon",
            () -> DataComponentType.<ClaySoldierChipAddon>builder()
                    .persistent(ClaySoldierChipAddon.CODEC)
                    .networkSynchronized(ClaySoldierChipAddon.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<Integer>> CLAY_SOLDIER_CHIP_ADDON_ACCELERATION = ClaySoldiersCommon.PLATFORM.registerDataComponent("clay_soldier_chip_addon_acceleration",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(1, 64))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );


    public static void init() {
    }

    private ModDataComponents() {
    }
}
