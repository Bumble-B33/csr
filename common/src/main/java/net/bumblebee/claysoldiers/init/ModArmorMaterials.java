package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    public static final Holder<ArmorMaterial> CLAY_ARMOR_MATERIAL = ClaySoldiersCommon.PLATFORM.registerArmorMaterial("clay_armor",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), slotId -> {
                        slotId.put(ArmorItem.Type.BOOTS, 1);
                        slotId.put(ArmorItem.Type.LEGGINGS, 2);
                        slotId.put(ArmorItem.Type.CHESTPLATE, 3);
                        slotId.put(ArmorItem.Type.HELMET, 1);
                        slotId.put(ArmorItem.Type.BODY, 3);
                    }),
                    20,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ModTags.Items.INGOTS_COPPER),
                    List.of(
                            new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_goggles"))
                    ),
                    0,
                    0
            ));

    private ModArmorMaterials() {
    }

    public static void init() {}
}
