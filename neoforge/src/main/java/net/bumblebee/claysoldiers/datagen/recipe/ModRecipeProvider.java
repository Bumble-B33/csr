package net.bumblebee.claysoldiers.datagen.recipe;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.recipe.BrickedItemReviveRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCookingRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCraftingRecipe;
import net.bumblebee.claysoldiers.recipe.ShearBladeRecipe;
import net.bumblebee.claysoldiers.recipe.chip.AddonChipRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    private static final String CLAY_HORSE_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_horse";
    private static final String CLAY_PEGASUS_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_pegasus";
    private static final String CLAY_DISRUPTOR_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_disruptor";
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_CRAFTING = createResourceKey("clay_soldier");
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_REVIVE = createResourceKey("clay_soldier_reviving");
    public static final ResourceKey<Recipe<?>> BLUEPRINT_PAGE = createResourceKey("blueprint_page");
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_CHIP = createResourceKey("clay_soldier_chip");

    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_CHIP_ADD_ADDON = createResourceKey("clay_soldier_chip_add_addon");
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_BLUEPRINT_CHIP = createResourceKey("blueprint_clay_soldier_chip");

    private final RecipeOutput recipeOutput;


    private ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.recipeOutput = output;
    }

    public static Runner create(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
         return new Runner(packOutput, registries);
    }

    @Override
    protected void buildRecipes() {
        SpecialRecipeBuilder.special(ClaySoldierCraftingRecipe::new).save(recipeOutput, createResourceKey("clay_soldier_crafting"));
        SpecialRecipeBuilder.special(BrickedItemReviveRecipe::new).save(recipeOutput, CLAY_SOLDIER_REVIVE);
        SpecialRecipeBuilder.special(ShearBladeRecipe::new).save(recipeOutput, createResourceKey("shear_blade"));
        SpecialRecipeBuilder.special(ClaySoldierCookingRecipe::smelting).save(recipeOutput, createResourceKey("clay_soldier_smelting"));
        SpecialRecipeBuilder.special(ClaySoldierCookingRecipe::blasting).save(recipeOutput, createResourceKey("clay_soldier_blasting"));
        SpecialRecipeBuilder.special(ClaySoldierCookingRecipe::campfire).save(recipeOutput, createResourceKey("clay_soldier_campfire"));
        SpecialRecipeBuilder.special(ClaySoldierCookingRecipe::smoking).save(recipeOutput, createResourceKey("clay_soldier_smoking"));

        shaped(RecipeCategory.MISC, ModItems.CLAY_SOLDIER.get(), 4)
                .define('E', Items.CLAY_BALL)
                .define('S', Items.SOUL_SAND)
                .pattern("E")
                .pattern("S")
                .unlockedBy("has_clay", has(Items.CLAY))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput, CLAY_SOLDIER_CRAFTING);
        shapeless(RecipeCategory.TOOLS, Items.SHEARS)
                .requires(ModItems.SHEAR_BLADE.get())
                .requires(ModItems.SHEAR_BLADE.get())
                .unlockedBy("has_shear_blade", has(ModItems.SHEAR_BLADE.get()))
                .save(recipeOutput);

        shaped(RecipeCategory.TOOLS, ModItems.CLAY_DISRUPTOR.get())
                .define('C', Items.CLAY)
                .define('R', Items.REDSTONE)
                .define('S', Items.STICK)
                .pattern("CSC")
                .pattern("CRC")
                .group(CLAY_DISRUPTOR_GROUP_NAME)
                .unlockedBy("has_clay", has(Items.CLAY))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);
        shaped(RecipeCategory.TOOLS, ModItems.TERRACOTTA_DISRUPTOR.get())
                .define('C', ItemTags.TERRACOTTA)
                .define('R', ModItems.CLAY_DISRUPTOR)
                .define('S', Items.REDSTONE)
                .pattern("CSC")
                .pattern("CRC")
                .group(CLAY_DISRUPTOR_GROUP_NAME)
                .unlockedBy("has_clay", has(Items.CLAY))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .unlockedBy("has_terracotta", has(Items.TERRACOTTA))
                .save(recipeOutput);
        shaped(RecipeCategory.TOOLS, ModItems.CLAY_COOKIE.get(), 4)
                .define('#', Items.CLAY_BALL)
                .define('R', Items.GHAST_TEAR)
                .pattern("#R#")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);
        shaped(RecipeCategory.TOOLS, ModItems.CLAY_GOGGLES.get())
                .define('#', Items.GLASS_PANE)
                .define('R', Items.COPPER_INGOT)
                .define('S', Items.LEATHER)
                .pattern(" S ")
                .pattern("#R#")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(recipeOutput);

        shaped(RecipeCategory.TOOLS, ModItems.SLIME_BOOTS.get())
                .define('#', Items.SLIME_BLOCK)
                .define('S', Items.COPPER_INGOT)
                .pattern("S S")
                .pattern("# #")
                .unlockedBy("has_slime_ball", has(Items.SLIME_BLOCK))
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(recipeOutput);

        shaped(RecipeCategory.TOOLS, ModItems.CLAY_BRUSH.get())
                .define('#', Items.CLAY_BALL)
                .define('C', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .pattern("#")
                .pattern("C")
                .pattern("S")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        shaped(RecipeCategory.TOOLS, ModItems.STATOMETER.get())
                .define('#', Items.IRON_INGOT)
                .define('C', Items.CLAY_BALL)
                .define('R', Items.REDSTONE)
                .pattern("  #")
                .pattern("#R#")
                .pattern("#C#")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        shaped(RecipeCategory.MISC, ModItems.BLUEPRINT_PAGE.get())
                .define('#', Items.LAPIS_LAZULI)
                .define('C', Items.PAPER)
                .define('S', Items.CLAY_BALL)
                .pattern("#")
                .pattern("C")
                .pattern("S")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput, BLUEPRINT_PAGE);

        shaped(RecipeCategory.DECORATIONS, ModBlocks.ESCRITOIRE_BLOCK)
                .define('#', ModItems.BLUEPRINT_PAGE)
                .define('C', ItemTags.PLANKS)
                .pattern("##")
                .pattern("CC")
                .pattern("CC")
                .unlockedBy("has_blueprint", has(ModItems.BLUEPRINT_PAGE))
                .save(recipeOutput);

        shaped(RecipeCategory.DECORATIONS, ModBlocks.EASEL_BLOCK.get())
                .define('#', Items.STICK)
                .pattern(" # ")
                .pattern("###")
                .pattern("# #")
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        shaped(RecipeCategory.DECORATIONS, ModBlocks.HAMSTER_WHEEL_BLOCK.get())
                .define('#', Items.COPPER_INGOT)
                .define('C', Items.STICK)
                .define('S', Items.STONE)
                .pattern(" # ")
                .pattern("#C#")
                .pattern("S#S")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        clayHorseRecipe(ClayHorseVariants.CAKE, Items.CAKE);
        clayHorseRecipe(ClayHorseVariants.GRASS, Items.GRASS_BLOCK);
        clayHorseRecipe(ClayHorseVariants.SNOW, Items.SNOW_BLOCK);
        clayHorseRecipe(ClayHorseVariants.MYCELIUM, Items.MYCELIUM);

        stonecutterResultFromBase(RecipeCategory.COMBAT, ModItems.SHARPENED_STICK.get(), Items.STICK);

        shaped(RecipeCategory.DECORATIONS, ModBlocks.CHIP_ASSEMBLER.get())
                .define('#', Items.IRON_INGOT)
                .define('C', Items.REDSTONE)
                .define('S', Items.STONE)
                .pattern(" # ")
                .pattern("#C#")
                .pattern("S#S")
                .unlockedBy("has_copper_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.BLANK_CHIP, 8)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.IRON_INGOT)
                .chip(Items.REDSTONE)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(recipeOutput, CLAY_SOLDIER_CHIP);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.POI_CHIP)
                .addInput(Items.LAPIS_LAZULI)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.BREAK_CROPS_CHIP)
                .addInput(Items.STONE_HOE)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.PLACE_SEEDS_CHIP)
                .addInput(Items.STONE_HOE)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.WHEAT_SEEDS)
                .chip(ModItems.BLANK_CHIP)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.PICK_UP_ITEMS_CHIP)
                .addInput(Items.BUNDLE)
                .addInput(Items.IRON_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.DIG_CHIP)
                .addInput(Items.IRON_PICKAXE)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.FISHING_CHIP)
                .addInput(Items.FISHING_ROD)
                .addInput(Items.IRON_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.COMBAT_CHIP)
                .addInput(Items.IRON_SWORD)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.BLUEPRINT_CHIP)
                .addInput(ModItems.BLUEPRINT_PAGE)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.BUNDLE)
                .addInput(Items.IRON_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput, CLAY_SOLDIER_BLUEPRINT_CHIP);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.CHIP, ModItems.BEE_KEEPING_CHIP)
                .addInput(Items.HONEYCOMB)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.HONEYCOMB)
                .addInput(Items.IRON_INGOT)
                .chip(ModItems.BLANK_CHIP)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.BLANK_ADDON, 4)
                .addInput(Items.IRON_INGOT)
                .addInput(Items.RED_DYE)
                .chip(Items.BRICK)
                .setEnergyCost(6)
                .setBuildTime(6)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.TREASURY_ADDON)
                .addInput(Items.NAUTILUS_SHELL)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.NO_BREAK_ADDON)
                .addInput(Items.BREAD)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.RANGE_ADDON)
                .addInput(Items.STICK)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.TARGET_ANIMAL_ADDON)
                .addInput(Items.PORKCHOP)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.TARGET_MONSTER_ADDON)
                .addInput(Items.ROTTEN_FLESH)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.TARGET_IGNORE_BABIES_ADDON)
                .addInput(Ingredient.of(registries.get(ItemTags.EGGS).orElseThrow()))
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(8)
                .setBuildTime(10)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        ChipAssemblyRecipeBuilder.of(ChipAssemblyCategory.ADDON, ModItems.ACCELERATION_ADDON)
                .addInput(Items.SUGAR)
                .addInput(Items.SUGAR)
                .addInput(Items.GOLD_INGOT)
                .chip(ModItems.BLANK_ADDON)
                .setEnergyCost(7)
                .setBuildTime(9)
                .unlockedBy("has_chip", has(ModTags.Items.CHIP))
                .save(recipeOutput);

        SpecialRecipeBuilder.special(() -> AddonChipRecipe.INSTANCE).save(recipeOutput, CLAY_SOLDIER_CHIP_ADD_ADDON);
    }

    private void clayHorseRecipe(ClayHorseVariants variant, ItemLike material) {
        shaped(RecipeCategory.MISC, ClayHorseVariants.clayHorseByVariant(variant).get(), 2)
                .define('C', Items.CLAY_BALL)
                .define('H', material)
                .pattern("CHC")
                .pattern("C C")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group(CLAY_HORSE_GROUP_NAME)
                .save(recipeOutput, getClayHorseKey(variant));
        shaped(RecipeCategory.MISC, ClayHorseVariants.clayPegasusByVariant(variant).get(), 2)
                .define('C', Items.CLAY_BALL)
                .define('H', material)
                .define('F', Items.FEATHER)
                .pattern(" F ")
                .pattern("CHC")
                .pattern("C C")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group(CLAY_PEGASUS_GROUP_NAME)
                .save(recipeOutput, getClayPegasusKey(variant, false));
        shaped(RecipeCategory.MISC, ClayHorseVariants.clayPegasusByVariant(variant).get())
                .define('H', ClayHorseVariants.clayHorseByVariant(variant).get())
                .define('F', Items.FEATHER)
                .pattern("F")
                .pattern("H")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group(CLAY_PEGASUS_GROUP_NAME)
                .save(recipeOutput, getClayPegasusKey(variant, true));

    }

    public static ResourceKey<Recipe<?>> getClayHorseKey(ClayHorseVariants variant) {
        return createResourceKey(variant.getVariantName() + "_horse");
    }
    public static ResourceKey<Recipe<?>> getClayPegasusKey(ClayHorseVariants variant, boolean fromHorse) {
        return createResourceKey(variant.getVariantName() + "_pegasus" + (fromHorse ? "_feather" : ""));
    }

    private static ResourceKey<Recipe<?>> createResourceKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
    }

    public static class Runner extends RecipeProvider.Runner {
        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider provider, @NonNull RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public @NonNull String getName() {
            return ClaySoldiersCommon.MOD_ID + " Recipies";
        }
    }

}
