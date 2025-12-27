package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShearBladeRecipe extends CustomRecipe {
    @Nullable
    private PlacementInfo placementInfo;

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
    @NotNull
    public RecipeSerializer<ShearBladeRecipe> getSerializer() {
        return ModRecipes.SHEAR_BLADE_CRAFTING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(Ingredient.of(Items.SHEARS));
        }

        return placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(new SlotDisplay.ItemSlotDisplay(Items.SHEARS)),
                new SlotDisplay.ItemSlotDisplay(ModItems.SHEAR_BLADE.get()),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }
}