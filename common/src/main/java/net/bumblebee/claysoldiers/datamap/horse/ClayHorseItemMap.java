package net.bumblebee.claysoldiers.datamap.horse;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClayHorseItemMap {
    private static final Map<Item, ClayHorseWearableProperties> MAP = new HashMap<>();

    static {
        MAP.put(Items.LEATHER, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(3).armorItem(Items.LEATHER_HORSE_ARMOR).build());
        MAP.put(Items.IRON_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(7).armorItem(Items.IRON_HORSE_ARMOR).build());
        MAP.put(Items.COPPER_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(10).armorItem(Items.COPPER_HORSE_ARMOR).build());
        MAP.put(Items.GOLD_INGOT, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(15).armorItem(Items.GOLDEN_HORSE_ARMOR).build());
        MAP.put(Items.DIAMOND, ClayHorseWearableProperties.of(ClayHorseSlot.ARMOR).protection(20).armorItem(Items.DIAMOND_HORSE_ARMOR).build());
        MAP.put(Items.GOAT_HORN, ClayHorseWearableProperties.of(ClayHorseSlot.HORN).build());
    }

    @Nullable
    public static ClayHorseWearableProperties get(Item item) {
        return MAP.get(item);
    }
    @Nullable
    public static ClayHorseWearableProperties get(ItemStack stack) {
        return MAP.get(stack.getItem());
    }
}
