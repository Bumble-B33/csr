package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClaySoldierCraftingRecipe extends CustomRecipe {
    public static final ClaySoldierCraftingRecipe INSTANCE = new ClaySoldierCraftingRecipe();
    public static final MapCodec<ClaySoldierCraftingRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierCraftingRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public ClaySoldierCraftingRecipe() {
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
    public ItemStack assemble(CraftingInput input) {
        int count = 0;
        ResourceKey<ClayMobTeam> id = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack itemAtI = input.getItem(i);
            if (itemAtI.is(ModItems.CLAY_SOLDIER.get())) {
                count++;
            } else if (!itemAtI.isEmpty()) {
                id = ClayMobTeamManger.getFromItem(itemAtI.getItem());
            }

        }
        if (count > 0 && id != null) {
            return ClaySoldierSpawnItem.createStackUnchecked(id, count);
        }
        ClaySoldiersCommon.ERROR_HANDLER.warn("Crafting: Tried Crafting a Clay Soldier without a team item");
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull RecipeSerializer<ClaySoldierCraftingRecipe> getSerializer() {
        return ModRecipes.CLAY_SOLDIER_CRAFTING.get();
    }
}
