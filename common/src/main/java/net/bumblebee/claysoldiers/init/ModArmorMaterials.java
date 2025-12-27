package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public final class ModArmorMaterials {
    public static final ResourceKey<EquipmentAsset> CLAY_GOGGLES_ID = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_goggles"));
    public static final ArmorMaterial CLAY_ARMOR_MATERIAL = new ArmorMaterial(
                    5,
                    Util.make(new EnumMap<>(ArmorType.class), p_371485_ -> {
                        p_371485_.put(ArmorType.BOOTS, 2);
                        p_371485_.put(ArmorType.LEGGINGS, 2);
                        p_371485_.put(ArmorType.CHESTPLATE, 3);
                        p_371485_.put(ArmorType.HELMET, 1);
                        p_371485_.put(ArmorType.BODY, 3);
                    }),
                    20,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    0F,
                    0F,
                    ModTags.Items.INGOTS_COPPER,
                    CLAY_GOGGLES_ID
            );

    private ModArmorMaterials() {
    }

    public static void init() {}
}
