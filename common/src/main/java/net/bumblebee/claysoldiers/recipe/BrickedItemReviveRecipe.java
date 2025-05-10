package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.BrickedItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BrickedItemReviveRecipe extends CustomRecipe {
    public BrickedItemReviveRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack ghastTear = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (!itemAtI.isEmpty()) {
                if (itemAtI.getItem() instanceof BrickedItem) {
                    if (!itemstack.isEmpty()) {
                        return false;
                    }
                    itemstack = itemAtI;
                } else if (itemAtI.is(Items.GHAST_TEAR)) {
                    if (!ghastTear.isEmpty()) {
                        return false;
                    }
                    ghastTear = itemAtI;
                }
            }
        }

        return !itemstack.isEmpty() && !ghastTear.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (!itemAtI.isEmpty()) {
                if (itemAtI.getItem() instanceof BrickedItem brickedItem) {
                    return brickedItem.getOriginal(itemAtI);
                }
            }
        }
        throw new IllegalStateException("Crafting: Tried Reviving a Clay Soldier with no BrickedItem");
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<BrickedItemReviveRecipe> getSerializer() {
        return ModRecipes.CLAY_SOLDIER_REVIVING.get();
    }
}
