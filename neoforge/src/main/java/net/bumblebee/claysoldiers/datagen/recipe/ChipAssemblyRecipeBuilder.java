package net.bumblebee.claysoldiers.datagen.recipe;

import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerInventory;
import net.bumblebee.claysoldiers.recipe.chip.BasicChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyCategory;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChipAssemblyRecipeBuilder implements RecipeBuilder {
    private final ChipAssemblyCategory category;
    private final ItemStackTemplate result;
    private final List<Ingredient> inputs;
    private final RecipeUnlockAdvancementBuilder advancementBuilder;
    private String group = null;
    private Ingredient chip = null;
    private int buildTime = 8;
    private int energyCost = 6;

    public ChipAssemblyRecipeBuilder(ChipAssemblyCategory category, ItemStackTemplate result) {
        this.category = category;
        this.result = result;
        this.inputs = new ArrayList<>();
        this.advancementBuilder = new RecipeUnlockAdvancementBuilder();
    }

    public static ChipAssemblyRecipeBuilder of(ChipAssemblyCategory category, ItemLike result) {
        return new ChipAssemblyRecipeBuilder(category, new ItemStackTemplate(result.asItem()));
    }

    public static ChipAssemblyRecipeBuilder of(ChipAssemblyCategory category, ItemLike result, int count) {
        return new ChipAssemblyRecipeBuilder(category, new ItemStackTemplate(result.asItem(), count));
    }

    @Override
    public ChipAssemblyRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    public ChipAssemblyRecipeBuilder chip(@NotNull ItemLike chip) {
        this.chip = Ingredient.of(chip);
        return this;
    }

    public ChipAssemblyRecipeBuilder setBuildTime(int buildTime) {
        this.buildTime = buildTime;
        return this;
    }

    public ChipAssemblyRecipeBuilder setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
        return this;
    }

    @Override
    public ChipAssemblyRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public ChipAssemblyRecipeBuilder addInput(Ingredient ingredient) {
        inputs.add(ingredient);
        if (inputs.size() > ChipAssemblerInventory.MAX_SIZE) {
            throw new IllegalStateException("Input size to big than");
        }
        return this;
    }

    public ChipAssemblyRecipeBuilder addInput(@NotNull ItemLike input) {
        return addInput(Ingredient.of(input));
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        BasicChipAssemblyRecipe recipe = new BasicChipAssemblyRecipe(
                RecipeBuilder.createCraftingCommonInfo(true),
                new ChipAssemblyRecipe.ChipAssemblyInfo(category, Objects.requireNonNullElse(group, "")),
                result,
                Optional.ofNullable(chip),
                inputs,
                buildTime,
                energyCost
        );
        output.accept(id, recipe, this.advancementBuilder.build(output, id, RecipeCategory.MISC));

    }
}
