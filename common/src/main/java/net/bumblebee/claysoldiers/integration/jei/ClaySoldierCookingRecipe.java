package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class ClaySoldierCookingRecipe {
    private static final String GROUP = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.cooking";
    private static final String ID = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.%s.%s";

    public static <T extends AbstractCookingRecipe> List<RecipeHolder<T>> createCookingRecipe(CookingRecipeBuilder<T> factory, int smeltingTime) {
        ItemStack output = ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance();
        Ingredient input = Ingredient.of(ClayMobTeamManger.getAllKeys(Minecraft.getInstance().level.registryAccess())
                .map(team -> ClayMobTeamManger.createStackForTeam(team, Minecraft.getInstance().level.registryAccess())));

        T cookingRecipe = factory.build(GROUP, CookingBookCategory.MISC, input, output, 1, smeltingTime);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, ID.formatted(cookingRecipe.category().getSerializedName(), output.getDescriptionId()));

        return List.of(new RecipeHolder<>(id, cookingRecipe));
    }

    @FunctionalInterface
    public interface CookingRecipeBuilder<T extends AbstractCookingRecipe> {
        T build(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int smeltingTime);
    }

    private ClaySoldierCookingRecipe() {
    }
}
