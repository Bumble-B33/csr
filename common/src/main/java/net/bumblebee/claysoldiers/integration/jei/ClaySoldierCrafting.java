package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.recipe.BrickedItemReviveRecipe;
import net.bumblebee.claysoldiers.recipe.ClaySoldierCraftingRecipe;
import net.bumblebee.claysoldiers.recipe.TeamBasesItemStackDisplay;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaySoldierCrafting {
    private static final String CLAY_SOLDIER_REVIVING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.revive";
    private static final String CLAY_SOLDIER_CRAFTING = "jei." + ClaySoldiersCommon.MOD_ID + ".soldier.crafting";

    public static List<RecipeHolder<CraftingRecipe>> createRecipes(RegistryAccess registryAccess) {
        var allKeys = ClayMobTeamManger.getAll(registryAccess);
        return allKeys.<RecipeHolder<CraftingRecipe>>mapMulti((entry, r) -> {
            for (int i = 1; i <= 8; i++) {
                r.accept(createRecipe(entry, i, registryAccess));
            }
        }).filter(Objects::nonNull).toList();
    }
    @Nullable
    private static RecipeHolder<CraftingRecipe> createRecipe(Holder.Reference<ClayMobTeam> entry, int count, RegistryAccess registryAccess) {
        Item getFrom = entry.value().getGetFrom();
        if (getFrom == null) {
            return null;
        }
        NonNullList<Ingredient> inputs = NonNullList.createWithCapacity(count + 1);
        List<SlotDisplay> displays = new ArrayList<>(count);
        inputs.add(Ingredient.of(getFrom));
        displays.add(new SlotDisplay.ItemSlotDisplay(getFrom));
        for (int i = 0; i < count; i++) {
            inputs.add(Ingredient.of(ModItems.CLAY_SOLDIER.get()));
            displays.add(new TeamBasesItemStackDisplay(new ItemStackTemplate(ModItems.CLAY_SOLDIER.get())));
        }
        ItemStackTemplate output = ClaySoldierSpawnItem.createTemplateClayMobTeam(entry).withCount(count);

        ResourceKey<Recipe<?>> recipeId = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_CRAFTING + "." + entry.key().identifier().getPath().toString() + "_" + count));
        CraftingRecipe recipe = new JEIShapelessRecipe(ClaySoldierCraftingRecipe.INSTANCE.group(), ClaySoldierCraftingRecipe.INSTANCE.category(),
                output, inputs,
                displays,
                new SlotDisplay.ItemStackSlotDisplay(output)

        );

        return new RecipeHolder<>(recipeId, recipe);
    }

    public static List<RecipeHolder<CraftingRecipe>> createClaySoldierRevive(RegistryAccess registryAccess) {
        return ClayMobTeamManger.getAll(registryAccess).map(ClaySoldierCrafting::createClaySoldierRevive).toList();
    }
    public static RecipeHolder<CraftingRecipe> createClaySoldierRevive(Holder.Reference<ClayMobTeam> team) {
        ItemStackTemplate output = ClaySoldierSpawnItem.createTemplateClayMobTeam(team);


        ResourceKey<Recipe<?>> recipeId = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, CLAY_SOLDIER_REVIVING + "." + team.key().identifier().getPath()));

        List<Ingredient> inputs = List.of(
                Ingredient.of(ModItems.BRICKED_CLAY_SOLDIER.get()),
                Ingredient.of(Items.GHAST_TEAR)
        );
        List<SlotDisplay> displays = List.of(
                new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get(), DataComponentPatch.builder().set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team.key()).build())),
                new SlotDisplay.ItemSlotDisplay(Items.GHAST_TEAR)
        );

        CraftingRecipe recipe = new JEIShapelessRecipe(BrickedItemReviveRecipe.INSTANCE.group(), BrickedItemReviveRecipe.INSTANCE.category(),
                output, inputs,
                displays,
                new SlotDisplay.ItemStackSlotDisplay(output)
        );
        return new RecipeHolder<>(recipeId, recipe);
    }
}
