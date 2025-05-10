package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EscritoireBlock;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctions;
import net.bumblebee.claysoldiers.claypoifunction.ColorGetterFunction;
import net.bumblebee.claysoldiers.clayremovalcondition.*;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.commands.ClaySoldierCommands;
import net.bumblebee.claysoldiers.commands.ColorHelperArgumentType;
import net.bumblebee.claysoldiers.commands.DefaultedResourceLocationArgument;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.*;
import net.bumblebee.claysoldiers.entity.goal.workgoal.dig.DigHoleGoal;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.VampireSubjugate;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusManager;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.bumblebee.claysoldiers.integration.jade.providers.*;
import net.bumblebee.claysoldiers.item.BrickedClaySoldierItem;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchItem;
import net.bumblebee.claysoldiers.item.disruptor.ClayMobKillItem;
import net.bumblebee.claysoldiers.menu.AbstractClayMobScreen;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseScreen;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseSlot;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierScreen;
import net.bumblebee.claysoldiers.soldieritemtypes.DefaultSoldierItemTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.IEvacuationProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.RangedAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttacks;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Supplier;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, ClaySoldiersCommon.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ClaySoldiersCommon.BLUEPRINT_DATA_PACK_LANG, "Clay Soldiers: Blueprints");
        add(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION, "Enabled Blueprint features for Clay Soldiers");
        add(ClaySoldiersCommon.BLUEPRINT_PACK_SOURCE, "feature, experimental");

        addConfig("csrClientConfig", "Client Config");
        addConfig("claySoldierMenuModify", "Modify in Menu");
        addConfig("hamsterWheelCapacity", "Hamster Wheel Energy Capacity");
        addConfig("hamsterWheelSpeed", "Hamster Wheel Speed");

        add(ClaySoldiersCommon.CLAY_SOLDIER_DROP_RULE.getDescriptionId(), "Chance (in %) for a Clay Soldier to drop itself when killed");
        add(ClaySoldiersCommon.CLAY_SOLDIER_INVENTORY_DROP_RULE.getDescriptionId(), "Clay Soldier drop their inventory on Death");

        add(ModCreativeTab.CLAY_SOLDIERS_TAB_TITLE, "Clay Soldiers");
        add(ModCreativeTab.CLAY_SOLDIER_ITEMS_TAB_TITLE, "Clay Soldier Items");

        add(Util.makeDescriptionId("enchantment", ModEnchantments.SOLDIER_PROJECTILE.location()), "Clay Soldier Slingshot");

        addBlock(ModBlocks.HAMSTER_WHEEL_BLOCK, "Hamster Wheel");
        addBlock(ModBlocks.EASEL_BLOCK, "Easel");
        addBlock(ModBlocks.ESCRITOIRE_BLOCK, "Escritoire");
        add(EscritoireBlock.CONTAINER_TITLE, "Escritoire");

        addItem(ModItems.CLAY_SOLDIER, "Clay Soldier");
        add(ModItems.CLAY_SOLDIER.get().getDescriptionId() + ClaySoldierSpawnItem.DESCRIPTION_ID_PREFIX, "%s Clay Soldier");
        add(ClaySoldierSpawnItem.PLAYER_LANG, "Created by %s");

        addItem(ModItems.BRICKED_CLAY_SOLDIER, "Bricked Clay Soldier");
        addItem(ModItems.SHEAR_BLADE, "Shear Blade");
        addItem(ModItems.SHARPENED_STICK, "Sharpened Stick");
        addItem(ModItems.CLAY_DISRUPTOR, "Disruptor");
        addItem(ModItems.TERRACOTTA_DISRUPTOR, "Disruptor");
        add(ClayMobKillItem.RANGE_LANG, "[Range: %s]");
        add(ClayMobKillItem.RANGE_UNLIMITED_LANG, "Unlimited");
        add(ClayMobKillItem.RANGE_ERROR_LANG, "[Invalid Range]");
        addItem(ModItems.CLAY_COOKIE, "Clay Cookie");
        addItem(ModItems.TEST_ITEM, "Debug Item");
        addItem(ModItems.CLAY_GOGGLES, "Clay Goggles");
        addItem(ModItems.CLAY_BRUSH, "Clay Brush");
        add(ClayBrushItem.POI_SET_LANG, "Poi: (%s)");
        add(ClayBrushItem.POI_CLEAR_LANG, "Poi cleared");
        add(ClayBrushItem.NO_MODE_LANG, "No Selected Mode");
        addItem(ModItems.BLUEPRINT_PAGE, "Empty Blueprint");
        addItem(ModItems.BLUEPRINT, "Blueprint (Empty)");
        add(BlueprintItem.DESCRIPTION_ID_WITH_STRUCTURE, "Blueprint (%s)");
        add(BlueprintItem.STRUCTURE_NAME_LANG, "Structure: %s");
        add(BlueprintItem.BLUEPRINT_INVALID_LANG, "Invalid");

        addItem(ModItems.CLAY_STAFF, "Clay Staff");
        addItem(ModItems.CLAY_POUCH, "Clay Pouch");
        add(ClayPouchItem.FULLNESS_LANG, "%s/%s");

        add(ModDatapackProvider.SMALL_HOUSE_LANG, "Small House");
        add(ModDatapackProvider.SMALL_FARM_LANG, "Farm");
        add(ModDatapackProvider.LARGE_HOUSE_LANG, "Large House");


        addKeyableProperty(ClayBrushItem.Mode.COMMAND, "Command");
        addKeyableProperty(ClayBrushItem.Mode.WORK, "Work");
        addKeyableProperty(ClayBrushItem.Mode.POI, "Poi");
        add(SoldierStatusManager.SITTING_LANG, "Sitting");
        add(SoldierStatusManager.USING_POI_LANG, "Using Poi");
        add(IWorkGoal.DEFAULT_STATUS_LANG, "Working");
        add(AbstractWorkGoal.BREAK_LANG, "On a short Break");
        add(AbstractWorkGoal.CARRYING_LANG, "Carrying");
        add(AbstractWorkGoal.SEARCHING_LANG, "Searching");
        add(AbstractWorkGoal.STUCK_LANG, "Stuck");
        add(AbstractWorkGoal.REQUIRES_POI_LANG, "But does not know where");
        add(DigHoleGoal.DIG_LANG, "Digging");
        add(DigHoleGoal.BREAKING_LANG, "breaking blocks");
        add(DigHoleGoal.UNBREAKABLE_BLOCK, "but cannot break block");
        add(BreakCropGoal.BREAK_CROPS_LANG, "Harvesting");
        add(BreakCropGoal.CROP_BREAK_DISALLOWED, "Not allowed to Break Crops");
        add(PlaceSeedsGoal.PLACING_SEEDS_LANG, "Replanting");
        add(PickUpItemsGoal.PICK_UP_ITEM_LANG, "Pick up Items");
        add(BuildBlueprintGoal.BUILDING_LANG, "Building");
        add(WorkSelectorGoal.WORK_STATUS_PAIR_LANG, "%s: %s");
        add(WorkSelectorGoal.WORK_STATUS_SOMETHING_LANG, "Does Something");
        add(WorkSelectorGoal.WORK_STATUS_RESTING_LANG, "Resting");

        add(AbstractClaySoldierEntity.DEFENDING_AREA_LANG, "Defending this Area");
        add(AbstractClaySoldierEntity.PROTECTING_OWNER_LANG, "Protecting its owner");
        add(ClayMobEntity.WORK_POI_CLEARED_LANG, "Poi Cleared");
        add(ClayMobEntity.WORK_POI_INVALID_LANG, "Invalid Poi (%s)");

        add(ModItems.CAKE_HORSE.get(), "Cake Horse");
        add(ModItems.GRASS_HORSE.get(), "Grass Horse");
        add(ModItems.SNOW_HORSE.get(), "Snow Horse");
        add(ModItems.MYCELIUM_HORSE.get(), "Mycelium Horse");
        add(ModItems.CAKE_PEGASUS.get(), "Cake Pegasus");
        add(ModItems.GRASS_PEGASUS.get(), "Grass Pegasus");
        add(ModItems.SNOW_PEGASUS.get(), "Snow Pegasus");
        add(ModItems.MYCELIUM_PEGASUS.get(), "Mycelium Pegasus");

        add(BrickedClaySoldierItem.ORIGINAL_DISPLAY_NAME, "Original: %s");

        addEntityType(ModEntityTypes.CLAY_SOLDIER_ENTITY, "Clay Soldier");
        addEntityType(ModEntityTypes.CLAY_WRAITH, "Wraith");
        addEntityType(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY, "Zombie Clay Soldier");
        addEntityType(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY, "Vampire Clay Soldier");
        addEntityType(ModEntityTypes.CLAY_HORSE_ENTITY, "Clay Horse");
        addEntityType(ModEntityTypes.CLAY_PEGASUS_ENTITY, "Clay Pegasus");

        add(VampireSubjugate.SUBJUGATE_TRANSLATION_KEY, "Subjugate");

        addEntityType(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY, "Boss Clay Soldier");
        addEntityType(ModEntityTypes.VAMPIRE_BAT, "Vampire Bat");

        addEntityType(ModEntityTypes.CLAY_SOLDIER_THROWABLE_ITEM, "Clay Soldier Thrown Item");
        addEntityType(ModEntityTypes.CLAY_SOLDIER_SNOWBALL, "Clay Soldier Snowball");
        addEntityType(ModEntityTypes.CLAY_SOLDIER_POTION, "Clay Soldier Potion");

        addEntityType(ModEntityTypes.CLAY_BLOCK_PROJECTILE, "Clay Block");


        add(ModEffects.SLIME_ROOT.value(), "Slime Root");
        add(ModEffects.VAMPIRE_CONVERSION.value(), "Vampiric Conversion");

        addKeyableProperty(DefaultSoldierItemTypes.BASIC, "Spawned a Basic Set of Items");
        addKeyableProperty(DefaultSoldierItemTypes.RANGED, "Spawned a set of Ranged Items");
        addKeyableProperty(DefaultSoldierItemTypes.TANK, "Spawned a set of Tank Items");
        addKeyableProperty(DefaultSoldierItemTypes.ARMORED, "Spawned a set of Armor Items");
        addKeyableProperty(DefaultSoldierItemTypes.SPECIALIST, "Spawned Items for Specialists");
        addKeyableProperty(DefaultSoldierItemTypes.HEALER, "Spawned Items for Healers");
        addKeyableProperty(DefaultSoldierItemTypes.DIVER, "Spawned Items for Divers");
        addKeyableProperty(DefaultSoldierItemTypes.ARSONIST, "Spawned Items for Arsonists");
        addKeyableProperty(DefaultSoldierItemTypes.ROYALTY, "Spawned Items for Royalty");
        addKeyableProperty(DefaultSoldierItemTypes.FASHION, "Spawned some stylish Items");
        addKeyableProperty(DefaultSoldierItemTypes.MAGICIAN, "Spawned some magical Items");
        addKeyableProperty(DefaultSoldierItemTypes.KINGDOM, "Spawned Items for an entire Kingdom");

        add(ColorHelperArgumentType.INVALID_COLOR, "'%s' is not a valid color");
        add(DefaultedResourceLocationArgument.INVALID_RESOURCE_LOCATION, "Unknown Id '%s'");

        add(ClaySoldierCommands.COMMAND_TEAM_SINGLE_SUCCESS, "Changed the Team of a ClaySoldier to %s");
        add(ClaySoldierCommands.COMMAND_TEAM_MULTIPLE_SUCCESS, "Changed the Team of %s ClaySoldiers to %s");
        add(ClaySoldierCommands.COMMAND_TEAM_FAILURE, "Failed to find any Clay Soldiers");

        add(ClaySoldierCommands.COMMAND_EXECUTED_BY_PLAYER, "Command needs to be executed by a player");
        add(ClaySoldierCommands.COMMAND_TEAM_ITEM_SUCCESS, "Successfully changed the Team of a Clay Doll");

        add(ClaySoldierCommands.COMMAND_LOADED_TEAMS_SUCCESS, "All currently loaded Clay Soldier Teams [%s]");
        add(ClaySoldierCommands.COMMAND_SHOW_TEAM_ALLEGIANCE_SUCCESS, "%s is loyal to %s");
        add(ClaySoldierCommands.COMMAND_SHOW_TEAM_ALLEGIANCE_EMPTY, "No Team is currently loyal to any Player");

        add(ClaySoldierCommands.COMMAND_ITEM_SET_ERROR, "This item set does not exist");
        add(ClaySoldierCommands.COMMAND_ITEM_SET_FAILURE, "The Item Set does not contain any Items");

        add(ClaySoldierCommands.COMMAND_TEAM_LOYALTY_REMOVE, "%s is no longer loyal anyone");
        add(ClaySoldierCommands.COMMAND_TEAM_LOYALTY_SET, "%s is now loyal to %s");

        add(ClaySoldierCommands.COMMAND_TEAM_LOYALTY_REMOVE_FAILURE, "%s is not loyal to any one");
        add(ClaySoldierCommands.COMMAND_TEAM_LOYALTY_FAILURE, "Could not set loyalty of %s to %s");


        this.addJade(JadeRegistry.CLAY_MOB, "Clay Mob");
        this.addJade(JadeRegistry.CLAY_SOLDIER, "Clay Soldier");
        this.addJade(JadeRegistry.ZOMBIE_CLAY_SOLDIER, "Zombie Clay Soldier");
        this.addJade(JadeRegistry.CLAY_WRAITH, "Clay Wraith");
        this.addJade(JadeRegistry.VAMPIRE_SOLDIER, "Vampire Clay Soldier");
        this.addJade(JadeRegistry.EASEL_BLOCK, "Easel Block");
        this.addJade(JadeRegistry.HAMSTER_WHEEL_BLOCK, "Hamster Wheel Block");
        this.addJade(JadeRegistry.BOSS_CLAY_SOLDIER, "Clay Soldier Boss");


        add(ClayMobProvider.CLAY_MOB_TEAM, "Team");
        add(ClaySoldierProvider.SOLDIER_PROPERTIES, "Properties");
        add(ClaySoldierProvider.ALPHA_PREFIX, "Alpha");
        add(ClaySoldierProvider.OFFSET_COLOR, "Offset Color");

        add(ZombieClaySoldierProvider.PREVIOUS_CLAY_MOB_TEAM, "Previous Team: %s");
        add(ZombieClaySoldierProvider.CURABLE, "Curable");
        add(ZombieClaySoldierProvider.CURABLE_TRUE, "Yes");
        add(ZombieClaySoldierProvider.CURABLE_FALSE, "No");

        add(VampireClaySoldierProvider.POWER, "Power: %s");

        add(ClayWraithProvider.LIMITED_LIFE, "Limited Life");
        add(ClayWraithProvider.LIMITED_LIFE_FALSE, "No");
        add(ClayWraithProvider.LIMITED_LIFE_TIME, "%ss");

        add(EaselBlockProvider.EASEL_REMAINING_LANG, "Remaining Blocks:");
        add(EaselBlockProvider.EASEL_FINISHED_LANG, "Finished");

        add(HamsterWheelBlockProvider.HAMSTER_WHEEL_SPEED, "Speed: %s");
        add(HamsterWheelBlockProvider.GENERATING, "Generating: %s" + ClaySoldiersCommon.PLATFORM.getEnergyUnitName() + "/t");

        add(BossClaySoldierProvider.CLAY_SOLDIER_BOSS_NAME, "Boss");

        add(ComponentFormating.SOLDIER_PROPERTIES_EQUIP, "When equip by Soldier:");
        add(ComponentFormating.SOLDIER_PROPERTIES_EQUIP_AND_PREDICATE, "When equip by Soldier and:");
        add(ComponentFormating.SOLDIER_BECOMES_ATTACK_TYPE, "Soldier Becomes %s");

        add(ComponentFormating.SOLDIER_POI_ITEM, "Clay Soldier can interact with this Item when it is on the ground.");
        add(ComponentFormating.SOLDIER_POI_BLOCK, "Clay Soldier can interact with this Block when it is nearby.");

        add(ComponentFormating.SOLDIER_POI_PREDICATE, "Soldier can use:");
        add(ComponentFormating.SOLDIER_POI_EFFECT, "When Soldier uses:");
        add(ComponentFormating.SOLDIER_ITEM_PICKUP_EFFECT, "On equip:");
        add(ComponentFormating.SOLDIER_ITEM_PICKUP_REMOVE, "Remove when:");
        add(ComponentFormating.CLAY_HORSE_PROPERTIES_EQUIP, "When equip by Clay Horse:");

        add(ComponentFormating.CLAY_HORSE_PROTECTION, "Protection: %s");

        add(ClayPredicates.HAS_CUSTOM_COLOR_COMPONENT, "When has custom color");
        add(ClayPredicates.ITEM_PREDICATE_COMPONENT, "When %s in slot %s");
        add(ClayPredicates.ITEM_PREDICATE_ANY_COMPONENT, "When %s %s");
        addKeyableProperty(ClayPredicates.ItemPredicateSlot.ANY_SLOT, "in any slot");
        addKeyableProperty(ClayPredicates.ItemPredicateSlot.SUITABLE, "has any suitable slots");
        add(ClayPredicates.SOLDIER_PROPERTY_PREDICATE_COMPONENT, "When %s is %s");
        add(ClayPredicates.EFFECT_PREDICATE_COMPONENT, "When %s");
        add(ClayPredicates.EFFECT_PREDICATE_DURATION_COMPONENT, "When %s with duration of %ss");
        add(ClayPredicates.EFFECT_PREDICATE_AMPLIFIER_COMPONENT, "When %s with amplifier of %s");
        add(ClayPredicates.EFFECT_PREDICATE_DURATION_AMPLIFIER_COMPONENT, "When %s with duration of %ss and amplifier of %s");

        add(ClayPoiFunctions.EFFECT_FUNCTION_ADD, "Add %s with amplifier %s for %s");
        add(ClayPoiFunctions.EFFECT_FUNCTION_REMOVE, "Remove %s");
        add(ClayPoiFunctions.EFFECT_FUNCTION_INCREASE, "Increase %s by %s and %s");
        add(ClayPoiFunctions.DYE_FUNCTION, "Set color %s");
        addKeyableProperty(ColorGetterFunction.NONE, "to clear");
        addKeyableProperty(ColorGetterFunction.FROM_BLOCK_MAP_COLOR, "from Block");
        addKeyableProperty(ColorGetterFunction.FROM_DYE, "from Dye");
        add(ClayPoiFunctions.SET_ITEM_FUNCTION, "Set %s in %s ");
        add(ClayPoiFunctions.SET_ITEM_FIND_FUNCTION, "Set %s in any Slot");
        add(ClayPoiFunctions.CONVERSION_FUNCTION, "Convert to %s");
        add(ClayPoiFunctions.SELECT_RANDOM_FUNCTION, "Selected randomly:");


        addKeyableProperty(RemovalConditionContext.MovementType.TO_OWNER, "owner");
        addKeyableProperty(RemovalConditionContext.MovementType.TO_SAFETY, "safety");
        addKeyableProperty(RemovalConditionContext.MovementType.TO_TARGET, "target");
        add(OnHurtCondition.HURT_LANG_KEY, "Hurt");
        add(OnTeleportCondition.TELEPORTATION_LANG_KEY, "Teleport to %s");
        add(OnUseCondition.MELEE_LANG_KEY, "Performed Melee Action");
        add(OnUseCondition.RANGED_LANG_KEY, "Performed Ranged Action");
        add(OnUseCondition.ERROR_LANG_KEY, "Error Invalid Type (%s) for On Use");
        add(OnEscapeCondition.ESCAPE_STRING, "Evacuate with Rocket");
        add(OnBounceCondition.BOUNCE_LANG, "Bounced");

        add(AbstractClayMobScreen.CLAY_TEAM_LABEL, "Team: %s");
        add(AbstractClayMobScreen.CLAY_TEAM_LOYAL_LABEL, "Loyal to %s");
        add(AbstractClayMobScreen.SLOT_LABEL, "Slot: %s");
        add(ClayHorseScreen.CLAY_RIDER_TEAM_LABEL, "Team: %s");

        add(ClaySoldierScreen.SOLDIER_PROPERTIES, "Soldier Properties:");
        add(ClaySoldierScreen.PREVIOUS_CLAY_TEAM_LABEL, "Previous Team: %s");
        add(ClaySoldierScreen.REVIVE_TYPE_COOLDOWN, "Revive Cooldowns");
        add(ClaySoldierScreen.REVIVE_TYPE_COOLDOWN_ENTRY, "%s: %ss");

        addKeyableProperty(AttackTypeProperty.NORMAL, "Normal");
        addKeyableProperty(AttackTypeProperty.PACIFIST, "Pacifist");
        addKeyableProperty(AttackTypeProperty.AGGRESSIVE, "Aggressive");
        addKeyableProperty(AttackTypeProperty.SUPPORT, "Support");
        addKeyableProperty(AttackTypeProperty.KING, "King");
        addKeyableProperty(AttackTypeProperty.QUEEN, "Queen");
        addKeyableProperty(AttackTypeProperty.ZOMBIE, "Zombie");
        addKeyableProperty(AttackTypeProperty.VAMPIRE, "Vampire");
        addKeyableProperty(AttackTypeProperty.BOSS, "Boss");
        add(RangedAttackType.RANGED_ATTACK_TYPE, "Ranged Attack");
        addKeyableProperty(RangedAttackType.NONE, "None");
        addKeyableProperty(RangedAttackType.HARM, "Harmful");
        addKeyableProperty(RangedAttackType.HELPING, "Helping");

        addKeyableProperty(ReviveType.NECROTIC, "Necromancer");
        addKeyableProperty(ReviveType.DARK_NECROTIC, "Dark Necromancer");
        addKeyableProperty(ReviveType.MEDIC, "Medic");
        addKeyableProperty(ReviveType.ANGEL, "Angel");
        addKeyableProperty(ReviveType.WRAITH, "Spirit Master");

        addKeyableProperty(EffectImmunityType.IMMUNE, "Immune");
        addKeyableProperty(EffectImmunityType.PERSISTENT, "Persist");

        add(ReviveType.NECROTIC.translatablePrefixKey(), "Necromantic");
        add(ReviveType.DARK_NECROTIC.translatablePrefixKey(), "Necrotic");
        add(ReviveType.MEDIC.translatablePrefixKey(), "Medical");
        add(ReviveType.ANGEL.translatablePrefixKey(), "Angelic");
        add(ReviveType.WRAITH.translatablePrefixKey(), "Spiritual");

        add(IEvacuationProperty.EvacuationProperty.NONE.translatableKey(), "None");
        add(IEvacuationProperty.EvacuationProperty.FIREWORK.translatableKey(), "Firework");

        add(SpecialAttacks.CritAttack.DISPLAY_NAME_KEY, "Critical Strike");
        add(SpecialAttacks.Ignite.DISPLAY_NAME_KEY, "Ignite: %ss");
        add(SpecialAttacks.Smite.DISPLAY_NAME_KEY, "Smite");
        add(SpecialAttacks.Thorns.DISPLAY_NAME_KEY, "Thorns");
        add(SpecialAttacks.EffectAttack.DISPLAY_NAME_KEY, "Effect");
        add(SpecialAttacks.LightningAttack.DISPLAY_NAME_KEY, "Lightning");
        add(SpecialAttacks.SneakAttack.DISPLAY_NAME_KEY, "Sneak Attack");

        addKeyableProperty(SoldierEquipmentSlot.HEAD, "Head");
        addKeyableProperty(SoldierEquipmentSlot.CHEST, "Chest");
        addKeyableProperty(SoldierEquipmentSlot.LEGS, "Legs");
        addKeyableProperty(SoldierEquipmentSlot.FEET, "Feet");
        addKeyableProperty(SoldierEquipmentSlot.BACKPACK_PASSIVE, "Backpack");
        addKeyableProperty(SoldierEquipmentSlot.BACKPACK, "Backpack");
        addKeyableProperty(SoldierEquipmentSlot.CAPE, "Cape");
        addKeyableProperty(SoldierEquipmentSlot.OFFHAND, "Offhand");
        addKeyableProperty(SoldierEquipmentSlot.MAINHAND, "Mainhand");

        add(ClayHorseSlot.ARMOR_SLOT_NAME, "Armor");

        add(ClayPredicates.LogicComparator.ALL.getDisplayName().getString(), "When all:");
        add(ClayPredicates.LogicComparator.ANY.getDisplayName().getString(), "When any:");
        add(ClayPredicates.LogicComparator.NONE.getDisplayName().getString(), "When none:");
        add(ClayPredicates.LogicComparator.NOT.getDisplayName().getString(), "When not:");

        add(ClayPredicates.PropertyTestType.EXIST.getDescriptionId(), "When has %s");
        add(ClayPredicates.PropertyTestType.INCREASE.getDescriptionId(), "When %s is increased");
        add(ClayPredicates.PropertyTestType.DECREASE.getDescriptionId(), "When %s is decreased");
        add(ClayPredicates.PropertyTestType.COUNT.getDescriptionId(), "When has %s at least %s");
        add(ClayPredicates.PropertyTestType.IS_EXACTLY.getDescriptionId(), "When %s is exactly %s");

        addSoldierPropertyType(SoldierPropertyTypes.DAMAGE, "Damage");
        addSoldierPropertyType(SoldierPropertyTypes.PROTECTION, "Protection");
        addSoldierPropertyType(SoldierPropertyTypes.EXPLOSION_RESISTANCE, "Blast Protection");
        addSoldierPropertyType(SoldierPropertyTypes.SET_ON_FIRE, "Set On Fire");
        addSoldierPropertyType(SoldierPropertyTypes.THROWABLE, "Throwable");
        addSoldierPropertyType(SoldierPropertyTypes.SEE_INVISIBILITY, "Can See Invisibility");
        addSoldierPropertyType(SoldierPropertyTypes.CAN_SWIM, "Can Swim");
        addSoldierPropertyType(SoldierPropertyTypes.BREATH_HOLD, "Breath Hold");
        addKeyableProperty(BreathHoldPropertyType.BreathHoldProperty.INFINITE, "Infinite");
        addKeyableProperty(BreathHoldPropertyType.BreathHoldProperty.NONE, "None");
        addSoldierPropertyType(SoldierPropertyTypes.ATTRIBUTES, "Attributes");
        addSoldierPropertyType(SoldierPropertyTypes.DEATH_CLOUD, "Death Cloud");
        addSoldierPropertyType(SoldierPropertyTypes.DEATH_EXPLOSION, "Death Explosion");
        addSoldierPropertyType(SoldierPropertyTypes.SIZE, "Size");
        addSoldierPropertyType(SoldierPropertyTypes.INVISIBLE, "Invisible");
        addSoldierPropertyType(SoldierPropertyTypes.GLOW_OUTLINE, "Glow Outline");
        addSoldierPropertyType(SoldierPropertyTypes.GLOW_IN_THE_DARK, "Glowing");
        addSoldierPropertyType(SoldierPropertyTypes.ATTACK_TYPE, "Attack Type");
        addSoldierPropertyType(SoldierPropertyTypes.HEAVY, "Armor Toughness");
        addSoldierPropertyType(SoldierPropertyTypes.SPECIAL_ATTACK, "Special Attack");
        addSoldierPropertyType(SoldierPropertyTypes.COUNTER_ATTACK, "Counter Attack");
        addSoldierPropertyType(SoldierPropertyTypes.DAMAGE_BLOCK, "Damage Block");
        addSoldierPropertyType(SoldierPropertyTypes.REVIVE_PROPERTY, "Revive");
        addSoldierPropertyType(SoldierPropertyTypes.IMMUNITY, "Immunity");
        addSoldierPropertyType(SoldierPropertyTypes.WRAITH, "Wraith");
        addSoldierPropertyType(SoldierPropertyTypes.ATTACK_RANGE, "Attack Range");
        addSoldierPropertyType(SoldierPropertyTypes.CAN_GLIDE, "Gliding");
        addSoldierPropertyType(SoldierPropertyTypes.TELEPORTATION, "Teleportation");
        addSoldierPropertyType(SoldierPropertyTypes.TELEPORT_TO_OWNER, "Teleport to Owner");
        addSoldierPropertyType(SoldierPropertyTypes.EVACUATION, "Emergency Evacuation");
        addSoldierPropertyType(SoldierPropertyTypes.BOUNCE, "Bounce");


        add(ModTags.Items.GAME_MASTER_ITEM, "Game Master Items");
        add(ModTags.Items.SOLDIER_WEAPON, "Clay Soldier Weapon");
        add(ModTags.Items.SOLDIER_HOLDABLE, "Clay Soldier Item");
        add(ModTags.Items.SOLDIER_ARMOR, "Clay Soldier Armor");
        add(ModTags.Items.CLAY_HORSE_ARMOR, "Clay Horse Armor");
        add(ModTags.Items.SOLDIER_RGB_GLASSES, "Clay Soldier RGB Glasses");
        add(ModTags.Items.CLAY_FOOD, "Clay Soldier Food");
        add(ModTags.Items.SOLDIER_POI, "Clay Soldier POIs");
        add(ModTags.Items.CLAY_WAX, "Clay Soldier Wax");
        add(ModTags.Items.SOLDIER_BOSS_EQUIPABLE, "Clay Soldier Boss Equipment");


        add(ModTags.Items.ARMORED, "Clay Soldier Armored Items");
        add(ModTags.Items.BASIC, "Basic Clay Soldier Items");
        add(ModTags.Items.EXPLOSIVE_EXPERT, "Explosive Clay Soldier Items");
        add(ModTags.Items.DIVER, "Aquatic Clay Soldier Items");
        add(ModTags.Items.MAGICIAN, "Magical Clay Soldier Items");
        add(ModTags.Items.RANGED, "Ranged Clay Soldier Items");
        add(ModTags.Items.SPECIALIST, "Miscellaneous Clay Soldier Items");
        add(ModTags.Items.HEALER, "Clay Soldier Items for Supporting");
        add(ModTags.Items.TANK, "Clay Soldier Items for Tanking");
        add(ModTags.Items.ROYALTY, "Clay Soldier Items for Royalty");
        add(ModTags.Items.FASHION, "Stylish Clay Soldier Items");
        add(ModTags.Items.KINGDOM, "Clay Soldier Items for a Kingdom");


        add(ModTags.Blocks.BLUEPRINT_BLACK_LISTED, "Blueprint Blacklist");
        add(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE, "Clay Soldier Damage");
        add(ModTags.EntityTypes.CLAY_BOSS, "Clay Soldier Boss");
        add(ModTags.SoldierPropertyTypes.REQUIRES_OWNER, "Requires Owner");
    }

    private void addKeyableProperty(KeyableTranslatableProperty property, String name) {
        this.add(property.translatableKey(), name);
    }

    private <T extends SoldierPropertyType<?>> void addSoldierPropertyType(Supplier<T> propertyType, String name) {
        this.add(propertyType.get().getDescriptionId(), name);
    }
    private void addJade(ResourceLocation key, String name) {
        this.add("config.jade.plugin_%s.%s".formatted(key.getNamespace(), key.getPath()), name);
    }
    private void addConfig(String key, String name) {
        this.add(ClaySoldiersCommon.MOD_ID + ".configuration." + key, name);
    }
}
