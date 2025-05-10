package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public final class ShearBladeRecipe {
    private static final String SHEAR_BLADE_CRAFTING = "jei." + ClaySoldiersCommon.MOD_ID + ".shear_blade";

    public static List<RecipeHolder<CraftingRecipe>> createShearBladeRecipe() {
        ItemStack output = ModItems.SHEAR_BLADE.get().getDefaultInstance();
        ItemStack input1 = Items.SHEARS.getDefaultInstance();
        List<ItemStack> inp = new ArrayList<>();
        inp.add(input1);

        int maxDamage = input1.getMaxDamage();
        for (int i = 1; i < maxDamage / 2; i += 20) {
            ItemStack inputDamaged = Items.SHEARS.getDefaultInstance();
            inputDamaged.setDamageValue(i);
            inp.add(inputDamaged);
        }

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(inp.stream()));
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, SHEAR_BLADE_CRAFTING);

        CraftingRecipe recipe = new ShapelessRecipe(SHEAR_BLADE_CRAFTING, CraftingBookCategory.MISC, output, inputs);
        return List.of(new RecipeHolder<>(recipeId, recipe));
    }

    private ShearBladeRecipe() {
    }
}
