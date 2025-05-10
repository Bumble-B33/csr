package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class ModDataMaps {
    public static final DataMapType<Item, SoldierHoldableEffect> SOLDIER_HOLDABLE = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_holdable"), Registries.ITEM, SoldierHoldableEffect.CODEC
    ).synced(SoldierHoldableEffect.CODEC, true)
            .build();

    public static final DataMapType<Item, SoldierMultiWearable> SOLDIER_ARMOR = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_wearable"), Registries.ITEM, SoldierMultiWearable.CODEC
    ).synced(SoldierMultiWearable.CODEC, true).build();


    public static final DataMapType<Item, SoldierPoi> SOLDIER_ITEM_POI = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_poi"), Registries.ITEM, SoldierPoi.CODEC
    ).synced(SoldierPoi.CODEC, true).build();
    public static final DataMapType<Block, SoldierPoi> SOLDIER_BLOCK_POI = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_poi"), Registries.BLOCK, SoldierPoi.CODEC
    ).synced(SoldierPoi.CODEC, true).build();

    public static final DataMapType<EntityType<?>, SoldierVehicleProperties> SOLDIER_VEHICLE_PROPERTIES = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_vehicle_properties"), Registries.ENTITY_TYPE, SoldierVehicleProperties.CODEC
    ).synced(SoldierVehicleProperties.CODEC, true).build();


    public static void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(ModDataMaps.SOLDIER_HOLDABLE);
        event.register(ModDataMaps.SOLDIER_ITEM_POI);
        event.register(ModDataMaps.SOLDIER_BLOCK_POI);
        event.register(ModDataMaps.SOLDIER_ARMOR);
        event.register(ModDataMaps.SOLDIER_VEHICLE_PROPERTIES);
    }
}
