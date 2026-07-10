package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.BrickedItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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

public class BrickedItemReviveRecipe implements CraftingRecipe {
    public static final BrickedItemReviveRecipe INSTANCE = new BrickedItemReviveRecipe();
    public static final MapCodec<BrickedItemReviveRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrickedItemReviveRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Nullable
    private PlacementInfo placementInfo;

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
    public ItemStack assemble(CraftingInput input) {
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
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        List.of(
                                new SlotDisplay.ItemSlotDisplay(ModItems.BRICKED_CLAY_SOLDIER.get()),
                                new SlotDisplay.ItemSlotDisplay(Items.GHAST_TEAR)
                        ),
                        new SlotDisplay.ItemSlotDisplay(ModItems.CLAY_SOLDIER.get()),
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)

                )
        );
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(
                    List.of(
                            Ingredient.of(ModItems.BRICKED_CLAY_SOLDIER.get()),
                            Ingredient.of(Items.GHAST_TEAR)
                    )
            );
        }
        return placementInfo;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return ClaySoldiersCommon.MOD_ID + ":revive";
    }

    @Override
    public @NotNull RecipeSerializer<BrickedItemReviveRecipe> getSerializer() {
        return ModRecipes.CLAY_SOLDIER_REVIVING.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
