package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ClaySoldierCrafting {
    private static final String CLAY_SOLDIER_REVIVING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.revive";
    private static final String CLAY_SOLDIER_CRAFTING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.crafting";
    private static final Recipe.CommonInfo COMMON_INFO = new Recipe.CommonInfo(false);
    private static final CraftingRecipe.CraftingBookInfo CRAFTING_INFO = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, CLAY_SOLDIER_CRAFTING);
    private static final CraftingRecipe.CraftingBookInfo REVIVE_INFO = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, CLAY_SOLDIER_REVIVING);


    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
        var allKeys = ClayMobTeamManger.getAll(getRegistries());
        return allKeys.<RecipeHolder<CraftingRecipe>>mapMulti((entry, r) -> {
            for (int i = 1; i <= 8; i++) {
                r.accept(createRecipe(entry, i));
            }
        }).filter(Objects::nonNull).toList();
    }
    @Nullable
    private static RecipeHolder<CraftingRecipe> createRecipe(Holder.Reference<ClayMobTeam> entry, int count) {
        Item getFrom = entry.value().getGetFrom();
        if (getFrom == null) {
            return null;
        }
        NonNullList<Ingredient> inputs = NonNullList.createWithCapacity(count + 1);
        inputs.add(Ingredient.of(getFrom));
        for (int i = 0; i < count; i++) {
            // Todo stacks
            inputs.add(Ingredient.of(ModItems.CLAY_SOLDIER.get()));
        }
        ItemStackTemplate output = ClaySoldierSpawnItem.createTemplateClayMobTeam(entry).withCount(count);

        ResourceKey<Recipe<?>> recipeId = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_CRAFTING + "." + entry.key().identifier().getPath().toString() + "_" + count));
        CraftingRecipe recipe = new ShapelessRecipe(COMMON_INFO, CRAFTING_INFO, output, inputs);

        return new RecipeHolder<>(recipeId, recipe);
    }

    public static List<RecipeHolder<CraftingRecipe>> createClaySoldierRevive() {
        return ClayMobTeamManger.getAll(getRegistries()).map(ClaySoldierCrafting::createClaySoldierRevive).toList();
    }
    public static RecipeHolder<CraftingRecipe> createClaySoldierRevive(Holder.Reference<ClayMobTeam> team) {
        ItemStackTemplate output = ClaySoldierSpawnItem.createTemplateClayMobTeam(team);
        ItemStack input = ModItems.BRICKED_CLAY_SOLDIER.get().getDefaultInstance();
        // Todo use stack
        input.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team.key());

        ResourceKey<Recipe<?>> recipeId = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_REVIVING + "." + team.key().identifier().getPath()));
        List<Ingredient> inputs = List.of(
                Ingredient.of(ModItems.BRICKED_CLAY_SOLDIER.get()),
                Ingredient.of(Items.GHAST_TEAR)
        );

        CraftingRecipe recipe = new ShapelessRecipe(COMMON_INFO, REVIVE_INFO, output, inputs);
        return new RecipeHolder<>(recipeId, recipe);
    }

    private static RegistryAccess getRegistries() {
        return Minecraft.getInstance().level.registryAccess();
    }
}
