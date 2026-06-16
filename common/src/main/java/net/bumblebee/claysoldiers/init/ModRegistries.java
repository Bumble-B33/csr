package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctionSerializer;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicateSerializer;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.soldieritemtypes.ItemGenerator;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackSerializer;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;

public final class ModRegistries {
    public static final ResourceKey<Registry<ClayPredicateSerializer<?>>> CLAY_PREDICATE_SERIALIZERS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_predicate_serializers"));
    public static final Registry<ClayPredicateSerializer<?>> CLAY_SOLDIER_PREDICATE_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(CLAY_PREDICATE_SERIALIZERS, true);

    public static final ResourceKey<Registry<SpecialAttackSerializer<?>>> SPECIAL_ATTACK_SERIALIZERS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "special_attack_serializers"));
    public static final Registry<SpecialAttackSerializer<?>> SPECIAL_ATTACK_SERIALIZERS_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(SPECIAL_ATTACK_SERIALIZERS, true);

    public static final ResourceKey<Registry<ClayPoiFunctionSerializer<?>>> CLAY_POI_FUNCTION_SERIALIZERS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_poi_function_serializer"));
    public static final Registry<ClayPoiFunctionSerializer<?>> CLAY_POI_FUNCTION_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(CLAY_POI_FUNCTION_SERIALIZERS, true);

    public static final ResourceKey<Registry<SoldierPropertyType<?>>> SOLDIER_PROPERTY_TYPES =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_property_types"));
    public static final Registry<SoldierPropertyType<?>> SOLDIER_PROPERTY_TYPES_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(SOLDIER_PROPERTY_TYPES, true);

    public static final ResourceKey<Registry<ItemGenerator>> ITEM_GENERATORS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item_generators"));
    public static final Registry<ItemGenerator> ITEM_GENERATORS_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(ITEM_GENERATORS, false);

    public static final ResourceKey<Registry<BossClaySoldierBehaviour>> BOSS_CLAY_SOLDIER_BEHAVIOURS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "boss_clay_soldier_behaviours"));
    public static final Registry<BossClaySoldierBehaviour> BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(BOSS_CLAY_SOLDIER_BEHAVIOURS, true);

    public static final ResourceKey<Registry<SoldierItemType>> SOLDIER_ITEM_TYPES =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_item_type"));

    public static final ResourceKey<Registry<BlueprintData>> BLUEPRINTS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprints"));

    public static final ResourceKey<Registry<ClayMobTeam>> CLAY_MOB_TEAMS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_teams"));

    public static final ResourceKey<Registry<ClaySoldierChip.Type<?>>> CLAY_SOLDIER_MODULES =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_modules"));
    public static final Registry<ClaySoldierChip.Type<?>> CLAY_SOLDIER_MODULES_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(CLAY_SOLDIER_MODULES, true);

    public static final ResourceKey<Registry<ClaySoldierChipAddon>> CLAY_SOLDIER_CHIP_ADDONS =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_chip_addons"));
    public static final Registry<ClaySoldierChipAddon> CLAY_SOLDIER_CHIP_ADDONS_REGISTRY = ClaySoldiersCommon.PLATFORM.createRegistry(CLAY_SOLDIER_CHIP_ADDONS, true);


    public static void register(Consumer<Registry<?>> event) {
        event.accept(ModRegistries.CLAY_SOLDIER_PREDICATE_REGISTRY);
        event.accept(ModRegistries.SPECIAL_ATTACK_SERIALIZERS_REGISTRY);
        event.accept(ModRegistries.CLAY_POI_FUNCTION_REGISTRY);
        event.accept(ModRegistries.SOLDIER_PROPERTY_TYPES_REGISTRY);
        event.accept(ModRegistries.ITEM_GENERATORS_REGISTRY);
        event.accept(ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS_REGISTRY);
        event.accept(ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY);
        event.accept(ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY);
    }

    public static void registerDynamicRegistry(ClaySoldiersCommon.DynamicRegistryEvent event) {
        event.register(ModRegistries.BLUEPRINTS, BlueprintData.JSON_CODEC, BlueprintData.JSON_CODEC);
        event.register(ModRegistries.SOLDIER_ITEM_TYPES, SoldierItemType.CODEC, SoldierItemType.SYNC_CODEC, ((id, location, value) -> value.onRegister(location)));
        event.register(ModRegistries.CLAY_MOB_TEAMS, ClayMobTeam.CODEC_JSON, ClayMobTeam.CODEC_JSON,
                (id, key, value) -> ClayMobTeamManger.appendFromItemMap(value.getGetFrom(), key));
    }

    private ModRegistries() {
    }
}
