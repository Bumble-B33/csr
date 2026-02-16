package net.bumblebee.claysoldiers.datagen.advancements;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.advancements.*;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.datagen.ModRecipeProvider;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.goal.UseAssignedPoiGoal;
import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModAdvancements implements AdvancementSubProvider {
    private static final String TITLE_BASE = "advancements." + ClaySoldiersCommon.MOD_ID + ".clay_soldiers.%s.title";
    private static final String DESCRIPTION_BASE = "advancements." + ClaySoldiersCommon.MOD_ID + ".clay_soldiers.%s.description";

    public static final String ROOT_TITLE = TITLE_BASE.formatted("root");
    public static final String ROOT_DESCRIPTION = DESCRIPTION_BASE.formatted("root");

    public static final String SOLDIER_TITLE = TITLE_BASE.formatted("soldier");
    public static final String SOLDIER_DESCRIPTION = DESCRIPTION_BASE.formatted("soldier");

    public static final String BRICKED_TITLE = TITLE_BASE.formatted("bricked");
    public static final String BRICKED_DESCRIPTION = DESCRIPTION_BASE.formatted("bricked");

    public static final String REVIVE_TITLE = TITLE_BASE.formatted("revive");
    public static final String REVIVE_DESCRIPTION = DESCRIPTION_BASE.formatted("revive");


    public static final String DISRUPTOR_TITLE = TITLE_BASE.formatted("disruptor");
    public static final String DISRUPTOR_DESCRIPTION = DESCRIPTION_BASE.formatted("disruptor");

    public static final String WAXED_TITLE = TITLE_BASE.formatted("waxed");
    public static final String WAXED_DESCRIPTION = DESCRIPTION_BASE.formatted("waxed");

    public static final String BATTLE_TITLE = TITLE_BASE.formatted("battle");
    public static final String BATTLE_DESCRIPTION = DESCRIPTION_BASE.formatted("battle");

    public static final String FOOD_TITLE = TITLE_BASE.formatted("food");
    public static final String FOOD_DESCRIPTION = DESCRIPTION_BASE.formatted("food");

    public static final String EQUIPMENT_TITLE = TITLE_BASE.formatted("equipment");
    public static final String EQUIPMENT_DESCRIPTION = DESCRIPTION_BASE.formatted("equipment");

    public static final String ARMOR_TITLE = TITLE_BASE.formatted("armor");
    public static final String ARMOR_DESCRIPTION = DESCRIPTION_BASE.formatted("armor");

    public static final String WEAPON_TITLE = TITLE_BASE.formatted("weapon");
    public static final String WEAPON_DESCRIPTION = DESCRIPTION_BASE.formatted("weapon");

    public static final String ROYALTY_TITLE = TITLE_BASE.formatted("royalty");
    public static final String ROYALTY_DESCRIPTION = DESCRIPTION_BASE.formatted("royalty");

    public static final String WORKER_TITLE = TITLE_BASE.formatted("worker");
    public static final String WORKER_DESCRIPTION = DESCRIPTION_BASE.formatted("worker");

    public static final String LOYALTY_TITLE = TITLE_BASE.formatted("loyalty");
    public static final String LOYALTY_DESCRIPTION = DESCRIPTION_BASE.formatted("loyalty");

    public static final String COMMAND_TITLE = TITLE_BASE.formatted("command");
    public static final String COMMAND_DESCRIPTION = DESCRIPTION_BASE.formatted("command");

    public static final String POI_TITLE = TITLE_BASE.formatted("poi");
    public static final String POI_DESCRIPTION = DESCRIPTION_BASE.formatted("poi");

    public static final String HAMSTER_TITLE = TITLE_BASE.formatted("hamster");
    public static final String HAMSTER_DESCRIPTION = DESCRIPTION_BASE.formatted("hamster");

    public static final String CHEST_TITLE = TITLE_BASE.formatted("chest");
    public static final String CHEST_DESCRIPTION = DESCRIPTION_BASE.formatted("chest");

    public static final String WORK_TITLE = TITLE_BASE.formatted("work");
    public static final String WORK_DESCRIPTION = DESCRIPTION_BASE.formatted("work");

    public static final String POI_USE_TITLE = TITLE_BASE.formatted("poi_use");
    public static final String POI_USE_DESCRIPTION = DESCRIPTION_BASE.formatted("poi_use");

    public static final String BOSS_TITLE = TITLE_BASE.formatted("boss");
    public static final String BOSS_DESCRIPTION = DESCRIPTION_BASE.formatted("boss");

    public static final String STAFF_TITLE = TITLE_BASE.formatted("clay_staff");
    public static final String STAFF_DESCRIPTION = DESCRIPTION_BASE.formatted("clay_staff");

    public static final String POUCH_TITLE = TITLE_BASE.formatted("clay_pouch");
    public static final String POUCH_DESCRIPTION = DESCRIPTION_BASE.formatted("clay_pouch");

    public static final String BOOK_TITLE = TITLE_BASE.formatted("book");
    public static final String BOOK_DESCRIPTION = DESCRIPTION_BASE.formatted("book");

    public static final String HORSE_TITLE = TITLE_BASE.formatted("horse");
    public static final String HORSE_DESCRIPTION = DESCRIPTION_BASE.formatted("horse");

    public static final String PEGASUS_TITLE = TITLE_BASE.formatted("pegasus");
    public static final String PEGASUS_DESCRIPTION = DESCRIPTION_BASE.formatted("pegasus");

    public static final String BLUEPRINT_PAGE_TITLE = TITLE_BASE.formatted("blueprint_page");
    public static final String BLUEPRINT_PAGE_DESCRIPTION = DESCRIPTION_BASE.formatted("blueprint_page");

    public static final String BLUEPRINT_TITLE = TITLE_BASE.formatted("blueprint");
    public static final String BLUEPRINT_DESCRIPTION = DESCRIPTION_BASE.formatted("blueprint");

    public static final String HOUSE_TITLE = TITLE_BASE.formatted("house");
    public static final String HOUSE_DESCRIPTION = DESCRIPTION_BASE.formatted("house");

    public static final String SOLDIER_ON_HEAD_TITLE = TITLE_BASE.formatted("soldier_on_head");
    public static final String SOLDIER_ON_HEAD_DESCRIPTION = DESCRIPTION_BASE.formatted("soldier_on_head");

    public static ResourceLocation SOLDIER_ID = getSaveLocation("soldier");
    public static ResourceLocation LOYALTY_ID = getSaveLocation("loyalty");



    @Override
    public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> saver) {
        HolderGetter<EntityType<?>> entityTypeHolderGetter = provider.lookupOrThrow(Registries.ENTITY_TYPE);
        HolderGetter<Item> itemRegistry = provider.lookupOrThrow(Registries.ITEM);

        ItemStack clayBrushItemPoi = ModItems.CLAY_BRUSH.get().getDefaultInstance();
        clayBrushItemPoi.set(ModDataComponents.CLAY_BRUSH_MODE.get(), ClayBrushItem.Mode.POI);

        ItemStack clayBrushItemWork = ModItems.CLAY_BRUSH.get().getDefaultInstance();
        clayBrushItemWork.set(ModDataComponents.CLAY_BRUSH_MODE.get(), ClayBrushItem.Mode.WORK);

        var root = new Advancement.Builder()
                .display(
                        new ItemStack(Items.CLAY_BALL),
                        Component.translatable(ROOT_TITLE),
                        Component.translatable(ROOT_DESCRIPTION),
                        ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/gui/advancements/backgrounds/clay.png"),
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(100).addRecipe(ModRecipeProvider.CLAY_SOLDIER_CRAFTING))
                .addCriterion("has_clay_ball", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CLAY_BALL))
                .addCriterion("has_clay", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CLAY))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, getSaveLocation("root"));

        var soldier = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.CLAY_SOLDIER.get()),
                        Component.translatable(SOLDIER_TITLE),
                        Component.translatable(SOLDIER_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(root)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("clay_soldier_crafted", RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.CLAY_SOLDIER_CRAFTING))
                .save(saver, SOLDIER_ID);

        var bricked = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.BRICKED_CLAY_SOLDIER.get()),
                        Component.translatable(BRICKED_TITLE),
                        Component.translatable(BRICKED_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        true
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("clay_soldier_smelted", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.BRICKED_CLAY_SOLDIER.get()))
                .save(saver, getSaveLocation("clay_soldier_smelted"));

        var revive = new Advancement.Builder()
                .display(
                        new ItemStack(Items.GHAST_TEAR),
                        Component.translatable(REVIVE_TITLE),
                        Component.translatable(REVIVE_DESCRIPTION, Items.GHAST_TEAR.getName()),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(bricked)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("revive", RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.CLAY_SOLDIER_REVIVE))
                .save(saver, getSaveLocation("revive"));


        var disruptor = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.CLAY_DISRUPTOR.get()),
                        Component.translatable(DISRUPTOR_TITLE),
                        Component.translatable(DISRUPTOR_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("kill_clay_soldier", DisruptorKillTrigger.createInstance(32))
                .save(saver, getSaveLocation("disruptor_kill"));

        var waxed_soldier = new Advancement.Builder()
                .display(
                        new ItemStack(Items.HONEYCOMB),
                        Component.translatable(WAXED_TITLE),
                        Component.translatable(WAXED_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        true
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("wax_clay_mob", FeedItemClaySoldierTrigger.wax().build())
                .save(saver, getSaveLocation("wax"));

        var battle = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.CLAY_SOLDIER.get()),
                        Component.translatable(BATTLE_TITLE),
                        Component.translatable(BATTLE_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(200))
                .addCriterion("clay_soldier_kill", ClaySoldierDeathTrigger.createBattle(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModTags.EntityTypes.CLAY_SOLDIER).build()))
                .save(saver, getSaveLocation("battler"));

        var food = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.CLAY_COOKIE.get()),
                        Component.translatable(FOOD_TITLE),
                        Component.translatable(FOOD_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(200))
                .addCriterion("clay_soldier_food", FeedItemClaySoldierTrigger.ofCookie().build())
                .save(saver, getSaveLocation("food"));


        var equipment = new Advancement.Builder()
                .display(
                        new ItemStack(Items.STICK),
                        Component.translatable(EQUIPMENT_TITLE),
                        Component.translatable(EQUIPMENT_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(20))
                .addCriterion("equipment", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
                        ContextAwarePredicate.create(),
                        Optional.empty(),
                        Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModEntityTypes.CLAY_SOLDIER_ENTITY.get())))
                ))
                .save(saver, getSaveLocation("equipment"));

        var armor = new Advancement.Builder()
                .display(
                        new ItemStack(Items.LEATHER),
                        Component.translatable(ARMOR_TITLE),
                        Component.translatable(ARMOR_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(equipment)
                .rewards(AdvancementRewards.Builder.experience(20))
                .addCriterion("armor", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
                        ContextAwarePredicate.create(),
                        Optional.of(ItemPredicate.Builder.item().of(itemRegistry, ModTags.Items.SOLDIER_ARMOR).build()),
                        Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModEntityTypes.CLAY_SOLDIER_ENTITY.get())))
                ))
                .save(saver, getSaveLocation("armor"));
        var weapon = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.SHEAR_BLADE.get()),
                        Component.translatable(WEAPON_TITLE),
                        Component.translatable(WEAPON_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(equipment)
                .rewards(AdvancementRewards.Builder.experience(20))
                .addCriterion("weapon", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
                        ContextAwarePredicate.create(),
                        Optional.of(ItemPredicate.Builder.item().of(itemRegistry, ModTags.Items.SOLDIER_WEAPON).build()),
                        Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModEntityTypes.CLAY_SOLDIER_ENTITY.get())))
                ))
                .save(saver, getSaveLocation("weapon"));

        var makeAKingBuilder = new Advancement.Builder()
                .display(
                        new ItemStack(Items.GOLD_INGOT),
                        Component.translatable(ROYALTY_TITLE),
                        Component.translatable(ROYALTY_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        false,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(200))
                .requirements(AdvancementRequirements.Strategy.OR);
        for (AttackTypeProperty attackTypeProperty : AttackTypeProperty.values()) {
            if (!attackTypeProperty.isRoyalty()) {
                continue;
            }
            makeAKingBuilder.addCriterion(attackTypeProperty.getSerializedName(), PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
                    ContextAwarePredicate.create(),
                    Optional.empty(),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity()
                            .of(entityTypeHolderGetter, ModEntityTypes.CLAY_SOLDIER_ENTITY.get()).subPredicate(PropertyClaySoldierSubPredicate.hasProperty(SoldierPropertyTypes.ATTACK_TYPE.get().createProperty(attackTypeProperty)))
                            )
                    )
            ));
        }
        var makeAKing = makeAKingBuilder.save(saver, getSaveLocation("make_a_king"));

        var loyalty = new Advancement.Builder()
                .display(
                        new ItemStack(Items.EMERALD),
                        Component.translatable(LOYALTY_TITLE),
                        Component.translatable(LOYALTY_DESCRIPTION),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        false,
                        false
                )
                .parent(makeAKing)
                .rewards(AdvancementRewards.Builder.experience(400))
                .addCriterion("loyalty", FeedItemClaySoldierTrigger.ofLoyalty().build())
                .save(saver, LOYALTY_ID);

        var clayBrush = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.CLAY_BRUSH.get()),
                        Component.translatable(COMMAND_TITLE),
                        Component.translatable(COMMAND_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(loyalty)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("clay_soldier_sit", ClayBrushCommandTrigger.create(ClayBrushItem.Mode.COMMAND))
                .save(saver, getSaveLocation("clay_soldier_sit"));

        var workerBuilder = new Advancement.Builder()
                .display(
                        new ItemStack(Items.WHEAT),
                        Component.translatable(WORKER_TITLE),
                        Component.translatable(WORKER_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        false,
                        false,
                        false
                )
                .parent(loyalty)
                .rewards(AdvancementRewards.Builder.experience(200))
                .requirements(AdvancementRequirements.Strategy.OR);

        for (AttackTypeProperty attackTypeProperty : AttackTypeProperty.values()) {
            if (!attackTypeProperty.isSupportive()) {
                continue;
            }
            workerBuilder.addCriterion(attackTypeProperty.getSerializedName(), PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
                    ContextAwarePredicate.create(),
                    Optional.empty(),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModEntityTypes.CLAY_SOLDIER_ENTITY.get()).subPredicate(PropertyClaySoldierSubPredicate.hasProperty(SoldierPropertyTypes.ATTACK_TYPE.get().createProperty(attackTypeProperty)))))
            ));
        }

        var worker = workerBuilder.save(saver, getSaveLocation("worker"));


        var clayBrushPoi = new Advancement.Builder()
                .display(
                        clayBrushItemPoi,
                        Component.translatable(POI_TITLE),
                        Component.translatable(POI_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(loyalty)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("clay_soldier_poi", ClayBrushCommandTrigger.create(ClayBrushItem.Mode.POI))
                .save(saver, getSaveLocation("clay_soldier_poi"));

        var hamsterWheel = new Advancement.Builder()
                .display(
                        ModBlocks.HAMSTER_WHEEL_BLOCK.asItem(),
                        Component.translatable(HAMSTER_TITLE),
                        Component.translatable(HAMSTER_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        true
                )
                .parent(clayBrushPoi)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("hamster", UseAssignedWorksiteTrigger.of().setType(HamsterWheelBlockEntity.WORKSITE_ID).setTime(MinMaxBounds.Ints.between(13000, 23000)).build())
                .save(saver, getSaveLocation("hamster"));

        var chest = new Advancement.Builder()
                .display(
                        Blocks.CHEST,
                        Component.translatable(CHEST_TITLE),
                        Component.translatable(CHEST_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        true
                )
                .parent(clayBrushPoi)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("chest", UseAssignedWorksiteTrigger.of().setType(UseAssignedPoiGoal.STORAGE_WORKSITE_ID).setTimesUsed(MinMaxBounds.Ints.atLeast(SoldierEquipmentSlot.values().length)).build())
                .save(saver, getSaveLocation("chest"));

        var clayBrushWork = new Advancement.Builder()
                .display(
                        clayBrushItemWork,
                        Component.translatable(WORK_TITLE),
                        Component.translatable(WORK_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(worker)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("clay_soldier_work", ClayBrushCommandTrigger.create(ClayBrushItem.Mode.WORK))
                .save(saver, getSaveLocation("clay_soldier_work"));

        var flint = new Advancement.Builder()
                .display(
                        Items.FLINT.getDefaultInstance(),
                        Component.translatable(POI_USE_TITLE),
                        Component.translatable(POI_USE_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("poi", SoldierPoiUseTrigger.any())
                .save(saver, getSaveLocation("poi_use"));

        var spawnClayBoss = new Advancement.Builder()
                .display(
                        Items.EGG.getDefaultInstance(),
                        Component.translatable(BOSS_TITLE),
                        Component.translatable(BOSS_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        false
                )
                .parent(flint)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("clay_boss", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get())))
                .save(saver, getSaveLocation("boss"));

        var clayStaff = new Advancement.Builder()
                .display(
                        ModItems.CLAY_STAFF.get().getDefaultInstance(),
                        Component.translatable(STAFF_TITLE),
                        Component.translatable(STAFF_DESCRIPTION),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        false,
                        true
                )
                .parent(spawnClayBoss)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("normal", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                        .of(entityTypeHolderGetter, ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get())
                        .subPredicate(BossClaySoldierSubPredicate.of(ModBossBehaviours.DEFAULT.get()))))
                .addCriterion("zombie", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                        .of(entityTypeHolderGetter, ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get())
                        .subPredicate(BossClaySoldierSubPredicate.of(ModBossBehaviours.ZOMBIE.get()))))
                .addCriterion("staff", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CLAY_STAFF.get()))
                .requirements(new AdvancementRequirements(List.of(List.of("staff"), List.of("normal", "zombie"))))
                .save(saver, getSaveLocation("boss_staff"));

        var clayPouch = new Advancement.Builder()
                .display(
                        ModItems.CLAY_POUCH.get().getDefaultInstance(),
                        Component.translatable(POUCH_TITLE),
                        Component.translatable(POUCH_DESCRIPTION),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        false,
                        true
                )
                .parent(spawnClayBoss)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("pouch", MultiSpawnItemUseTrigger.of().setMinMaxBounds(MinMaxBounds.Ints.atLeast(65)).build())
                .save(saver, getSaveLocation("boss_pouch"));

        var staffEnchant = new Advancement.Builder()
                .display(
                        ModItems.CLAY_SOLDIER.get().getDefaultInstance(),
                        Component.translatable(BOOK_TITLE),
                        Component.translatable(BOOK_DESCRIPTION),
                        null,
                        AdvancementType.CHALLENGE,
                        false,
                        false,
                        true
                )
                .parent(clayStaff)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("hit", HitWithClayBlockTrigger.of()
                        .needsToBeSoldier()
                        .setTarget(EntityPredicate.Builder.entity().of(entityTypeHolderGetter, ModTags.EntityTypes.CLAY_SOLDIER).build())
                        .setWeapon(ItemPredicate.Builder.item().of(itemRegistry, ModItems.CLAY_STAFF.get()))
                        .build())
                .save(saver, getSaveLocation("boss_book"));

        var horseBuilder = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.GRASS_HORSE.get()),
                        Component.translatable(HORSE_TITLE),
                        Component.translatable(HORSE_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(soldier)
                .rewards(AdvancementRewards.Builder.experience(100))
                .requirements(AdvancementRequirements.Strategy.OR);
        for (ClayHorseVariants variant : ClayHorseVariants.values()) {
            horseBuilder.addCriterion(variant.getVariantName(), RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.getClayHorseKey(variant)));
        }
        var horse = horseBuilder.save(saver, getSaveLocation("horse"));
        var pegasusBuilder = new Advancement.Builder()
                .display(
                        new ItemStack(ModItems.GRASS_PEGASUS.get()),
                        Component.translatable(PEGASUS_TITLE),
                        Component.translatable(PEGASUS_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(horse)
                .rewards(AdvancementRewards.Builder.experience(100))
                .requirements(AdvancementRequirements.Strategy.OR);
        for (ClayHorseVariants variant : ClayHorseVariants.values()) {
            pegasusBuilder.addCriterion(variant.getVariantName(), RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.getClayPegasusKey(variant, false)));
            pegasusBuilder.addCriterion(variant.getVariantName() + "_feather", RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.getClayPegasusKey(variant, true)));
        }

        var pegasus = pegasusBuilder.save(saver, getSaveLocation("pegasus"));

        var soldierOnHead = new Advancement.Builder()
                .display(
                        ModItems.CLAY_SOLDIER.get().getDefaultInstance(),
                        Component.translatable(SOLDIER_ON_HEAD_TITLE),
                        Component.translatable(SOLDIER_ON_HEAD_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        false,
                        false,
                        true
                )
                .parent(loyalty)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("equip", ClaySoldierOnHeadTrigger.create())
                .save(saver, getSaveLocation("clay_soldier_on_head"));
    }

    public static void generateForBlueprint(HolderLookup.Provider provider, Consumer<AdvancementHolder> saver) {
        ItemStack blueprintStack = ModItems.BLUEPRINT.get().getDefaultInstance();
        blueprintStack.set(ModDataComponents.BLUEPRINT_ITEM_DATA, new BlueprintItem.BlueprintItemData(0.1f));

        var page = new Advancement.Builder()
                .display(
                        ModItems.BLUEPRINT_PAGE.get().getDefaultInstance(),
                        Component.translatable(BLUEPRINT_PAGE_TITLE),
                        Component.translatable(BLUEPRINT_PAGE_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(Advancement.Builder.advancement().build(LOYALTY_ID))
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("page", RecipeCraftedTrigger.TriggerInstance.craftedItem(ModRecipeProvider.BLUEPRINT_PAGE))
                .save(saver, getSaveLocation("blueprint_page"));

        var blueprint = new Advancement.Builder()
                .display(
                        blueprintStack,
                        Component.translatable(BLUEPRINT_TITLE),
                        Component.translatable(BLUEPRINT_DESCRIPTION),
                        null,
                        AdvancementType.TASK,
                        true,
                        false,
                        false
                )
                .parent(page)
                .rewards(AdvancementRewards.Builder.experience(10))
                .addCriterion("page", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.BLUEPRINT.get()))
                .save(saver, getSaveLocation("blueprint"));

        var house = new Advancement.Builder()
                .display(
                        Items.OAK_DOOR,
                        Component.translatable(HOUSE_TITLE),
                        Component.translatable(HOUSE_DESCRIPTION),
                        null,
                        AdvancementType.GOAL,
                        true,
                        false,
                        false
                )
                .parent(blueprint)
                .rewards(AdvancementRewards.Builder.experience(150))
                .addCriterion("house", BlueprintPlaceBlockTrigger.of().build())
                .save(saver, getSaveLocation("house"));
    }

    private static ResourceLocation getSaveLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "%s/%s".formatted("clay_soldiers", name));
    }
}
