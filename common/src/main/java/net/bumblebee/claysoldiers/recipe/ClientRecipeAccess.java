package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.recipe.chip.BasicChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipInput;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClientRecipeAccess {
    public static final ClientRecipeAccess INSTANCE = new ClientRecipeAccess();

    private final List<Consumer<List<BasicChipAssemblyRecipe>>> recipesCreatedRunCallback = new ArrayList<>();

    @Nullable
    private RecipeMap recipeMap = null;
    @Nullable
    private List<BasicChipAssemblyRecipe> basicRecipes = null;

    private ClientRecipeAccess() {
    }

    public void fill(Collection<? extends RecipeHolder<?>> recipes) {
        this.recipeMap = RecipeMap.create(new ArrayList<>(recipes));
        this.basicRecipes = recipes.stream().map(s -> {
            if (s.value() instanceof BasicChipAssemblyRecipe basicChipAssemblyRecipe) {
                return basicChipAssemblyRecipe;
            }
            return null;
        }).filter(Objects::nonNull).toList();

        recipesCreatedRunCallback.forEach(a -> a.accept(basicRecipes));
        recipesCreatedRunCallback.clear();
    }

    public void whenBasicRecipesAreLoaded(Consumer<List<BasicChipAssemblyRecipe>> action) {
        if (basicRecipes != null) {
            action.accept(basicRecipes);
        } else {
            recipesCreatedRunCallback.add(action);
        }
    }

    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, I container, Level level) {
        if (recipeMap == null) {
            return Stream.of();
        }
        return recipeMap.getRecipesFor(type, container, level);
    }

    public Stream<RecipeHolder<ChipAssemblyRecipe>> getChipAssemblyRecipe(ChipInput chipInput, Level level) {
        return getRecipesFor(ModRecipes.CHIP_ASSEMBLY_TYPE, chipInput, level);
    }

    @Override
    public String toString() {
        return "ClientRecipeAccess{"+ recipeMap + '}';
    }
}
