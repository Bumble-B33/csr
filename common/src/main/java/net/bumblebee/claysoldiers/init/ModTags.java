package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final class Items {
        public static final TagKey<Item> SOLDIER_HOLDABLE = create("clay_soldier_holdable");

        public static final TagKey<Item> SOLDIER_WEAPON = create("clay_soldier_weapon");
        public static final TagKey<Item> SOLDIER_ARMOR = create("clay_soldier_armor");
        public static final TagKey<Item> SOLDIER_BOSS_EQUIPABLE = create("clay_boss_equiable");

        public static final TagKey<Item> BASIC = create("clay_soldier_items/basic");
        public static final TagKey<Item> RANGED = create("clay_soldier_items/ranged");
        public static final TagKey<Item> TANK = create("clay_soldier_items/tank");
        public static final TagKey<Item> ARMORED = create("clay_soldier_items/armored");
        public static final TagKey<Item> SPECIALIST = create("clay_soldier_items/specialist");
        public static final TagKey<Item> HEALER = create("clay_soldier_items/supportive");
        public static final TagKey<Item> EXPLOSIVE_EXPERT = create("clay_soldier_items/explosive_export");
        public static final TagKey<Item> DIVER = create("clay_soldier_items/diver");
        public static final TagKey<Item> ARSONIST = create("clay_soldier_items/arsonist");
        public static final TagKey<Item> MAGICIAN = create("clay_soldier_items/magician");
        public static final TagKey<Item> ROYALTY = create("clay_soldier_items/royalty");
        public static final TagKey<Item> FASHION = create("clay_soldier_items/fashion");
        public static final TagKey<Item> KINGDOM = create("clay_soldier_items/kingdom");


        public static final TagKey<Item> CLAY_HORSE_ARMOR = create("clay_horse_armor");
        public static final TagKey<Item> SOLDIER_RGB_GLASSES = create("soldier_rgb_glasses");
        public static final TagKey<Item> CLAY_FOOD = create("clay_food");
        public static final TagKey<Item> CLAY_WAX = create("clay_wax");

        public static final TagKey<Item> SOLDIER_POI = create("soldier_poi");

        public static final TagKey<Item> SOLDIER_SLINGSHOT_ENCHANTABLE = create("enchantable/clay_soldier_slingshot");

        public static final TagKey<Item> DYES = commonTag("dyes");
        public static final TagKey<Item> GLASS_PANES = commonTag("glass_panes");
        public static final TagKey<Item> GLASS_PANES_COLORLESS = commonTag("glass_panes/colorless");
        public static final TagKey<Item> GAME_MASTER_ITEM = commonTag("game_master_items");
        public static final TagKey<Item> INGOTS_COPPER = commonTag("ingots/copper");

        public static final TagKey<Item> WRENCH = commonTag("tools/wrench");

        public static final TagKey<Item> ACCESSORIES_FACE = create(ExternalMods.ACCESSORIES.getName(), "face");
        public static final TagKey<Item> CURIOS_HEAD = create(ExternalMods.CURIOS.getName(), "head");

        private static TagKey<Item> create(String location) {
            return create(ClaySoldiersCommon.MOD_ID, location);
        }
        private static TagKey<Item> commonTag(String name) {
            return create("c", name);
        }
        private static TagKey<Item> create(String modId, String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(modId, name));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> BLUEPRINT_BLACK_LISTED = create("blueprint_blacklisted");

        private static TagKey<Block> create(String location) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, location));
        }
    }

    public static final class DamageTypes {
        public static final TagKey<DamageType> CLAY_SOLDIER_DAMAGE = create("clay_soldier_damage");

        private static TagKey<DamageType> create(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        }
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> CLAY_BOSS = create("clay_boss");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        }
    }

    public static final class Enchantments {
        public static final TagKey<Enchantment> SOLDIER_SLINGSHOT_EXCLUSIVE = create("clay_soldier_slingshot_exclusive");

        private static TagKey<Enchantment> create(String name) {
            return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        }
    }

    public static final class SoldierPropertyTypes {
        public static final TagKey<SoldierPropertyType<?>> REQUIRES_OWNER = create("requires_owner");

        private static TagKey<SoldierPropertyType<?>> create(String name) {
            return TagKey.create(ModRegistries.SOLDIER_PROPERTY_TYPES, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        }
    }

    public static final class PoiTypes {
        public static final TagKey<PoiType> SOLDIER_CONTAINER = create("soldier_container");

        private static TagKey<PoiType> create(String name) {
            return TagKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        }
    }
}
