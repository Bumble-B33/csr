package net.bumblebee.claysoldiers.claysoldierchips.addon;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ClaySoldierChipAddons {
    public static final ClaySoldierChipAddon RANGE_ADDON = register("range", ModItems.RANGE_ADDON);
    public static final ClaySoldierChipAddon NO_BREAK_ADDON = register("no_break", ModItems.NO_BREAK_ADDON);
    public static final ClaySoldierChipAddon FISH_TREASURE_ADDON = register("fish_treasure", ModItems.TREASURY_ADDON);

    public static final ClaySoldierChipAddon TARGET_MONSTER_ADDON = register("target_monsters", ModItems.TARGET_MONSTER_ADDON);
    public static final ClaySoldierChipAddon TARGET_ANIMALS_ADDON = register("target_animals", ModItems.TARGET_ANIMAL_ADDON);
    public static final ClaySoldierChipAddon TARGET_IGNORE_BABIES_ADDON = register("target_ignore_babies", ModItems.TARGET_IGNORE_BABIES_ADDON);

    public static final ClaySoldierChipAddon ACCELERATION_ADDON = register("acceleration", ModItems.ACCELERATION_ADDON, () -> DataComponentMap.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON_ACCELERATION.get(), 1).build());
    public static final ClaySoldierChipAddon UPGRADED_ACCELERATION_ADDON = register("upgraded_acceleration", ModItems.UPGRADED_ACCELERATION_ADDON, () -> DataComponentMap.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON_ACCELERATION.get(), 2).build());


    private static ClaySoldierChipAddon register(String name, Supplier<Item> item) {
        return ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon(name, new ClaySoldierChipAddon(name));
    }

    private static ClaySoldierChipAddon register(String name, Supplier<Item> item, Supplier<DataComponentMap> dataComponents) {
        return ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon(name, new ClaySoldierChipAddon(name, dataComponents));
    }

    public static void init() {

    }
}
