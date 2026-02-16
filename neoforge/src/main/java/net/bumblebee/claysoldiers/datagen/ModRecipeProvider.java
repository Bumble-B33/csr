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
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    private static final String CLAY_HORSE_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_horse";
    private static final String CLAY_PEGASUS_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_pegasus";
    private static final String CLAY_DISRUPTOR_GROUP_NAME = ClaySoldiersCommon.MOD_ID + ":clay_disruptor";
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_CRAFTING = createResourceKey("clay_soldier");
    public static final ResourceKey<Recipe<?>> CLAY_SOLDIER_REVIVE = createResourceKey("clay_soldier_reviving");
    public static final ResourceKey<Recipe<?>> BLUEPRINT_PAGE = createResourceKey("blueprint_page");


    private final RecipeOutput recipeOutput;

    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.recipeOutput = output;
    }



    @Override
    protected void buildRecipes() {
        SpecialRecipeBuilder.special(ClaySoldierCraftingRecipe::new).save(recipeOutput, createResourceKey("clay_soldier_crafting"));
        SpecialRecipeBuilder.special(BrickedItemReviveRecipe::new).save(recipeOutput, CLAY_SOLDIER_REVIVE);
        SpecialRecipeBuilder.special(ShearBladeRecipe::new).save(recipeOutput, createResourceKey("shear_blade"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.smelting()).save(recipeOutput, createResourceKey("clay_soldier_smelting"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.blasting()).save(recipeOutput, createResourceKey("clay_soldier_blasting"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.campfire()).save(recipeOutput, createResourceKey("clay_soldier_campfire"));
        SpecialRecipeBuilder.special(bc -> ClaySoldierCookingRecipe.smoking()).save(recipeOutput, createResourceKey("clay_soldier_smoking"));

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
        return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
    }

    public static class Runner extends RecipeProvider.Runner {
        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return ClaySoldiersCommon.MOD_ID + " Recipies";
        }
    }

}
