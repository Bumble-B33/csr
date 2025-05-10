package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> SOLDIER_PROJECTILE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_slingshot"));

    public static void boostrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> enchantmentHolderGetter = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> itemHolderGetter = context.lookup(Registries.ITEM);


        context.register(SOLDIER_PROJECTILE,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemHolderGetter.getOrThrow(ModTags.Items.SOLDIER_SLINGSHOT_ENCHANTABLE),
                                2,
                                1,
                                Enchantment.constantCost(20),
                                Enchantment.constantCost(50),
                                4,
                                EquipmentSlotGroup.ANY
                        )
                ).exclusiveWith(enchantmentHolderGetter.get(ModTags.Enchantments.SOLDIER_SLINGSHOT_EXCLUSIVE).orElseThrow()).build(SOLDIER_PROJECTILE.location()));
    }
}
