package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class ClayMobWorkStatusManger {
    private static final byte MAX_STATUSES = Byte.MAX_VALUE;
    private static final ArrayList<Component> STATUSES = new ArrayList<>();
    public static final String STATUS_LANG_KEY = "clay_soldier_work.%s.status.%s";

    public static final String BREAK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "on_break");
    public static final String SEARCHING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "searching_item");
    public static final String CARRYING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "carrying");
    public static final String REQUIRES_POI_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "requires_poi");
    public static final String RETURNING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "returning");
    public static final String CANNOT_FIND_ITEM_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "cannot_find_item");
    public static final String IS_ANKER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.anker");
    public static final String SEARCHING_FOR_WATER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.searching_water");
    public static final String SEARCHING_ANKER_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.searching_anker");
    public static final String FISHING_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "fish.fishing");
    public static final String EXTRACTING_ENERGY_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician.extracting");
    public static final String INSERTING_ENERGY_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician.inserting");
    public static final String NEEDS_BATTERY_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician.needs_battery");
    public static final String NOTHING_TO_CHARGE_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician.needs_poi");
    public static final String NO_CHARGING_PAD_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician.no_charging_pad");
    public static final String BREAKING_BLOCK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig.breaking");
    public static final String UNBREAKABLE_BLOCK_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig.unbreakable_block");
    public static final String BLOCK_BREAK_DISALLOWED = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "dig.breaking_disallowed");
    public static final String BREAK_CROPS_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "harvest.breaking_crops");
    public static final String CROP_BREAK_DISALLOWED = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "harvest.breaking_disallowed");
    public static final String NO_CHEST_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "pick_up_items.no_chest");
    public static final String NEEDS_SHEARS_OR_BOTTLE_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "beekeeping.requires_tools");
    public static final String NO_HIVE_LANG = STATUS_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "beekeeping.no_hive");


    public static final byte BREAK_ID = register(BREAK_LANG);
    public static final byte SEARCHING_ID = register(SEARCHING_LANG);
    public static final byte CARRYING_ID = register(CARRYING_LANG);
    public static final byte REQUIRES_POI_ID = register(REQUIRES_POI_LANG);
    public static final byte RETURNING_ID = register(RETURNING_LANG);
    public static final byte CANNOT_FIND_ITEM_ID = register(CANNOT_FIND_ITEM_LANG);
    public static final byte IS_ANKER_ID = register(IS_ANKER_LANG);
    public static final byte SEARCHING_FOR_WATER_ID = register(SEARCHING_FOR_WATER_LANG);
    public static final byte SEARCHING_ANKER_ID = register(SEARCHING_ANKER_LANG);
    public static final byte FISHING_ID = register(FISHING_LANG);
    public static final byte EXTRACTING_ID = register(EXTRACTING_ENERGY_LANG);
    public static final byte INSERTING_ID = register(INSERTING_ENERGY_LANG);
    public static final byte NEEDS_BATTERY_ID = register(NEEDS_BATTERY_LANG);
    public static final byte NOTHING_TO_CHARGE_ID = register(NOTHING_TO_CHARGE_LANG);
    public static final byte NO_CHARGING_PAD_ID = register(NO_CHARGING_PAD_LANG);
    public static final byte BREAKING_ID = register(BREAKING_BLOCK_LANG);
    public static final byte UNBREAKABLE_ID = register(UNBREAKABLE_BLOCK_LANG);
    public static final byte CANNOT_BREAK_BLOCKS_ID = register(BLOCK_BREAK_DISALLOWED);
    public static final byte BREAKING_CROP_ID = register(BREAK_CROPS_LANG);
    public static final byte CANT_BREAK_CROP_ID = register(CROP_BREAK_DISALLOWED);
    public static final byte NO_CHEST_ID = register(NO_CHEST_LANG);
    public static final byte BEEKEEPING_REQUIRES_TOOLS_ID = register(NEEDS_SHEARS_OR_BOTTLE_LANG);
    public static final byte NO_HIVE_ID = register(NO_HIVE_LANG);

    static {
        STATUSES.trimToSize();
    }

    private static byte register(@NonNull Component status) {
        if (STATUSES.size() > MAX_STATUSES) {
            throw new IllegalStateException("Cannot have more than %s Statuses".formatted(MAX_STATUSES));
        }
        STATUSES.add(status);
        return (byte) (STATUSES.size() - 1);
    }

    public static byte register(@NonNull String status) {
        return register(Component.translatable(status));
    }

    public static @Nullable Component get(byte id) {
        return id < 0 ? null : STATUSES.get(id);
    }
}
