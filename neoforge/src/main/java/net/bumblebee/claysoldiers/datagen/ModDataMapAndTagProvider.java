package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiFunctions;
import net.bumblebee.claysoldiers.claypoifunction.ColorGetterFunction;
import net.bumblebee.claysoldiers.clayremovalcondition.*;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.datagen.api.ClaySoldiersItemProvider;
import net.bumblebee.claysoldiers.datamap.DropRateProperty;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.SoldierPickUpPriority;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.armor.SoldierWearableBuilder;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessorySlot;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.*;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.VampireClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.soldieritemtypes.DefaultSoldierItemTypes;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.*;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.effectimmunity.EffectImmunityType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttacks;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDataMapAndTagProvider extends ClaySoldiersItemProvider {
    private static final CompoundTag VAMPIRE_TAG = new CompoundTag();
    private static final CompoundTag ZOMBIE_TAG = new CompoundTag();
    private static final CompoundTag CLAY_WRAITH_TAG = new CompoundTag();
    private static final CompoundTag DEFAULT_BOSS_TAG = new CompoundTag();
    private static final CompoundTag VAMPIRE_BOSS_TAG = new CompoundTag();

    static {
        VAMPIRE_TAG.putBoolean(VampireClaySoldierEntity.ALPHA_TAG, true);

        ZOMBIE_TAG.putBoolean(ZombieClaySoldierEntity.CURABLE_TAG, false);
        ZOMBIE_TAG.putBoolean(ZombieClaySoldierEntity.PICK_ITEMS_TAG, true);
        ZOMBIE_TAG.putBoolean(ZombieClaySoldierEntity.MATCH_TEAMS, true);

        CLAY_WRAITH_TAG.putInt(ClayWraithEntity.LIFE_TICKS_TAG, 120);
        ClayWraithEntity.writeSpecialAttackToTag(CLAY_WRAITH_TAG, List.of(new SpecialAttacks.LightningAttack(SpecialAttackType.MELEE, 2)));

        BossClaySoldierEntity.writeBossAIToTag(ModBossBehaviours.VAMPIRE.get(), VAMPIRE_BOSS_TAG);
        BossClaySoldierEntity.writeBasePropertiesToTag(SoldierPropertyMap.of(
                SoldierPropertyTypes.SIZE.get().createProperty(5f),
                SoldierPropertyTypes.ATTACK_RANGE.get().createProperty(1f)
        ), VAMPIRE_BOSS_TAG);

        BossClaySoldierEntity.writeBossAIToTag(ModBossBehaviours.DEFAULT.get(), DEFAULT_BOSS_TAG);
        BossClaySoldierEntity.writeBasePropertiesToTag(SoldierPropertyMap.of(
                SoldierPropertyTypes.SIZE.get().createProperty(4f),
                SoldierPropertyTypes.ATTACK_RANGE.get().createProperty(1f)
        ), DEFAULT_BOSS_TAG);
    }

    public ModDataMapAndTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper helper) {
        super(packOutput, lookupProvider, ClaySoldiersCommon.MOD_ID, helper);
        helper.trackGenerated(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_holdable"), PackType.SERVER_DATA, ".json", "tags/item");
    }

    @Override
    protected void gather() {
        this.tag(ModTags.Items.SOLDIER_HOLDABLE)
                .addOptionalTag(ModTags.Items.SOLDIER_WEAPON)
                .addOptionalTag(ModTags.Items.SOLDIER_ARMOR);
        this.tag(ModTags.Items.CLAY_HORSE_ARMOR).add(Items.FEATHER, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT, Items.LEATHER);
        this.tag(ModTags.Items.CLAY_FOOD).add(ModItems.CLAY_COOKIE.get());
        this.tag(ModTags.Items.CLAY_WAX).add(Items.HONEYCOMB);
        this.tag(ModTags.Items.SOLDIER_RGB_GLASSES).addTag(ModTags.Items.GLASS_PANES).remove(ModTags.Items.GLASS_PANES_COLORLESS);
        this.tag(ModTags.Items.GAME_MASTER_ITEM).add(
                Items.COMMAND_BLOCK,
                Items.CHAIN_COMMAND_BLOCK,
                Items.REPEATING_COMMAND_BLOCK,
                Items.COMMAND_BLOCK_MINECART,
                Items.STRUCTURE_BLOCK,
                Items.STRUCTURE_VOID,
                Items.JIGSAW,
                Items.BARRIER,
                Items.LIGHT,
                Items.DEBUG_STICK
        );
        this.tag(ModTags.Items.ACCESSORIES_FACE).add(ModItems.CLAY_GOGGLES.get());
        this.tag(ModTags.Items.CURIOS_HEAD).add(ModItems.CLAY_GOGGLES.get());
        this.tag(ModTags.Items.SOLDIER_BOSS_EQUIPABLE).addTag(ModTags.Items.SOLDIER_HOLDABLE).remove(Items.COMMAND_BLOCK, Items.DEBUG_STICK);
        this.addItemToTags(ModItems.CLAY_STAFF.get(),
                ItemTags.CROSSBOW_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE, Tags.Items.RANGED_WEAPON_TOOLS, ModTags.Items.SOLDIER_SLINGSHOT_ENCHANTABLE);

        addHoldable(Items.STICK, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setDamage(2f)).setSlot(SoldierEquipmentSlot.MAINHAND).build(), TagType.WEAPON, DefaultSoldierItemTypes.BASIC);
        addHoldable(ModItems.SHARPENED_STICK.get(), SoldierHoldableEffect.of(SoldierPropertyMap.builder().setDamage(3f)).setSlot(SoldierEquipmentSlot.MAINHAND).build(), TagType.WEAPON, DefaultSoldierItemTypes.BASIC);
        addHoldable(Items.BONE, new SoldierHoldableEffect(SoldierPropertyMap.builder().setDamage(1f).bonusAttackRange(0.1f), SoldierEquipmentSlot.MAINHAND), TagType.WEAPON, DefaultSoldierItemTypes.BASIC);

        addHoldable(Items.BLAZE_ROD, new SoldierHoldableEffect(SoldierPropertyMap.builder().setDamage(1.5f), SoldierEquipmentSlot.MAINHAND, SoldierPickUpPriority.HIGH), TagType.WEAPON, DefaultSoldierItemTypes.BASIC, DefaultSoldierItemTypes.ARSONIST);
        addHoldable(ItemTags.COALS, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setSetOnFire(20))
                .setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS)
                .setPredicate(new ClayPredicates.SoldierPropertyPredicate(ClayPredicates.PropertyTestType.INCREASE, SoldierPropertyTypes.SET_ON_FIRE.get())).build(), DefaultSoldierItemTypes.ARSONIST);
        addHoldable(Items.SHEARS, SoldierHoldableEffect.of(SoldierPropertyMap.builder()).setSlots(SoldierEquipmentSlot.CUSTOM_EQUIP).setPickUpPriority(SoldierPickUpPriority.HIGH).build(), TagType.WEAPON, DefaultSoldierItemTypes.BASIC);
        addHoldable(ModItems.SHEAR_BLADE.get(), SoldierHoldableEffect.of(SoldierPropertyMap.builder().setDamage(1.25f).addSpecialAttack(new SpecialAttacks.SneakAttack(SpecialAttackType.MELEE, 0.5f))).setSlots(SoldierEquipmentSlot.HANDS).setPickUpPriority(SoldierPickUpPriority.LOW).build(), TagType.WEAPON);

        addHoldable(Items.SNOWBALL, SoldierHoldableEffect.of(
                        SoldierPropertyMap.builder().throwable(RangedAttackType.HARM, 2f).addSpecialAttack(new SpecialAttacks.EffectAttack(SpecialAttackType.RANGED, 0f, MobEffects.MOVEMENT_SLOWDOWN, 400, 0)))
                .setSlot(SoldierEquipmentSlot.BACKPACK).setPickUpPriority(SoldierPickUpPriority.LOW).setMaxStackSize(8)
                .removalCondition(RemovalConditionType.ON_USE_RANGED, OnUseCondition.ranged(1f)).build(), DefaultSoldierItemTypes.BASIC, DefaultSoldierItemTypes.RANGED);
        addHoldable(Items.FIRE_CHARGE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().throwable(RangedAttackType.HARM, 2f).setSetOnFire(45)).setSlot(SoldierEquipmentSlot.BACKPACK).setPickUpPriority(SoldierPickUpPriority.HIGH).removalCondition(RemovalConditionType.ON_USE_RANGED, OnUseCondition.ranged(1f)).setMaxStackSize(6).build(), DefaultSoldierItemTypes.RANGED, DefaultSoldierItemTypes.ARSONIST);
        addHoldable(Items.SLIME_BALL, SoldierHoldableEffect.of(SoldierPropertyMap.builder().throwable(RangedAttackType.HARM, 2f).addSpecialAttack(new SpecialAttacks.EffectAttack(SpecialAttackType.RANGED, 0f, ModEffects.SLIME_ROOT, 20, 1))).setSlot(SoldierEquipmentSlot.BACKPACK).setPickUpPriority(SoldierPickUpPriority.HIGH).removalCondition(RemovalConditionType.ON_USE_RANGED, OnUseCondition.ranged(1f)).setMaxStackSize(6).build(), DefaultSoldierItemTypes.RANGED, DefaultSoldierItemTypes.SPECIALIST);
        addHoldable(Items.GRAVEL, SoldierHoldableEffect.of(SoldierPropertyMap.builder().addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.RANGED, 1f, 0.5f)).throwable(RangedAttackType.HARM, 1f)).setSlot(SoldierEquipmentSlot.BACKPACK).removalCondition(RemovalConditionType.ON_USE_RANGED, OnUseCondition.ranged(1f)).setMaxStackSize(4).build(), DefaultSoldierItemTypes.BASIC, DefaultSoldierItemTypes.RANGED);
        addHoldable(Items.GLASS_BOTTLE, new SoldierHoldableEffect(SoldierPropertyMap.builder().setBreathHold(10), SoldierEquipmentSlot.BACKPACK, SoldierPickUpPriority.LOW), DefaultSoldierItemTypes.DIVER, DefaultSoldierItemTypes.SPECIALIST);
        addHoldable(Items.SUGAR, SoldierHoldableEffect.of(
                SoldierPropertyMap.builder().addAttribute(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "sugar_speed"), 0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
        ).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.LOW).build(), DefaultSoldierItemTypes.SPECIALIST);
        addHoldable(Items.GUNPOWDER, SoldierHoldableEffect.of(SoldierPropertyMap.builder().explosion(1f)).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(), DefaultSoldierItemTypes.ARSONIST);
        addHoldable(Items.TNT, SoldierHoldableEffect.of(SoldierPropertyMap.builder().explosion(2f)).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.HIGH).build(), DefaultSoldierItemTypes.ARSONIST);
        addHoldable(Items.MAGMA_CREAM, SoldierHoldableEffect.of(SoldierPropertyMap.builder().explosion(1f).addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 1f, 0.5f))).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.HIGH).build(), DefaultSoldierItemTypes.ARSONIST);
        addHoldable(Items.POISONOUS_POTATO, SoldierHoldableEffect.of(SoldierPropertyMap.builder().addSpecialAttack(new SpecialAttacks.EffectAttack(SpecialAttackType.MELEE, 0f, MobEffects.POISON, 30, 1)).immunity(MobEffects.POISON, EffectImmunityType.IMMUNE).addDeathCloudEffect(new DeathCloudProperty(MobEffects.POISON, 1, 30))).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(), DefaultSoldierItemTypes.ARSONIST, DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.EMERALD, SoldierHoldableEffect.of(SoldierPropertyMap.builder().addSpecialAttack(new SpecialAttacks.LightningAttack(SpecialAttackType.MELEE, 1f))).setSlot(SoldierEquipmentSlot.BACKPACK).build(), DefaultSoldierItemTypes.SPECIALIST, DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.REDSTONE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().addSpecialAttack(new SpecialAttacks.EffectAttack(SpecialAttackType.MELEE_AND_RANGED, 0f, MobEffects.BLINDNESS, 30, 1))).setSlot(SoldierEquipmentSlot.BACKPACK_PASSIVE)
                .setPredicate(ClayPredicates.LogicPredicate.any(
                        Set.of(new ClayPredicates.ItemPredicate(ItemStack.EMPTY.getItem(), SoldierEquipmentSlot.MAINHAND), new ClayPredicates.ItemPredicate(Items.AIR, SoldierEquipmentSlot.OFFHAND)))
                ).build(), DefaultSoldierItemTypes.SPECIALIST);

        addHoldable(Items.NETHER_STAR, SoldierHoldableEffect.of(
                SoldierPropertyMap.builder().setDamage(2f).addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 1f, 0.5f)).size(2f)
                        .addAttribute(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "giant_slow"), -0.2F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                        .addAttribute(Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "giant_health"), 10F, AttributeModifier.Operation.ADD_VALUE))
        ).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).setDropRate(DropRateProperty.NEVER).build(), DefaultSoldierItemTypes.TANK);
        addHoldable(Items.AMETHYST_SHARD, SoldierHoldableEffect.of(SoldierPropertyMap.builder().invisible()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).addPickUpEffect(new ClayPoiFunctions.DyeSoldierFunction(0x7F3FB2, false)).build(), DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.GLOWSTONE_DUST, SoldierHoldableEffect.of(SoldierPropertyMap.builder().glowing()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.LOW).build(), DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.GLOW_INK_SAC, SoldierHoldableEffect.of(SoldierPropertyMap.builder().glowing()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.LOW).build(), DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.DRAGON_BREATH, SoldierHoldableEffect.of(SoldierPropertyMap.builder().canReviveOther(new ReviveProperty(ReviveType.WRAITH, 0, 0.75f, 10))).setSlot(SoldierEquipmentSlot.BACKPACK).build(), DefaultSoldierItemTypes.MAGICIAN);

        addHoldable(Items.GOLDEN_APPLE, SoldierHoldableEffect.of(
                        SoldierPropertyMap.builder().canReviveOther(new ReviveProperty(ReviveType.MEDIC, 3, 1f, 100))
                                .attackType(AttackTypeProperty.SUPPORT).addSpecialAttack(new SpecialAttacks.EffectAttack(SpecialAttackType.MELEE, 0,
                                        MobEffects.REGENERATION, 600, 2))
                )
                .setSlot(SoldierEquipmentSlot.BACKPACK).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).build(), DefaultSoldierItemTypes.HEALER);

        addHoldable(Items.TOTEM_OF_UNDYING, new SoldierHoldableEffect(SoldierPropertyMap.builder().canReviveOther(new ReviveProperty(ReviveType.ANGEL, 4, 1f, 2000)).addSpecialAttack(new SpecialAttacks.Smite(SpecialAttackType.MELEE_AND_RANGED, 3)), SoldierEquipmentSlot.BACKPACK_PASSIVE, SoldierPickUpPriority.VERY_HIGH), DefaultSoldierItemTypes.HEALER, DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.ECHO_SHARD, SoldierHoldableEffect.of(SoldierPropertyMap.builder().wraith(new WraithProperty(6, 0, List.of(new SpecialAttacks.Smite(SpecialAttackType.MELEE, 1)))).setSeeInvis()).addPickUpEffect(new ClayPoiFunctions.DyeSoldierFunction(0x034150, true)).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(), DefaultSoldierItemTypes.MAGICIAN);
        addHoldable(Items.FERMENTED_SPIDER_EYE, new SoldierHoldableEffect(SoldierPropertyMap.builder().attackType(AttackTypeProperty.AGGRESSIVE), SoldierEquipmentSlot.BACKPACK_PASSIVE, SoldierPickUpPriority.VERY_HIGH));
        addHoldable(Items.WHEAT, new SoldierHoldableEffect(SoldierPropertyMap.builder().attackType(AttackTypeProperty.PACIFIST), SoldierEquipmentSlot.BACKPACK_PASSIVE, SoldierPickUpPriority.VERY_HIGH));
        addHoldable(Items.GLISTERING_MELON_SLICE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().throwable(RangedAttackType.HELPING).attackType(AttackTypeProperty.SUPPORT)).setSlot(SoldierEquipmentSlot.BACKPACK_PASSIVE).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).build(), DefaultSoldierItemTypes.HEALER);
        addHoldable(Items.ENDER_PEARL, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setTeleportingToOwner().setCanSwim()).setDropRate(DropRateProperty.NEVER).setSlot(SoldierEquipmentSlot.BACKPACK).setPickUpPriority(SoldierPickUpPriority.HIGH).build());
        addHoldable(Items.CHORUS_FRUIT, SoldierHoldableEffect.of(SoldierPropertyMap.builder().allowTeleporting().size(0.9f)).addPickUpEffect(new ClayPoiFunctions.DyeSoldierFunction(0x8E678D, true))
                .setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).removalCondition(RemovalConditionType.ON_TELEPORT, new OnTeleportCondition(RemovalConditionContext.MovementType.TO_TARGET, 0.2f)).build(), DefaultSoldierItemTypes.MAGICIAN);

        addHoldable(Items.FIREWORK_ROCKET, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setEvacuation(IEvacuationProperty.FIREWORK)).removalCondition(RemovalConditionType.ON_ESCAPE, new OnEscapeCondition(1f)).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(), DefaultSoldierItemTypes.ARSONIST, DefaultSoldierItemTypes.SPECIALIST);

        addHoldable(Items.FIREWORK_STAR, SoldierHoldableEffect.of(SoldierPropertyMap.builder()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).setPickUpPriority(SoldierPickUpPriority.LOW).setDropRate(DropRateProperty.NEVER).build());
        addWearable(Items.LILY_PAD,
                SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(2f).setCanSwim()).setSlot(SoldierEquipmentSlot.LEGS).setPredicate(ClayPredicates.LogicPredicate.not(new ClayPredicates.SoldierPropertyPredicate(ClayPredicates.PropertyTestType.INCREASE, SoldierPropertyTypes.HEAVY.get()))).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.IRON_LEGGINGS).color(0x208030).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.DIVER);

        addWearable(Items.RED_MUSHROOM, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1f).immunity(MobEffects.POISON, EffectImmunityType.IMMUNE)).setSlot(SoldierEquipmentSlot.HEAD).build(),
                SoldierMultiWearable.accessory(SoldierAccessorySlot.HEAD_ITEM, new SkullRenderable(Items.RED_MUSHROOM_BLOCK)), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.SPECIALIST);
        addWearable(Items.LEATHER, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(3f)).setSlot(SoldierEquipmentSlot.CHEST).setPickUpPriority(SoldierPickUpPriority.LOW).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.LEATHER_CHESTPLATE).color(DyedItemColor.LEATHER_COLOR).affectedOffsetColor().build(), DefaultSoldierItemTypes.ARMORED);
        addWearable(Items.GLASS_PANE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1f).setSeeInvis()).setSlot(SoldierEquipmentSlot.HEAD).build(),
                SoldierWearableBuilder.empty()
                        .addTrim(TrimPatterns.EYE, TrimMaterials.QUARTZ)
                        .addTrim(TrimPatterns.SPIRE, TrimMaterials.NETHERITE).build(), DefaultSoldierItemTypes.SPECIALIST, DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.FASHION);
        addWearable(ModTags.Items.SOLDIER_RGB_GLASSES, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1f).setSeeInvis()).setSlot(SoldierEquipmentSlot.HEAD).build(),
                SoldierMultiWearable.of().put(
                        SoldierEquipmentSlot.HEAD, SoldierWearableBuilder.empty()
                                .addTrim(TrimPatterns.EYE, TrimMaterials.QUARTZ, ColorHelper.jeb())
                                .addTrim(TrimPatterns.SPIRE, TrimMaterials.NETHERITE).build()
                ).build(), DefaultSoldierItemTypes.FASHION);
        addWearable(Items.PAPER, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(0.1f))
                        .setSlot(SoldierEquipmentSlot.CAPE).setDropRate(DropRateProperty.NEVER).build(),
                SoldierMultiWearable.of()
                        .put(SoldierAccessorySlot.CAPE, new CapeRenderable(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/paper_cape.png")))
                        .build(), DefaultSoldierItemTypes.FASHION, DefaultSoldierItemTypes.ARMORED);
        addWearable(Items.BAMBOO, SoldierHoldableEffect.of(SoldierPropertyMap.builder().infiniteBreathHold()).setSlot(SoldierEquipmentSlot.HEAD).build(), SoldierMultiWearable.of().put(SoldierAccessorySlot.SNORKEL, new SnorkelRenderable(SnorkelRenderable.BAMBOO_STICK_TEXTURE)).build(), DefaultSoldierItemTypes.DIVER);
        addWearable(Items.BRICK, SoldierHoldableEffect.of(SoldierPropertyMap.builder().noBreathHold().setProtection(10f).heavy(5f).addAttribute(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "brick_armor_slow"), -0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))).setSlot(SoldierEquipmentSlot.CHEST).setPickUpPriority(SoldierPickUpPriority.HIGH).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.NETHERITE_CHESTPLATE).color(0xA8533B).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.TANK);
        addWearable(Items.TURTLE_SCUTE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setBreathHold(30).setProtection(2f)).setSlot(SoldierEquipmentSlot.HEAD).build(),
                SoldierWearableBuilder.armor(((ArmorItem) Items.TURTLE_HELMET)).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.DIVER);
        addWearable(Items.SLIME_BLOCK,
                SoldierHoldableEffect.of(SoldierPropertyMap.builder().canBounce()).setSlot(SoldierEquipmentSlot.FEET).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.IRON_BOOTS).color(0x77b568).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.SPECIALIST);

        addWearable(Items.GOLD_INGOT, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1.5f).size(1.25f).attackType(AttackTypeProperty.KING)).setSlot(SoldierEquipmentSlot.HEAD).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                        .setDropRate(DropRateProperty.NEVER).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.GOLDEN_HELMET).addTrim(TrimPatterns.SHAPER, TrimMaterials.LAPIS).build(), DefaultSoldierItemTypes.ROYALTY);
        addWearable(Items.GOLD_BLOCK, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(2f).addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 1f, 0.75f)).size(1.1f)).setSlots(SoldierEquipmentSlot.CUSTOM_EQUIP).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                        .setDropRate(DropRateProperty.NEVER).build(),
                SoldierMultiWearable.single(SoldierEquipmentSlot.CHEST, SoldierWearableBuilder.armor((ArmorItem) Items.CHAINMAIL_CHESTPLATE).addTrim(TrimPatterns.SHAPER, TrimMaterials.GOLD).addTrim(TrimPatterns.VEX, TrimMaterials.GOLD).build()));
        addWearable(Items.DIAMOND, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1.5f).size(1.1f).attackType(AttackTypeProperty.QUEEN)).setSlot(SoldierEquipmentSlot.HEAD).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                        .setDropRate(DropRateProperty.NEVER).build(),
                SoldierWearableBuilder.empty().addTrim(TrimPatterns.HOST, TrimMaterials.EMERALD).addTrim(TrimPatterns.WAYFINDER, TrimMaterials.DIAMOND).build(), DefaultSoldierItemTypes.ROYALTY);
        addWearable(Items.DIAMOND_BLOCK, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(1f).addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 1f, 0.5f)).size(1.1f)).setSlots(SoldierEquipmentSlot.CUSTOM_EQUIP).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                .setDropRate(DropRateProperty.NEVER).build(), SoldierMultiWearable.single(SoldierEquipmentSlot.CHEST, SoldierWearableBuilder.empty()
                .addTrim(TrimPatterns.SHAPER, TrimMaterials.EMERALD).addTrim(TrimPatterns.VEX, TrimMaterials.DIAMOND).build()
        ));
        addWearable(Items.CACTUS, SoldierHoldableEffect.of(SoldierPropertyMap.builder().addCounterAttack(new SpecialAttacks.Thorns(SpecialAttackType.MELEE_AND_RANGED, 1f)).setProtection(1f)).setSlot(SoldierEquipmentSlot.CHEST).build(),
                SoldierWearableBuilder.armor((ArmorItem) Items.NETHERITE_CHESTPLATE).color(0x649832).addTrim(TrimPatterns.RIB, TrimMaterials.NETHERITE).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.TANK);

        addWearable(Items.STRING, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setExplosionResistance(24)).setMaxStackSize(2).removalCondition(RemovalConditionType.ON_HURT, new OnHurtCondition(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_EXPLOSION)), 0.5f)).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.STRING, new StringRenderLayer()).build(), DefaultSoldierItemTypes.ARMORED, DefaultSoldierItemTypes.SPECIALIST);

        addWearable(Items.FEATHER, SoldierHoldableEffect.of(SoldierPropertyMap.builder().allowGliding()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.GLIDER, new GliderRenderable(Items.FEATHER)).build(), DefaultSoldierItemTypes.SPECIALIST);
        addWearable(Items.RABBIT_HIDE, SoldierHoldableEffect.of(SoldierPropertyMap.builder().allowGliding()).setSlots(SoldierEquipmentSlot.BACKPACK_SLOTS).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.GLIDER, new GliderRenderable(Items.RABBIT_HIDE)).build(), DefaultSoldierItemTypes.SPECIALIST);

        addWearable(Items.SKELETON_SKULL,
                SoldierHoldableEffect.of(SoldierPropertyMap.builder().canReviveOther(new ReviveProperty(ReviveType.NECROTIC, 1, 0.5f, 20))).setSlot(SoldierEquipmentSlot.HEAD).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.HEAD_ITEM, new SkullRenderable(Items.SKELETON_SKULL)).build(), DefaultSoldierItemTypes.MAGICIAN);
        addWearable(Items.WITHER_SKELETON_SKULL, SoldierHoldableEffect.of(SoldierPropertyMap.builder().canReviveOther(new ReviveProperty(ReviveType.DARK_NECROTIC, 2, 0.5f, 80))
                        .addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 1f, 0.5f))
                        .addDeathCloudEffect(new DeathCloudProperty(MobEffects.WITHER, 0, 10)).immunity(MobEffects.WITHER, EffectImmunityType.IMMUNE))
                .setSlot(SoldierEquipmentSlot.HEAD).setDropRate(DropRateProperty.NORMAL).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                .addPickUpEffect(ClayPoiFunctions.SetItem.drop(Items.COAL_BLOCK.getDefaultInstance(), SoldierEquipmentSlot.FEET))
                .addPickUpEffect(ClayPoiFunctions.SetItem.drop(Items.COAL_BLOCK.getDefaultInstance(), SoldierEquipmentSlot.LEGS))
                .addPickUpEffect(ClayPoiFunctions.SetItem.drop(Items.COAL_BLOCK.getDefaultInstance(), SoldierEquipmentSlot.CHEST))
                .build(), SoldierMultiWearable.of().put(SoldierAccessorySlot.HEAD_ITEM, new SkullRenderable(Items.WITHER_SKELETON_SKULL)).build(), DefaultSoldierItemTypes.MAGICIAN);
        addWearable(Items.COAL_BLOCK, SoldierHoldableEffect.of(SoldierPropertyMap.builder().setProtection(2f))
                        .setSlots(SoldierEquipmentSlot.NO_SLOT).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH)
                        .setDropRate(DropRateProperty.NEVER).build(),
                SoldierMultiWearable.of()
                        .put(SoldierEquipmentSlot.CHEST, SoldierWearableBuilder.armor((ArmorItem) Items.LEATHER_CHESTPLATE).color(0x1D1D21).build())
                        .put(SoldierEquipmentSlot.LEGS, SoldierWearableBuilder.armor((ArmorItem) Items.LEATHER_LEGGINGS).color(0x1D1D21).build())
                        .put(SoldierEquipmentSlot.FEET, SoldierWearableBuilder.armor((ArmorItem) Items.LEATHER_BOOTS).color(0x1D1D21).build()
                        ).build()
        );

        addWearable(Items.BOWL,
                SoldierHoldableEffect.of(SoldierPropertyMap.builder().damageBlock(0.5f, 2f)).setSlots(SoldierEquipmentSlot.HANDS).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.SHIELD, new ShieldRenderable(ShieldRenderable.SHIELD_TEXTURE)).build(),
                DefaultSoldierItemTypes.TANK, DefaultSoldierItemTypes.BASIC
        );
        addWearable(Items.IRON_NUGGET,
                SoldierHoldableEffect.of(SoldierPropertyMap.builder().damageBlock(0.5f, 3f).addCounterAttack(new SpecialAttacks.Thorns(SpecialAttackType.MELEE, 1))).setSlots(SoldierEquipmentSlot.CUSTOM_EQUIP).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.SHIELD, new ShieldRenderable(ShieldRenderable.STUDDED_SHIELD_TEXTURE)).build(),
                DefaultSoldierItemTypes.TANK
        );
        addWearable(Items.COMMAND_BLOCK, SoldierHoldableEffect.of(SoldierPropertyMap.builder()
                                .heavy(5).size(1.4f).setDamage(10f).glowing().glowOutline()
                                .setProtection(25f).explosion(2).attackType(AttackTypeProperty.AGGRESSIVE)
                                .addDeathCloudEffect(new DeathCloudProperty(MobEffects.HARM, 5, 1))
                                .infiniteBreathHold().setCanSwim().setSetOnFire(2)
                                .addSpecialAttack(new SpecialAttacks.SneakAttack(SpecialAttackType.MELEE_AND_RANGED, 2f))
                                .addSpecialAttack(new SpecialAttacks.LightningAttack(SpecialAttackType.MELEE_AND_RANGED, 1f))
                                .addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE_AND_RANGED, 7f, 0.5f))
                                .addCounterAttack(new SpecialAttacks.Thorns(SpecialAttackType.MELEE_AND_RANGED, 2f))
                                .damageBlock(0.5f, 2f, false)
                                .immunity(MobEffects.POISON, EffectImmunityType.IMMUNE)
                                .immunity(MobEffects.WITHER, EffectImmunityType.IMMUNE)
                                .immunity(MobEffects.WEAKNESS, EffectImmunityType.IMMUNE)
                                .immunity(MobEffects.BLINDNESS, EffectImmunityType.IMMUNE)
                                .immunity(MobEffects.REGENERATION, EffectImmunityType.PERSISTENT)
                                .wraith(new WraithProperty(13, 3.5f, List.of(
                                        new SpecialAttacks.LightningAttack(SpecialAttackType.MELEE, 1f),
                                        new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 7f, 0.5f)
                                )))
                                .addAttribute(Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "soldier_com_health"), 30, AttributeModifier.Operation.ADD_VALUE))
                        ).setDropRate(DropRateProperty.NEVER)
                        .addPickUpEffect(ClayPoiFunctions.EffectFunction.addEffect(MobEffects.REGENERATION, 300, 3))
                        .addPickUpEffect(ClayPoiFunctions.EffectFunction.addEffect(MobEffects.REGENERATION, 100000, 0))
                        .addPickUpEffect(new ClayPoiFunctions.DyeSoldierFunction(ColorHelper.jeb(), false))
                        .setSlot(SoldierEquipmentSlot.HEAD).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).build(),
                SoldierMultiWearable.of().put(SoldierAccessorySlot.HEAD_ITEM, new SkullRenderable(Items.COMMAND_BLOCK)).build()
        );
        addHoldable(Items.DEBUG_STICK, SoldierHoldableEffect.of(SoldierPropertyMap.builder()
                        .setDamage(25f).setSetOnFire(80).setSeeInvis().bonusAttackRange(0.2f).attackType(AttackTypeProperty.AGGRESSIVE)
                        .addSpecialAttack(new SpecialAttacks.CritAttack(SpecialAttackType.MELEE, 25f, 0.25f))
                ).setPickUpPriority(SoldierPickUpPriority.VERY_HIGH).setDropRate(DropRateProperty.NEVER).setSlots(SoldierEquipmentSlot.HANDS)
                .build(), TagType.WEAPON);


        addSoldierItemPoi(Items.FLINT, new SoldierPoi(
                ClayPoiFunctions.SetItem.replace(ModItems.SHARPENED_STICK.get().getDefaultInstance(), SoldierEquipmentSlot.MAINHAND),
                new ClayPredicates.ItemPredicate(Items.STICK, SoldierEquipmentSlot.MAINHAND), 0.15f)
        );
        addSoldierItemPoi(Items.HONEY_BOTTLE, new SoldierPoi(
                ClayPoiFunctions.EffectFunction.removeEffect(MobEffects.POISON),
                new ClayPredicates.EffectPredicate(MobEffects.POISON), 0.1f)
        );
        addSoldierItemPoi(Items.HEART_OF_THE_SEA, new SoldierPoi(
                ClayPoiFunctions.EffectFunction.addEffect(MobEffects.CONDUIT_POWER, 360, 0),
                ClayPredicates.LogicPredicate.not(new ClayPredicates.EffectPredicate(MobEffects.CONDUIT_POWER)),
                0)
        );
        addSoldierItemPoi(ModTags.Items.DYES, new SoldierPoi(
                new ClayPoiFunctions.DyeSoldierFunction(ColorGetterFunction.FROM_DYE, true),
                ClayPredicates.LogicPredicate.not(ClayPredicates.ConstantPredicate.getHasCustomColor()),
                0)
        );
        addSoldierItemPoi(Items.NETHER_WART, new SoldierPoi(
                new ClayPoiFunctions.ConvertTo(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), VAMPIRE_TAG),
                ClayPredicates.LogicPredicate.not(
                        ClayPredicates.SoldierPropertyPredicate.isExactly(SoldierPropertyTypes.ATTACK_TYPE.get(), AttackTypeProperty.VAMPIRE.ordinal())
                ),
                1));
        addSoldierItemPoi(Items.ROTTEN_FLESH, new SoldierPoi(
                new ClayPoiFunctions.ConvertTo(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ZOMBIE_TAG),
                ClayPredicates.LogicPredicate.not(
                        ClayPredicates.SoldierPropertyPredicate.isExactly(SoldierPropertyTypes.ATTACK_TYPE.get(), AttackTypeProperty.ZOMBIE.ordinal())
                ),
                1));
        addSoldierBlockPoi(Blocks.END_ROD, new SoldierPoi(
                new ClayPoiFunctions.ConvertTo(ModEntityTypes.CLAY_WRAITH.get(), CLAY_WRAITH_TAG),
                ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(),
                0));

        addSoldierItemPoi(Items.BOOK, new SoldierPoi(
                ClayPoiFunctions.SetItem.drop(Items.PAPER.getDefaultInstance(), null),
                ClayPredicates.ItemPredicate.suitable(Items.PAPER),
                0.05f));

        addSoldierItemPoi(Items.EGG, new SoldierPoi(List.of(
                ClayPoiFunctions.SelectRandom.allEqual(
                        new ClayPoiFunctions.ConvertTo(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get(), DEFAULT_BOSS_TAG),
                        new ClayPoiFunctions.ConvertTo(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get(), VAMPIRE_BOSS_TAG),
                        ClayPoiFunctions.EffectFunction.addEffect(MobEffects.WITHER, 340, 9)
                )), ClayPredicates.ConstantPredicate.getAlwaysTruePredicate(),
                1f
        ));
    }

    @SafeVarargs
    private void addItemToTags(Item item, TagKey<Item>... tags) {
        for (TagKey<Item> tag : tags) {
            this.tag(tag).add(item);
        }
    }
}