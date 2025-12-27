package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.recipe.BrickedItemReviveRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCookingRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCraftingRecipe;
import net.bumblebee.claysoldiers.recipe.ShearBladeRecipe;
import net.minecraft.world.item.crafting.*;

import java.util.function.Supplier;

public final class ModRecipes {
    public static final Supplier<RecipeSerializer<ClaySoldierCraftingRecipe>> CLAY_SOLDIER_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_crafting",
            () -> new CustomRecipe.Serializer<>(ClaySoldierCraftingRecipe::new)
    );
    public static final Supplier<RecipeSerializer<BrickedItemReviveRecipe>> CLAY_SOLDIER_REVIVING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_reviving",
            () -> new CustomRecipe.Serializer<>(BrickedItemReviveRecipe::new)
    );
    public static final Supplier<RecipeSerializer<ShearBladeRecipe>> SHEAR_BLADE_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("shear_blade_crafting",
            () -> new CustomRecipe.Serializer<>(ShearBladeRecipe::new)
    );

    public static final Supplier<RecipeSerializer<SmeltingRecipe>> CLAY_SOLDIER_SMELTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smelting",
            () -> new AbstractCookingRecipe.Serializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smelting(), 100)
    );
    public static final Supplier<RecipeSerializer<BlastingRecipe>> CLAY_SOLDIER_BLASTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_blasting",
            () -> new AbstractCookingRecipe.Serializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.blasting(), 50)
    );
    public static final Supplier<RecipeSerializer<CampfireCookingRecipe>> CLAY_SOLDIER_CAMPFIRE = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_campfire",
            () -> new AbstractCookingRecipe.Serializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.campfire(), 200)
    );
    public static final Supplier<RecipeSerializer<SmokingRecipe>> CLAY_SOLDIER_SMOKING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smoking",
            () -> new AbstractCookingRecipe.Serializer<>((pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime) -> ClaySoldierCookingRecipe.smoking(), 300)
    );

    private ModRecipes() {
    }

    public static void init() {}
}
