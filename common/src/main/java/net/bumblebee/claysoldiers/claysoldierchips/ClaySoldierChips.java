package net.bumblebee.claysoldiers.claysoldierchips;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.work.*;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

import java.util.function.Supplier;

public final class ClaySoldierChips {
    public static final Supplier<ClaySoldierChip.Type<Unit>> EMPTY_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("empty_module", () -> ClaySoldierChip.Type.empty(
            () -> EmptyClaySoldierChip.EMPTY
    ));
    public static final Supplier<ClaySoldierChip.Type<Unit>> USE_POI = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("assignable_poi", () -> ClaySoldierChip.Type.create(
            PoiChip::new,
            PoiChip.ADDON_INFO

    ));
    public static final Supplier<ClaySoldierChip.Type<Unit>> COMBAT_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("combat_module", () -> ClaySoldierChip.Type.create(
            CombatChip::new,
            CombatChip.ADDON_INFO
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

    public static final Supplier<ClaySoldierChip.Type<SearchRange>> BEEKEEPING_TYPE = ClaySoldiersCommon.PLATFORM.registerClaySoldierModule("bee_keeping", () -> new ClaySoldierChip.Type<>(
            BeeKeepingChip::new,
            BeeKeepingChip.ADDON_INFO,
            SearchRange.CODEC.fieldOf("search_range"),
            SearchRange.STREAM_CODEC.cast()
    ));


    public static void init() {

    }
}
