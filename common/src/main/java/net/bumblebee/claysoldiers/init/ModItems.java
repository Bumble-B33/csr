package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.CombatChip;
import net.bumblebee.claysoldiers.claysoldierchips.EmptyClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.PoiChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.claysoldierchips.work.*;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.entity.common.variant.NameableVariant;
import net.bumblebee.claysoldiers.entity.common.variant.VariantHolder;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.bumblebee.claysoldiers.item.BrickedClaySoldierItem;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchItem;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffItem;
import net.bumblebee.claysoldiers.item.disruptor.ClayMobKillItem;
import net.bumblebee.claysoldiers.item.disruptor.DisruptorKillRange;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<Item> SHEAR_BLADE = ClaySoldiersCommon.PLATFORM.registerItem("shear_blade",
            p -> new Item(p.sword(ToolMaterial.STONE, 3f, -2.4f)));
    public static final Supplier<Item> SHARPENED_STICK = ClaySoldiersCommon.PLATFORM.registerItem("sharpened_stick",
            p -> new Item(p.sword(ToolMaterial.WOOD, 3f, -2.4f)));

    public static final ItemLikeSupplier<BrickedClaySoldierItem> BRICKED_CLAY_SOLDIER = ClaySoldiersCommon.PLATFORM.registerItem("bricked_clay_soldier",
            p -> new BrickedClaySoldierItem(p.component(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), ClayMobTeamManger.DEFAULT_KEY)));
    public static final ItemLikeSupplier<ClaySoldierSpawnItem> CLAY_SOLDIER = ClaySoldiersCommon.PLATFORM.registerItem("clay_soldier",
            p -> new ClaySoldierSpawnItem(p.component(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), ClayMobTeamManger.DEFAULT_KEY)));

    public static final ItemLikeSupplier<Item> CLAY_DISRUPTOR = ClaySoldiersCommon.PLATFORM.registerItem("clay_disruptor",
            properties -> new ClayMobKillItem(properties.component(ModDataComponents.DISRUPTOR_KILL_RANGE.get(), DisruptorKillRange.range(16f)).durability(127).repairable(Items.IRON_INGOT).enchantable(1)));
    public static final Supplier<Item> TERRACOTTA_DISRUPTOR = ClaySoldiersCommon.PLATFORM.registerItem("terracotta_disruptor",
            properties -> new ClayMobKillItem(properties.component(ModDataComponents.DISRUPTOR_KILL_RANGE.get(), DisruptorKillRange.unlimited()).durability(255).repairable(Items.IRON_INGOT).enchantable(1)));

    public static final ItemLikeSupplier<Item> CLAY_COOKIE = ClaySoldiersCommon.PLATFORM.registerItem("clay_cookie",
            Item::new);
    public static final Supplier<ClayBrushItem> CLAY_BRUSH = ClaySoldiersCommon.PLATFORM.registerItem("clay_brush",
            p -> new ClayBrushItem(p.stacksTo(1).rarity(Rarity.UNCOMMON).component(ModDataComponents.CLAY_BRUSH_MODE.get(), ClayBrushItem.Mode.COMMAND).component(ModDataComponents.POI_POS.get(), ClayBrushItem.PoiPos.EMPTY)));
    public static final ItemLikeSupplier<Item> CLAY_GOGGLES = ClaySoldiersCommon.PLATFORM.registerItem("clay_goggles",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.CLAY_ARMOR_MATERIAL, ArmorType.HELMET).rarity(Rarity.UNCOMMON)));

    public static final ItemLikeSupplier<Item> SLIME_BOOTS = ClaySoldiersCommon.PLATFORM.registerItem("slime_boots",
            p -> new Item(p.humanoidArmor(ModArmorMaterials.CLAY_ARMOR_MATERIAL, ArmorType.BOOTS).rarity(Rarity.UNCOMMON)));

    public static final ItemLikeSupplier<Item> STATOMETER = ClaySoldiersCommon.PLATFORM.registerItem("statometer",
            p -> new Item(p.stacksTo(1)));


    public static final ItemLikeSupplier<Item> TEST_ITEM = ClaySoldiersCommon.PLATFORM.ifDevEv(() -> ClaySoldiersCommon.PLATFORM.registerItem("debug_device",
            p -> new TestItem(p.stacksTo(1))), ItemLikeSupplier.EMPTY);

    public static final Supplier<Item> BLUEPRINT = ClaySoldiersCommon.PLATFORM.registerItem("blueprint",
            p -> new BlueprintItem(p.stacksTo(1)));

    public static final ItemLikeSupplier<Item> BLUEPRINT_PAGE = ClaySoldiersCommon.PLATFORM.registerItem("blueprint_page", Item::new);

    public static final ItemLikeSupplier<ClayStaffItem> CLAY_STAFF = ClaySoldiersCommon.PLATFORM.registerItem("clay_staff",
            p -> new ClayStaffItem(p.stacksTo(1).rarity(Rarity.RARE).enchantable(1)));

    public static final ItemLikeSupplier<ClayPouchItem> CLAY_POUCH = ClaySoldiersCommon.PLATFORM.registerItem("clay_pouch",
            p -> new ClayPouchItem(p.stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final ItemLikeSupplier<ClaySoldierChipItem> BLANK_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("clay_soldier_chip",
            p -> createChip(p, EmptyClaySoldierChip.EMPTY));

    public static final ItemLikeSupplier<ClaySoldierChipItem> POI_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("poi_clay_soldier_chip",
            p -> createChip(p, PoiChip.create()));
    public static final ItemLikeSupplier<ClaySoldierChipItem> COMBAT_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("animal_combat_clay_soldier_chip",
            p -> createChip(p, CombatChip.create()));
    public static final ItemLikeSupplier<ClaySoldierChipItem> DIG_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("dig_clay_soldier_chip",
            p -> createChip(p, DigChip.create()));
    public static final ItemLikeSupplier<ClaySoldierChipItem> PICK_UP_ITEMS_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("pick_up_items_clay_soldier_chip",
            p -> createChip(p, PickUpItemsChip.create(new SearchRange(8))));

    public static final ItemLikeSupplier<ClaySoldierChipItem> PLACE_SEEDS_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("place_seed_clay_soldier_chip",
            p -> createChip(p, PlaceSeedsChip.create(new SearchRange(16, 2))));

    public static final ItemLikeSupplier<ClaySoldierChipItem> BREAK_CROPS_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("break_crops_clay_soldier_chip",
            p -> createChip(p, BreakCropsChip.create(new SearchRange(16, 2))));

    public static final ItemLikeSupplier<ClaySoldierChipItem> FISHING_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("fishing_clay_soldier_chip",
            p -> createChip(p, FishingChip.create(new SearchRange(16, 2))));
    public static final ItemLikeSupplier<ClaySoldierChipItem> BLUEPRINT_CHIP = ClaySoldiersCommon.PLATFORM.registerItem("build_blueprint_clay_soldier_chip",
            p -> createChip(p, BlueprintChip.create(new SearchRange(16, 2))));



    public static final ItemLikeSupplier<Item> BLANK_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("blank_addon", Item::new);

    public static final ItemLikeSupplier<Item> RANGE_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("range_addon",
            p -> createAddon(p, ClaySoldierChipAddons.RANGE_ADDON));
    public static final ItemLikeSupplier<Item> NO_BREAK_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("no_break_addon",
            p -> createAddon(p, ClaySoldierChipAddons.NO_BREAK_ADDON));
    public static final ItemLikeSupplier<Item> TREASURY_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("treasury_addon",
            p -> createAddon(p, ClaySoldierChipAddons.FISH_TREASURE_ADDON));
    public static final ItemLikeSupplier<Item> TARGET_ANIMAL_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("target_animals_addon",
            p -> createAddon(p, ClaySoldierChipAddons.TARGET_ANIMALS_ADDON));
    public static final ItemLikeSupplier<Item> TARGET_MONSTER_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("target_monsters_addon",
            p -> createAddon(p, ClaySoldierChipAddons.TARGET_MONSTER_ADDON));
    public static final ItemLikeSupplier<Item> TARGET_IGNORE_BABIES_ADDON = ClaySoldiersCommon.PLATFORM.registerItem("target_ignore_babies_addon",
            p -> createAddon(p, ClaySoldierChipAddons.TARGET_IGNORE_BABIES_ADDON));

    public static final Supplier<? extends MultiSpawnItem<?>> CAKE_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.CAKE, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> GRASS_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.GRASS, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> SNOW_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.SNOW, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> MYCELIUM_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.MYCELIUM, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);

    public static final Supplier<? extends MultiSpawnItem<?>> CAKE_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.CAKE, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> GRASS_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.GRASS, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> SNOW_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.SNOW, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends MultiSpawnItem<?>> MYCELIUM_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.MYCELIUM, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);

    private static <V extends NameableVariant, T extends ClayMobEntity & VariantHolder<V>> Supplier<MultiSpawnItem<?>> registerSpawnItemVariant(String postfix, Supplier<EntityType<T>> entityType, V variant, Map<V, Supplier<MultiSpawnItem<?>>> variantItemMap) {
        Supplier<MultiSpawnItem<?>> variantItem = ClaySoldiersCommon.PLATFORM.registerItem(variant.getVariantName() + "_" + postfix,
                properties -> MultiSpawnItem.createClayMob(entityType, variant, properties));
        variantItemMap.put(variant, variantItem);
        return variantItem;
    }

    public static ItemStack createEnchantedBook(HolderLookup.Provider registries, ResourceKey<Enchantment> key, int level) {
        return EnchantmentHelper.createBook(new EnchantmentInstance(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), level));
    }

    public static Item createAddon(Item.Properties properties, @NotNull ClaySoldierChipAddon addon) {
        return new Item(properties.stacksTo(16).component(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON.get(), addon));
    }

    public static ClaySoldierChipItem createChip(Item.Properties properties, @NotNull ClaySoldierChip<?> chip) {
        return new ClaySoldierChipItem(properties.stacksTo(16).component(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip));
    }

    public static void init() {
    }
}
