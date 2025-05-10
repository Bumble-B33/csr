package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ClaySoldierCookingRecipe extends AbstractCookingRecipe {
    private final Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> serializer;

    private ClaySoldierCookingRecipe(Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> serializer, RecipeType<?> pType, int cookingTime) {
        super(pType, "clay_mob_cooking", CookingBookCategory.MISC, Ingredient.of(ModItems.CLAY_SOLDIER.get()), ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance(), 1, cookingTime);
        this.serializer = serializer;
    }

    public static ClaySoldierCookingRecipe smelting() {
        return new ClaySoldierCookingRecipe(ModRecipes.CLAY_SOLDIER_SMELTING, RecipeType.SMELTING, 100);
    }
    public static ClaySoldierCookingRecipe blasting() {
        return new ClaySoldierCookingRecipe(ModRecipes.CLAY_SOLDIER_BLASTING, RecipeType.BLASTING, 50);
    }
    public static ClaySoldierCookingRecipe campfire() {
        return new ClaySoldierCookingRecipe(ModRecipes.CLAY_SOLDIER_CAMPFIRE, RecipeType.CAMPFIRE_COOKING, 200);
    }
    public static ClaySoldierCookingRecipe smoking() {
        return new ClaySoldierCookingRecipe(ModRecipes.CLAY_SOLDIER_SMOKING, RecipeType.CAMPFIRE_COOKING, 300);
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level p_345375_) {
        return recipeInput.item().getItem() instanceof BrickedItemHolder;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput singleRecipeInput, HolderLookup.Provider p_346030_) {
        var stack = singleRecipeInput.item();
        var bricked = (BrickedItemHolder) stack.getItem();
        return bricked.getBrickedItem(stack);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public RecipeSerializer<ClaySoldierCookingRecipe> getSerializer() {
        return serializer.get();
    }
}
