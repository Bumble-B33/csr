package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ClaySoldierCrafting {
    private static final String CLAY_SOLDIER_REVIVING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.revive";
    private static final String CLAY_SOLDIER_CRAFTING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.crafting";

    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {

        return ClayMobTeamManger.getAllKeys(getRegistries()).<RecipeHolder<CraftingRecipe>>mapMulti((entry, r) -> {
            for (int i = 1; i <= 8; i++) {
                r.accept(createRecipe(entry, i));
            }
        }).filter(Objects::nonNull).toList();
    }
    @Nullable
    private static RecipeHolder<CraftingRecipe> createRecipe(ResourceLocation entry, int count) {
        Item getFrom = ClayMobTeamManger.getFromKeyOrError(entry, Minecraft.getInstance().level.registryAccess()).getGetFrom();
        if (getFrom == null) {
            return null;
        }
        NonNullList<Ingredient> inputs = NonNullList.createWithCapacity(count + 1);
        inputs.add(Ingredient.of(getFrom));
        for (int i = 0; i < count; i++) {
            inputs.add(Ingredient.of(ClayMobTeamManger.getAllKeys(getRegistries()).map(team -> ClayMobTeamManger.createStackForTeam(team, getRegistries()))));
        }
        ItemStack output = ClayMobTeamManger.createStackForTeam(entry, getRegistries());
        output.setCount(count);
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_CRAFTING + "." + output.getDescriptionId() + "_" + count);
        CraftingRecipe recipe = new ShapelessRecipe(CLAY_SOLDIER_CRAFTING, CraftingBookCategory.MISC, output, inputs);

        return new RecipeHolder<>(recipeId, recipe);
    }

    public static List<RecipeHolder<CraftingRecipe>> createClaySoldierRevive() {
        return ClayMobTeamManger.getAllKeys(getRegistries()).map(ClaySoldierCrafting::createClaySoldierRevive).toList();
    }
    public static RecipeHolder<CraftingRecipe> createClaySoldierRevive(ResourceLocation team) {
        ItemStack output = ClayMobTeamManger.createStackForTeam(team, getRegistries());
        ItemStack input = ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance();
        input.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team);

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_REVIVING + "." + output.getDescriptionId());
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(input), Ingredient.of(Items.GHAST_TEAR));

        CraftingRecipe recipe = new ShapelessRecipe(CLAY_SOLDIER_REVIVING, CraftingBookCategory.MISC, output, inputs);
        return new RecipeHolder<>(recipeId, recipe);
    }

    private static RegistryAccess getRegistries() {
        return Minecraft.getInstance().level.registryAccess();
    }
}
