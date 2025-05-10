package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.recipe.BrickedItemReviveRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCookingRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCraftingRecipe;
import net.bumblebee.claysoldiers.recipe.ShearBladeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

public final class ModRecipes {
    public static final Supplier<RecipeSerializer<ClaySoldierCraftingRecipe>> CLAY_SOLDIER_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_crafting",
            () -> new SimpleCraftingRecipeSerializer<>(ClaySoldierCraftingRecipe::new)
    );
    public static final Supplier<RecipeSerializer<BrickedItemReviveRecipe>> CLAY_SOLDIER_REVIVING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_reviving",
            () -> new SimpleCraftingRecipeSerializer<>(BrickedItemReviveRecipe::new)
    );
    public static final Supplier<RecipeSerializer<ShearBladeRecipe>> SHEAR_BLADE_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("shear_blade_crafting",
            () -> new SimpleCraftingRecipeSerializer<>(ShearBladeRecipe::new)
    );

    public static final Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> CLAY_SOLDIER_SMELTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smelting",
            () -> new SimpleCookingSerializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smelting(), 100)
    );
    public static final Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> CLAY_SOLDIER_BLASTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_blasting",
            () -> new SimpleCookingSerializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smelting(), 50)
    );
    public static final Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> CLAY_SOLDIER_CAMPFIRE = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_campfire",
            () -> new SimpleCookingSerializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smelting(), 200)
    );
    public static final Supplier<RecipeSerializer<ClaySoldierCookingRecipe>> CLAY_SOLDIER_SMOKING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smoking",
            () -> new SimpleCookingSerializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smelting(), 300)
    );

    public static void init() {
    }

    private ModRecipes() {
    }
}
