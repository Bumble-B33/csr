package net.bumblebee.claysoldiers.claysoldierchips.addon;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;

public class ClaySoldierChipAddons {
    public static final ClaySoldierChipAddon RANGE_ADDON = register("range");
    public static final ClaySoldierChipAddon NO_BREAK_ADDON = register("no_break");
    public static final ClaySoldierChipAddon FISH_TREASURE_ADDON = register("fish_treasure");

    public static final ClaySoldierChipAddon TARGET_MONSTER_ADDON = register("target_monsters");
    public static final ClaySoldierChipAddon TARGET_ANIMALS_ADDON = register("target_animals");
    public static final ClaySoldierChipAddon TARGET_IGNORE_BABIES_ADDON = register("target_ignore_babies");

    public static final ClaySoldierChipAddon ACCELERATION_ADDON = register("acceleration");

    private static ClaySoldierChipAddon register(String name) {
        return ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon(name, new ClaySoldierChipAddon(name));
    }

    public static void init() {

    }
}
