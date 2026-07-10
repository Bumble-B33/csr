package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.jei.JEIShapelessRecipe;
import net.bumblebee.claysoldiers.recipe.*;
import net.bumblebee.claysoldiers.recipe.chip.AddonChipRecipe;
import net.bumblebee.claysoldiers.recipe.chip.BasicChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.minecraft.world.item.crafting.*;

import java.util.function.Supplier;

public final class ModRecipes {
    public static final Supplier<RecipeSerializer<ClaySoldierCraftingRecipe>> CLAY_SOLDIER_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_crafting",
            () -> new RecipeSerializer<>(ClaySoldierCraftingRecipe.CODEC, ClaySoldierCraftingRecipe.STREAM_CODEC)
    );
    public static final Supplier<RecipeSerializer<BrickedItemReviveRecipe>> CLAY_SOLDIER_REVIVING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_reviving",
            () -> new RecipeSerializer<>(BrickedItemReviveRecipe.CODEC, BrickedItemReviveRecipe.STREAM_CODEC)
    );
    public static final Supplier<RecipeSerializer<ShearBladeRecipe>> SHEAR_BLADE_CRAFTING = ClaySoldiersCommon.PLATFORM.registerRecipe("shear_blade_crafting",
            () -> new RecipeSerializer<>(ShearBladeRecipe.CODEC, ShearBladeRecipe.STREAM_CODEC)
    );

    public static final Supplier<RecipeSerializer<SmeltingRecipe>> CLAY_SOLDIER_SMELTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smelting",
            () -> ClaySoldierCookingRecipe.serializer(ClaySoldierCookingRecipe.smelting())
    );
    public static final Supplier<RecipeSerializer<CampfireCookingRecipe>> CLAY_SOLDIER_CAMPFIRE = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_campfire",
            () -> ClaySoldierCookingRecipe.serializer(ClaySoldierCookingRecipe.campfire())
    );
    public static final Supplier<RecipeSerializer<SmokingRecipe>> CLAY_SOLDIER_SMOKING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_smoking",
            () -> ClaySoldierCookingRecipe.serializer(ClaySoldierCookingRecipe.smoking())
    );
    public static final Supplier<RecipeSerializer<BlastingRecipe>> CLAY_SOLDIER_BLASTING = ClaySoldiersCommon.PLATFORM.registerRecipe("clay_soldier_blasting",
            () -> ClaySoldierCookingRecipe.serializer(ClaySoldierCookingRecipe.blasting())
    );

    public static final RecipeBookCategory CHIP_ASSEMBLY = ClaySoldiersCommon.PLATFORM.registerRecipeBookCategory("chip_assembly", new RecipeBookCategory());
    public static final RecipeBookCategory ADDON_ASSEMBLY = ClaySoldiersCommon.PLATFORM.registerRecipeBookCategory("addon_assembly", new RecipeBookCategory());
    public static final RecipeBookCategory MISC_ASSEMBLY = ClaySoldiersCommon.PLATFORM.registerRecipeBookCategory("misc_assembly", new RecipeBookCategory());


    public static final RecipeType<ChipAssemblyRecipe> CHIP_ASSEMBLY_TYPE = ClaySoldiersCommon.PLATFORM.registerRecipeType("chip_assembly", new RecipeType<>() {
        @Override
        public String toString() {
            return ClaySoldiersCommon.MOD_ID + ":chip_assembly";
        }
    });

    public static final Supplier<RecipeSerializer<BasicChipAssemblyRecipe>> CHIP_ASSEMBLY_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerRecipe("chip_assembly",
            () -> new RecipeSerializer<>(BasicChipAssemblyRecipe.CODEC, BasicChipAssemblyRecipe.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<AddonChipRecipe>> ADDON_CHIP_SERIALIZER = ClaySoldiersCommon.PLATFORM.registerRecipe("addon_chip",
            () -> new RecipeSerializer<>(AddonChipRecipe.CODEC, AddonChipRecipe.STREAM_CODEC)
    );

    private ModRecipes() {
    }

    public static void init() {
        ClaySoldiersCommon.PLATFORM.registerRecipe("jei_shapeless_recipe", () -> JEIShapelessRecipe.SERIALIZER);
        ClaySoldiersCommon.PLATFORM.registerSlotDisplay("team_based", TeamBasesItemStackDisplay.TYPE);
    }
}
