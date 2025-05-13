package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.entity.variant.NameableVariant;
import net.bumblebee.claysoldiers.item.BrickedClaySoldierItem;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.TestItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintPageItem;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchItem;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffItem;
import net.bumblebee.claysoldiers.item.disruptor.ClayMobKillItem;
import net.bumblebee.claysoldiers.item.disruptor.DisruptorKillRange;
import net.bumblebee.claysoldiers.platform.ItemLikeSupplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.Map;
import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<Item> SHEAR_BLADE = ClaySoldiersCommon.PLATFORM.registerItem("shear_blade",
            properties -> new SwordItem(Tiers.STONE, properties), new Item.Properties().attributes(SwordItem.createAttributes(Tiers.STONE, 3, -2.4F)));
    public static final Supplier<Item> SHARPENED_STICK = ClaySoldiersCommon.PLATFORM.registerItem("sharpened_stick",
            properties -> new SwordItem(Tiers.WOOD, properties), new Item.Properties().attributes(SwordItem.createAttributes(Tiers.WOOD, 3, -2.4F)));

    public static final Supplier<BrickedClaySoldierItem> BRICKED_CLAY_SOLDIER = ClaySoldiersCommon.PLATFORM.registerItem("bricked_clay_soldier",
            BrickedClaySoldierItem::new, new Item.Properties());
    public static final ItemLikeSupplier<ClaySoldierSpawnItem> CLAY_SOLDIER = ClaySoldiersCommon.PLATFORM.registerItem("clay_soldier",
            ClaySoldierSpawnItem::new, new Item.Properties());

    public static final ItemLikeSupplier<Item> CLAY_DISRUPTOR = ClaySoldiersCommon.PLATFORM.registerItem("clay_disruptor",
            properties -> new ClayMobKillItem(properties, DisruptorKillRange.range(16f)), new Item.Properties().stacksTo(1));
    public static final Supplier<Item> TERRACOTTA_DISRUPTOR = ClaySoldiersCommon.PLATFORM.registerItem("terracotta_disruptor",
            properties -> new ClayMobKillItem(properties, DisruptorKillRange.unlimited()), new Item.Properties().stacksTo(1));

    public static final Supplier<Item> CLAY_COOKIE = ClaySoldiersCommon.PLATFORM.registerItem("clay_cookie",
            Item::new, new Item.Properties());
    public static final Supplier<ClayBrushItem> CLAY_BRUSH = ClaySoldiersCommon.PLATFORM.registerItem("clay_brush",
            ClayBrushItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final ItemLikeSupplier<ArmorItem> CLAY_GOGGLES = ClaySoldiersCommon.PLATFORM.registerItem("clay_goggles",
            properties -> new ArmorItem(ModArmorMaterials.CLAY_ARMOR_MATERIAL, ArmorItem.Type.HELMET, properties), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final ItemLikeSupplier<Item> TEST_ITEM = ClaySoldiersCommon.PLATFORM.ifDevEv(() -> ClaySoldiersCommon.PLATFORM.registerItem("debug_device",
            TestItem::new, new Item.Properties().stacksTo(1)), ItemLikeSupplier.EMPTY);

    public static final Supplier<Item> BLUEPRINT = ClaySoldiersCommon.PLATFORM.registerItem("blueprint",
            BlueprintItem::new, new Item.Properties().stacksTo(1));

    public static final ItemLikeSupplier<Item> BLUEPRINT_PAGE = ClaySoldiersCommon.PLATFORM.registerItem("blueprint_page",
            BlueprintPageItem::new, new Item.Properties());

    public static final ItemLikeSupplier<ClayStaffItem> CLAY_STAFF = ClaySoldiersCommon.PLATFORM.registerItem("clay_staff",
            ClayStaffItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final ItemLikeSupplier<ClayPouchItem> CLAY_POUCH = ClaySoldiersCommon.PLATFORM.registerItem("clay_pouch",
            ClayPouchItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));


    public static final Supplier<? extends Item> CAKE_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.CAKE, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> GRASS_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.GRASS, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> SNOW_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.SNOW, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> MYCELIUM_HORSE = registerSpawnItemVariant("horse", ModEntityTypes.CLAY_HORSE_ENTITY, ClayHorseVariants.MYCELIUM, ClayHorseVariants.CLAY_HORSE_ITEM_BY_VARIANT);

    public static final Supplier<? extends Item> CAKE_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.CAKE, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> GRASS_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.GRASS, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> SNOW_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.SNOW, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);
    public static final Supplier<? extends Item> MYCELIUM_PEGASUS = registerSpawnItemVariant("pegasus", ModEntityTypes.CLAY_PEGASUS_ENTITY, ClayHorseVariants.MYCELIUM, ClayHorseVariants.CLAY_PEGASUS_ITEM_BY_VARIANT);

    private static <V extends NameableVariant, T extends ClayMobEntity & VariantHolder<V>> Supplier<MultiSpawnItem<?>> registerSpawnItemVariant(String postfix, Supplier<EntityType<T>> entityType, V variant, Map<V, Supplier<MultiSpawnItem<?>>> variantItemMap) {
        Supplier<MultiSpawnItem<?>> variantItem = ClaySoldiersCommon.PLATFORM.registerItem(variant.getVariantName() + "_" + postfix,
                properties -> MultiSpawnItem.createClayMob(entityType, variant, properties), new Item.Properties());
        variantItemMap.put(variant, variantItem);
        return variantItem;
    }

    public static ItemStack createEnchantedBook(HolderLookup.Provider registries, ResourceKey<Enchantment> key, int level) {
        return EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), level));
    }

    public static void init() {
    }
}
