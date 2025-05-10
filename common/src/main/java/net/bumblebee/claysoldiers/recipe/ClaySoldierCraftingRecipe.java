package net.bumblebee.claysoldiers.recipe;

import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClaySoldierCraftingRecipe extends CustomRecipe {
    public ClaySoldierCraftingRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> soldier = new ArrayList<>();
        ItemStack dye = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (!itemAtI.isEmpty()) {
                if (itemAtI.is(ModItems.CLAY_SOLDIER.get())) {
                    soldier.add(itemAtI);
                } else {
                    if (ClayMobTeamManger.getFromItem(itemAtI.getItem()) != null) {
                        if (!dye.isEmpty()) {
                            return false;
                        }
                        dye = itemAtI;
                    }
                }
            }
        }

        return !soldier.isEmpty() && !dye.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack soldierPuppet = ModItems.CLAY_SOLDIER.get().getDefaultInstance();
        int count = 0;
        ResourceLocation id = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (itemAtI.is(ModItems.CLAY_SOLDIER.get())) {
                count++;
            } else if (!itemAtI.isEmpty()) {
                id = ClayMobTeamManger.getFromItem(itemAtI.getItem());
            }

        }
        if (count > 0 && id != null) {
            ClaySoldierSpawnItem.setClayMobTeam(soldierPuppet, id, provider);
            soldierPuppet.setCount(count);
            return soldierPuppet;
        }
        throw new IllegalStateException("Crafting: Tried Crafting a Clay Soldier without a team item");
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<ClaySoldierCraftingRecipe> getSerializer() {
        return ModRecipes.CLAY_SOLDIER_CRAFTING.get();
    }
}
