package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.work.*;

import java.util.function.Supplier;

public final class ClaySoldierChips {
    public static final Supplier<ClaySoldierChip.Type> EMPTY_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("empty_module", () -> ClaySoldierChip.Type.empty(
            () -> EmptyClaySoldierChip.EMPTY
    ));
    public static final Supplier<ClaySoldierChip.Type> USE_POI = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("assignable_poi", () -> new ClaySoldierChip.Type(
            PoiChip::create,
            PoiChip.ADDON_INFO

    ));
    public static final Supplier<ClaySoldierChip.Type> COMBAT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("combat_module", () -> new ClaySoldierChip.Type(
            CombatChip::create,
            CombatChip.ADDON_INFO
    ));

    public static final Supplier<ClaySoldierChip.Type> PICK_UP_ITEMS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("pick_up_items_module", () -> new ClaySoldierChip.Type(
            PickUpItemsChip::create,
            PickUpItemsChip.ADDON_INFO
    ));
    public static final Supplier<ClaySoldierChip.Type> BREAK_CROPS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("break_crops_module", () -> new ClaySoldierChip.Type(
            BreakCropsChip::create,
            BreakCropsChip.ADDON_INFO
    ));
    public static final Supplier<ClaySoldierChip.Type> PLACE_SEEDS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("place_seed_module", () -> new ClaySoldierChip.Type(
            PlaceSeedsChip::create,
            PlaceSeedsChip.ADDON_INFO
    ));
    public static final Supplier<ClaySoldierChip.Type> DIG_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("dig_module", () -> ClaySoldierChip.Type.empty(
            DigChip::create
    ));
    public static final Supplier<ClaySoldierChip.Type> FISHING_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("fishing_module", () -> new ClaySoldierChip.Type(
            FishingChip::create,
            FishingChip.ADDON_INFO
    ));

    public static final Supplier<ClaySoldierChip.Type> BUILD_BLUEPRINT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("build_blueprint", () -> new ClaySoldierChip.Type(
            BlueprintChip::create,
            BlueprintChip.ADDON_INFO
    ));

    public static final Supplier<ClaySoldierChip.Type> BEEKEEPING_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("bee_keeping", () -> new ClaySoldierChip.Type(
            BeeKeepingChip::create,
            BeeKeepingChip.ADDON_INFO
    ));

    public static final Supplier<ClaySoldierChip.Type> ELECTRICIAN_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("electrician", () -> new ClaySoldierChip.Type(
            ElectricianChip::create,
            ElectricianChip.ADDON_INFO
    ));


    public static void init() {

    }
}
