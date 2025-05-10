package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ShearBladeRecipe extends CustomRecipe {
    public ShearBladeRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack shear = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (!itemAtI.isEmpty()) {
                if (itemAtI.is(Items.SHEARS)) {
                    if (!shear.isEmpty() || shear.getDamageValue() > 0) {
                        return false;
                    }
                    shear = itemAtI;
                }
            }
        }
        return !shear.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return ModItems.SHEAR_BLADE.get().getDefaultInstance();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); i++) {
            ItemStack item = craftingInput.getItem(i);
            if (item.is(Items.SHEARS)) {
                nonnulllist.set(i, ModItems.SHEAR_BLADE.get().getDefaultInstance());
            }
        }

        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 1;
    }

    @Override
    @NotNull
    public RecipeSerializer<ShearBladeRecipe> getSerializer() {
        return ModRecipes.SHEAR_BLADE_CRAFTING.get();
    }

    // Show In Recipe Book

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ModItems.SHEAR_BLADE.get().getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.SHEARS));
    }
}