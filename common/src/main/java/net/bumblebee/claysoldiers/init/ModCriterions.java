package net.bumblebee.claysoldiers.init;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.advancements.*;

import java.util.function.Supplier;

public class ModCriterions {
    public static final Supplier<DisruptorKillTrigger> DISRUPTOR_KILL_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("disruptor_kill_trigger",
            DisruptorKillTrigger::new
    );

    public static final Supplier<FeedItemClaySoldierTrigger> FEED_CLAY_SOLDIER_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("wax_clay_mob_trigger",
            FeedItemClaySoldierTrigger::new
    );

    public static final Supplier<ClaySoldierDeathTrigger> CLAY_SOLDIER_DEATH = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("clay_soldier_death",
            ClaySoldierDeathTrigger::new
    );

    public static final Supplier<ClayBrushCommandTrigger> CLAY_BRUSH_COMMAND_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("clay_brush_command",
            ClayBrushCommandTrigger::new
    );

    public static final Supplier<UseAssignedWorksiteTrigger> USE_ASSIGNED_POI_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("work_site_trigger",
            UseAssignedWorksiteTrigger::new
    );

    public static final Supplier<SoldierPoiUseTrigger> SOLDIER_POI_USE_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("soldier_poi_use",
            SoldierPoiUseTrigger::new
    );

    public static final Supplier<HitWithClayBlockTrigger> HIT_WITH_CLAY_BLOCK_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("hit_with_clay_block",
            HitWithClayBlockTrigger::new
    );

    public static final Supplier<MultiSpawnItemUseTrigger> MULTI_SPAWN_ITEM_USE_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("clay_spawn_item_use",
            MultiSpawnItemUseTrigger::new
    );

    public static final Supplier<BlueprintPlaceBlockTrigger> BLUEPRINT_COMPLETION_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("blueprint_completion",
            BlueprintPlaceBlockTrigger::new
    );

    public static final Supplier<ClaySoldierOnHeadTrigger> CLAY_SOLDIER_ON_HEAD_TRIGGER = ClaySoldiersCommon.PLATFORM.registerCriterionTrigger("clay_soldier_on_head",
            ClaySoldierOnHeadTrigger::new
    );

    public static final Supplier<MapCodec<BossClaySoldierSubPredicate>> BOSS_CLAY_SOLDIER_PREDICATE = ClaySoldiersCommon.PLATFORM.registerEntitySubPredicate("boss_clay_soldier_predicate",
            () -> BossClaySoldierSubPredicate.CODEC
    );

    public static final Supplier<MapCodec<PropertyClaySoldierSubPredicate>> SOLDIER_PROPERTY_PREDICATE = ClaySoldiersCommon.PLATFORM.registerEntitySubPredicate("property_clay_soldier_predicate",
            () -> PropertyClaySoldierSubPredicate.CODEC
    );



    private ModCriterions() {
    }

    public static void init() {
    }
}
