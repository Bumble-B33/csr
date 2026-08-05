package net.bumblebee.claysoldiers.datamap.horse;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ClayHorseItemMap {
    private static final Map<Item, ClayHorseWearableProperties> MAP = new HashMap<>();

    static {
        MAP.put(Items.LEATHER, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(3).setArmor(EquipmentAssets.LEATHER).build());
        MAP.put(Items.IRON_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(7).setArmor(EquipmentAssets.IRON).build());
        MAP.put(Items.COPPER_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(10).setArmor(EquipmentAssets.COPPER).build());
        MAP.put(Items.GOLD_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(15).setArmor(EquipmentAssets.GOLD).build());
        MAP.put(Items.DIAMOND, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(20).setArmor(EquipmentAssets.DIAMOND).build());
        MAP.put(Items.GOAT_HORN, ClayHorseWearableProperties.of(ClayHorseSlot.HORN).build());
    }

    public static void forEach(Consumer<Item> action) {
        MAP.keySet().forEach(action);
    }

    @Nullable
    public static ClayHorseWearableProperties get(Item item) {
        return MAP.get(item);
    }
}
