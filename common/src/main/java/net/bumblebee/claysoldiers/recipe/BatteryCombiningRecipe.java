package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class BatteryCombiningRecipe extends CustomRecipe {
    public static final BatteryCombiningRecipe INSTANCE = new BatteryCombiningRecipe();
    public static final MapCodec<BatteryCombiningRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BatteryCombiningRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private final ItemStackTemplate result;
    private final Item small;

    private BatteryCombiningRecipe() {
        this.result = new ItemStackTemplate(ModItems.LARGE_BATTERY.get());
        this.small = ModItems.SMALL_BATTERY.get();
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != 2) {
            return false;
        }

        return craftingInput.items().stream().allMatch(s -> s.is(small) || s.isEmpty());

    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput) {
        ItemStack stack = result.create();

        craftingInput.items().forEach(s -> ClaySoldiersCommon.ENERGY_HELPER.insert(stack, ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(s)));

        return stack;
    }

    @Override
    public RecipeSerializer<? extends BatteryCombiningRecipe> getSerializer() {
        return ModRecipes.BATTERY_COMBING.get();
    }

    public ShapelessRecipe asShapeless() {
        return new ShapelessRecipe(
                new Recipe.CommonInfo(false),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "battery_combining"),
                result,
                List.of(Ingredient.of(small), Ingredient.of(small))
        );
    }
}
