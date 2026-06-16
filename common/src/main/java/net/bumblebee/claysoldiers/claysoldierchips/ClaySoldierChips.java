package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.work.*;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

import java.util.function.Supplier;

public final class ClaySoldierChips {
    public static final Supplier<ClaySoldierChip.Type<Unit>> EMPTY_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("empty_module", () -> ClaySoldierChip.Type.empty(
            () -> EmptyClaySoldierChip.EMPTY
    ));
    public static final Supplier<ClaySoldierChip.Type<Unit>> USE_POI = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("assignable_poi", () -> ClaySoldierChip.Type.empty(
            () -> PoiChip.INSTANCE
    ));
    public static final Supplier<ClaySoldierChip.Type<Integer>> COMBAT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("combat_module", () -> new ClaySoldierChip.Type<>(
            CombatChip::new,
            CombatChip.ADDON_INFO,
            ExtraCodecs.POSITIVE_INT.fieldOf("target_range"),
            CombatChip.STREAM_CODEC
    ));

    public static final Supplier<ClaySoldierChip.Type<SearchRange>> PICK_UP_ITEMS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("pick_up_items_module", () -> new ClaySoldierChip.Type<>(
            PickUpItemsChip::new,
            PickUpItemsChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));
    public static final Supplier<ClaySoldierChip.Type<SearchRange>> BREAK_CROPS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("break_crops_module", () -> new ClaySoldierChip.Type<SearchRange>(
            BreakCropsChip::new,
            BreakCropsChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));
    public static final Supplier<ClaySoldierChip.Type<SearchRange>> PLACE_SEEDS_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("place_seed_module", () -> new ClaySoldierChip.Type<>(
            PlaceSeedsChip::new,
            PlaceSeedsChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));
    public static final Supplier<ClaySoldierChip.Type<Unit>> DIG_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("dig_module", () -> ClaySoldierChip.Type.empty(
            DigChip::create
    ));
    public static final Supplier<ClaySoldierChip.Type<SearchRange>> FISHING_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("fishing_module", () -> new ClaySoldierChip.Type<>(
            FishingChip::new,
            FishingChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));

    public static final Supplier<ClaySoldierChip.Type<SearchRange>> BUILD_BLUEPRINT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("build_blueprint", () -> new ClaySoldierChip.Type<>(
            BlueprintChip::new,
            BlueprintChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));


    public static void init() {

    }
}
