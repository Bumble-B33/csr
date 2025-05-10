package net.bumblebee.claysoldiers.datamap.horse;

import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClayHorseItemMap {
    private static final Map<Item, ClayHorseWearableProperties> MAP = new HashMap<>();

    static {
        MAP.put(Items.LEATHER, new ClayHorseWearableProperties(3, Items.LEATHER_HORSE_ARMOR, ColorHelper.EMPTY));
        MAP.put(Items.GOLD_INGOT, new ClayHorseWearableProperties(7, Items.GOLDEN_HORSE_ARMOR, ColorHelper.EMPTY));
        MAP.put(Items.IRON_INGOT, new ClayHorseWearableProperties(10, Items.IRON_HORSE_ARMOR, ColorHelper.EMPTY));
        MAP.put(Items.DIAMOND, new ClayHorseWearableProperties(20, Items.DIAMOND_HORSE_ARMOR, ColorHelper.EMPTY));
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
