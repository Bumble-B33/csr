package net.bumblebee.claysoldiers.entity.common;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface StatInfoDisplay {
    String HAMSTER_WHEEL_LANG_BASE = "statinfo.%s.hamster_wheel.";
    String CLAY_MOB_LANG_BASE = "statinfo.%s.clay_mob.";
    String CLAY_SOLDIER_LANG_BASE = "statinfo.%s.clay_soldier.";
    String PROGRAMMABLE_CLAY_SOLDIER_LANG_BASE = "statinfo.%s.programmable_clay_soldier.";


    String ENERGY_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("energy");
    String SPEED_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("speed");
    String GENERATION_LANG = HAMSTER_WHEEL_LANG_BASE.formatted("generating");

    String HEALTH = CLAY_MOB_LANG_BASE.formatted("health");
    String ARMOR = CLAY_MOB_LANG_BASE.formatted("armor");
    String TEAM = CLAY_MOB_LANG_BASE.formatted("team");
    String OWNER = CLAY_MOB_LANG_BASE.formatted("owner");

    String DAMAGE = CLAY_SOLDIER_LANG_BASE.formatted("damage");
    String STATUS = CLAY_SOLDIER_LANG_BASE.formatted("status");

    String MODULE = PROGRAMMABLE_CLAY_SOLDIER_LANG_BASE.formatted("module");


    void getStatDisplay(List<Component> list, LivingEntity viewer);
}
