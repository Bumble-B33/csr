package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ClaySoldierCookingRecipe {
    public static ClaySoldierSmeltingRecipe smelting() {
        return new ClaySoldierSmeltingRecipe(ModRecipes.CLAY_SOLDIER_SMELTING, 100);
    }
    public static ClaySoldierBlastingRecipe blasting() {
        return new ClaySoldierBlastingRecipe(ModRecipes.CLAY_SOLDIER_BLASTING, 50);
    }
    public static ClaySoldierCampfireRecipe campfire() {
        return new ClaySoldierCampfireRecipe(ModRecipes.CLAY_SOLDIER_CAMPFIRE, 200);
    }
    public static ClaySoldierSmokingRecipe smoking() {
        return new ClaySoldierSmokingRecipe(ModRecipes.CLAY_SOLDIER_SMOKING, 300);
    }

    private static boolean recipeMatches(SingleRecipeInput recipeInput) {
        return recipeInput.item().getItem() instanceof BrickedItemHolder;
    }

    private static ItemStack assembleRecipe(SingleRecipeInput singleRecipeInput) {
        var stack = singleRecipeInput.item();
        var bricked = (BrickedItemHolder) stack.getItem();
        return bricked.getBrickedItem(stack);
    }

    public static class ClaySoldierSmeltingRecipe extends SmeltingRecipe  {
        private final Supplier<RecipeSerializer<ClaySoldierSmeltingRecipe>> serializer;

        public ClaySoldierSmeltingRecipe(Supplier<RecipeSerializer<ClaySoldierSmeltingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return serializer.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return assembleRecipe(input);
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return true;
        }
    }

    public static class ClaySoldierBlastingRecipe extends BlastingRecipe  {
        private final Supplier<RecipeSerializer<ClaySoldierBlastingRecipe>> serializer;

        public ClaySoldierBlastingRecipe(Supplier<RecipeSerializer<ClaySoldierBlastingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return serializer.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return assembleRecipe(input);
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return true;
        }
    }

    public static class ClaySoldierCampfireRecipe extends CampfireCookingRecipe  {
        private final Supplier<RecipeSerializer<ClaySoldierCampfireRecipe>> serializer;

        public ClaySoldierCampfireRecipe(Supplier<RecipeSerializer<ClaySoldierCampfireRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return serializer.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return assembleRecipe(input);
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return true;
        }
    }

    public static class ClaySoldierSmokingRecipe extends SmokingRecipe  {
        private final Supplier<RecipeSerializer<ClaySoldierSmokingRecipe>> serializer;

        public ClaySoldierSmokingRecipe(Supplier<RecipeSerializer<ClaySoldierSmokingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return serializer.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return assembleRecipe(input);
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return true;
        }
    }

}
