package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.recipe.BrickedItemReviveRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCookingRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCraftingRecipe;
import net.bumblebee.claysoldiers.recipe.ShearBladeRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    private static final String CLAY_HORSE_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_horse";
    private static final String CLAY_PEGASUS_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_pegasus";
    private static final String CLAY_DISRUPTOR_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_pegasus";



    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        SpecialRecipeBuilder.special(ClaySoldierCraftingRecipe::new).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_crafting"));
        SpecialRecipeBuilder.special(BrickedItemReviveRecipe::new).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_reviving"));
        SpecialRecipeBuilder.special(ShearBladeRecipe::new).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "shear_blade"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.smelting()).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_smelting"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.blasting()).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_blasting"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.campfire()).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_campfire"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.smoking()).save(recipeOutput, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_smoking"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLAY_SOLDIER.get(), 4)
                .define('E', Items.CLAY_BALL)
                .define('S', Items.SOUL_SAND)
                .pattern("E")
                .pattern("S")
                .unlockedBy("has_clay", has(Items.CLAY))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.SHEARS)
                .define('E', ModItems.SHEAR_BLADE.get())
                .pattern("E ")
                .pattern(" E")
                .unlockedBy("has_shear_blade", has(ModItems.SHEAR_BLADE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLAY_DISRUPTOR.get())
                .define('C', Items.CLAY)
                .define('R', Items.REDSTONE)
                .define('S', Items.STICK)
                .pattern("CSC")
                .pattern("CRC")
                .group(CLAY_DISRUPTOR_GROUP_NAME)
                .unlockedBy("has_clay", has(Items.CLAY))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TERRACOTTA_DISRUPTOR.get())
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
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLAY_COOKIE.get(), 4)
                .define('#', Items.CLAY_BALL)
                .define('R', Items.GHAST_TEAR)
                .pattern("#R#")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLAY_GOGGLES.get())
                .define('#', Items.GLASS_PANE)
                .define('R', Items.COPPER_INGOT)
                .define('S', Items.LEATHER)
                .pattern(" S ")
                .pattern("#R#")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLAY_BRUSH.get())
                .define('#', Items.CLAY_BALL)
                .define('C', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .pattern("#")
                .pattern("C")
                .pattern("S")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLUEPRINT_PAGE.get())
                .define('#', Items.LAPIS_LAZULI)
                .define('C', Items.PAPER)
                .define('S', Items.CLAY_BALL)
                .pattern("#")
                .pattern("C")
                .pattern("S")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ESCRITOIRE_BLOCK)
                .define('#', ModItems.BLUEPRINT_PAGE)
                .define('C', ItemTags.PLANKS)
                .pattern("##")
                .pattern("CC")
                .pattern("CC")
                .unlockedBy("has_blueprint", has(ModItems.BLUEPRINT_PAGE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.EASEL_BLOCK.get())
                .define('#', Items.STICK)
                .pattern(" # ")
                .pattern("###")
                .pattern("# #")
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HAMSTER_WHEEL_BLOCK.get())
                .define('#', Items.COPPER_INGOT)
                .define('C', Items.STICK)
                .define('S', Items.STONE)
                .pattern(" # ")
                .pattern("#C#")
                .pattern("S#S")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        clayHorseRecipe(recipeOutput, ClayHorseVariants.CAKE, Items.CAKE);
        clayHorseRecipe(recipeOutput, ClayHorseVariants.GRASS, Items.GRASS_BLOCK);
        clayHorseRecipe(recipeOutput, ClayHorseVariants.SNOW, Items.SNOW_BLOCK);
        clayHorseRecipe(recipeOutput, ClayHorseVariants.MYCELIUM, Items.MYCELIUM);

        stonecutterResultFromBase(recipeOutput, RecipeCategory.COMBAT, ModItems.SHARPENED_STICK.get(), Items.STICK);
    }

    private static void clayHorseRecipe(RecipeOutput recipeOutput, ClayHorseVariants variant, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayHorseVariants.clayHorseByVariant(variant).get(), 2)
                .define('C', Items.CLAY_BALL)
                .define('H', material)
                .pattern("CHC")
                .pattern("C C")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group(CLAY_HORSE_GROUP_NAME)
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ClayHorseVariants.clayPegasusByVariant(variant).get(), 2)
                .define('C', Items.CLAY_BALL)
                .define('H', material)
                .define('F', Items.FEATHER)
                .pattern(" F ")
                .pattern("CHC")
                .pattern("C C")
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group(CLAY_PEGASUS_GROUP_NAME)
                .save(recipeOutput);
    }

}
