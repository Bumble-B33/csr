package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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
    public static final ShearBladeRecipe INSTANCE = new ShearBladeRecipe();
    public static final MapCodec<ShearBladeRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShearBladeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Nullable
    private PlacementInfo placementInfo;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (isDisabled()) {
            return false;
        }

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
    public ItemStack assemble(CraftingInput input) {
        return ModItems.SHEAR_BLADE.get().getDefaultInstance();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

        for (int i = 0; i < nonNullList.size(); i++) {
            ItemStack item = craftingInput.getItem(i);
            if (item.is(Items.SHEARS)) {
                nonNullList.set(i, ModItems.SHEAR_BLADE.get().getDefaultInstance());
            }
        }

        return nonNullList;
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
        var stack = new ItemStackTemplate(ModItems.SHEAR_BLADE.get());

        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(new SlotDisplay.ItemSlotDisplay(Items.SHEARS)),
                new SlotDisplay.ItemStackSlotDisplay(stack),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }

    public boolean isDisabled() {
        return ClaySoldiersCommon.CONFIG.getCommonConfig().shearBladeRecipeEnabled();
    }
}