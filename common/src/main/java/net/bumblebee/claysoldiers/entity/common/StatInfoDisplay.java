package net.bumblebee.claysoldiers.entity.common;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface StatInfoDisplay {
    String HAMSTER_WHEEL_LANG_BASE = "statinfo.%s.hamster_wheel.";
    String CLAY_MOB_LANG_BASE = "statinfo.%s.clay_mob.";
    String CLAY_SOLDIER_LANG_BASE = "statinfo.%s.clay_soldier.";
    String PROGRAMMABLE_CLAY_SOLDIER_LANG_BASE = "statinfo.%s.programmable_clay_soldier.";
    String CHIP_ASSEMBLER_LANG_BASE = "statinfo.%s.chip_assembler.";
    String EASEL_LANG_BASE = "statinfo.%s.easel.";


    String ENERGY_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("energy");
    String SPEED_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("speed");
    String GENERATION_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("generating");

    String ENERGY = CHIP_ASSEMBLER_LANG_BASE.formatted("energy");
    String PROGRESS = CHIP_ASSEMBLER_LANG_BASE.formatted("progress");

    String NO_STRUCTURE = EASEL_LANG_BASE.formatted("structure.empty");
    String STRUCTURE = EASEL_LANG_BASE.formatted("structure");
    String BLUEPRINT_SETTINGS = EASEL_LANG_BASE.formatted("settings");
    String ITEMS_NEEDED = EASEL_LANG_BASE.formatted("items_needed");

    String MIRROR_NONE = EASEL_LANG_BASE.formatted("mirror.none");
    String MIRROR_LEFT_RIGHT = EASEL_LANG_BASE.formatted("mirror.left_rigth");
    String MIRROR_FRONT_BACK = EASEL_LANG_BASE.formatted("mirror.front_back");



    String HEALTH = CLAY_MOB_LANG_BASE.formatted("health");
    String ARMOR = CLAY_MOB_LANG_BASE.formatted("armor");
    String TEAM = CLAY_MOB_LANG_BASE.formatted("team");
    String OWNER = CLAY_MOB_LANG_BASE.formatted("owner");

    String DAMAGE = CLAY_SOLDIER_LANG_BASE.formatted("damage");
    String STATUS = CLAY_SOLDIER_LANG_BASE.formatted("status");

    String MODULE = PROGRAMMABLE_CLAY_SOLDIER_LANG_BASE.formatted("module");
    String FISHING = PROGRAMMABLE_CLAY_SOLDIER_LANG_BASE.formatted("fishing");



    void getStatDisplay(List<Component> list, LivingEntity viewer);
}
