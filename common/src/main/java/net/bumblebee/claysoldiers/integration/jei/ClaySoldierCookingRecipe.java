package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public final class ClaySoldierCookingRecipe {
    private static final String GROUP = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.cooking";
    private static final Recipe.CommonInfo COMMON_INFO = new Recipe.CommonInfo(false);
    private static final AbstractCookingRecipe.CookingBookInfo INFO = new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, GROUP);

    private static final String ID = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.%s.%s";

    public static <T extends AbstractCookingRecipe> List<RecipeHolder<T>> createCookingRecipe(CookingRecipeBuilder<T> factory, int smeltingTime, RegistryAccess registries) {
        var res = new ArrayList<RecipeHolder<T>>();

        Holder.Reference<ClayMobTeam> team = ClayMobTeamManger.getDefault(registries);
        ItemStackTemplate output = new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get(), DataComponentPatch.builder().set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team.key()).build());
        Ingredient input = Ingredient.of(ModItems.CLAY_SOLDIER);

        T cookingRecipe = factory.build(COMMON_INFO, INFO, input, output, 1, smeltingTime);
        Identifier id = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, ID.formatted(cookingRecipe.category().getSerializedName(), team.key().identifier().getPath()));

        res.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), cookingRecipe));

        return res;
    }

    @FunctionalInterface
    public interface CookingRecipeBuilder<T extends AbstractCookingRecipe> {
        T build(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo cookingBookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int smeltingTime);
    }

    private ClaySoldierCookingRecipe() {
    }
}
