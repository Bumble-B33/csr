package net.bumblebee.claysoldiers.claysoldierchips.addon;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;

public class ClaySoldierChipAddons {
    public static final ClaySoldierChipAddon RANGE_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("range", new ClaySoldierChipAddon("range"));
    public static final ClaySoldierChipAddon NO_BREAK_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("no_break", new ClaySoldierChipAddon("no_break"));
    public static final ClaySoldierChipAddon FISH_TREASURE_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("fish_treasure", new ClaySoldierChipAddon("fish_treasure"));

    public static final ClaySoldierChipAddon TARGET_MONSTER_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("target_monsters", new ClaySoldierChipAddon("target_monster"));
    public static final ClaySoldierChipAddon TARGET_ANIMALS_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("target_animals", new ClaySoldierChipAddon("target_animals"));
    public static final ClaySoldierChipAddon TARGET_IGNORE_BABIES_ADDON = ClaySoldiersCommon.PLATFORM.registerClaySoldierChipAddon("target_ignore_babies", new ClaySoldierChipAddon("target_ignore_babies"));


    public static void init() {

    }
}
