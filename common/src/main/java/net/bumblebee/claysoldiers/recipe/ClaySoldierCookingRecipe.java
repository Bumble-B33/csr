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

    public static class ClaySoldierSmeltingRecipe extends SmeltingRecipe {
        private final Supplier<RecipeSerializer<SmeltingRecipe>> serializer;

        public ClaySoldierSmeltingRecipe(Supplier<RecipeSerializer<SmeltingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<SmeltingRecipe> getSerializer() {
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
    }

    public static class ClaySoldierBlastingRecipe extends BlastingRecipe {
        private final Supplier<RecipeSerializer<BlastingRecipe>> serializer;

        public ClaySoldierBlastingRecipe(Supplier<RecipeSerializer<BlastingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<BlastingRecipe> getSerializer() {
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
    }

    public static class ClaySoldierCampfireRecipe extends CampfireCookingRecipe {
        private final Supplier<RecipeSerializer<CampfireCookingRecipe>> serializer;

        public ClaySoldierCampfireRecipe(Supplier<RecipeSerializer<CampfireCookingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
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
    }

    public static class ClaySoldierSmokingRecipe extends SmokingRecipe {
        private final Supplier<RecipeSerializer<SmokingRecipe>> serializer;

        public ClaySoldierSmokingRecipe(Supplier<RecipeSerializer<SmokingRecipe>> serializer, int cookingTime) {
            super("clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
            this.serializer = serializer;
        }

        @Override
        public RecipeSerializer<SmokingRecipe> getSerializer() {
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
    }
}
