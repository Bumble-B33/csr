package net.bumblebee.claysoldiers.recipe;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ClaySoldierCookingRecipe {
    public static final Recipe.CommonInfo COMMON_INFO = new Recipe.CommonInfo(false);
    public static final AbstractCookingRecipe.CookingBookInfo INFO = new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, "clay_mob_cooking");


    public static ClaySoldierSmeltingRecipe smelting() {
        return new ClaySoldierSmeltingRecipe(100);
    }

    public static ClaySoldierBlastingRecipe blasting() {
        return new ClaySoldierBlastingRecipe(50);
    }

    public static ClaySoldierCampfireRecipe campfire() {
        return new ClaySoldierCampfireRecipe(200);
    }


    public static ClaySoldierSmokingRecipe smoking() {
        return new ClaySoldierSmokingRecipe(300);
    }

    public static <T extends Recipe<?>> RecipeSerializer<T> serializer(T value) {
        return new RecipeSerializer<>(codec(value), streamCodec(value));
    }

    private static <T> MapCodec<T> codec(T value) {
        return MapCodec.unit(value);
    }

    private static <T>StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(T value) {
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf byteBuf) {
                return value;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf o, T t) {

            }
        };
    }


    private static boolean recipeMatches(SingleRecipeInput recipeInput) {
        return recipeInput.item().getItem() instanceof BrickedItemHolder;
    }

    private static ItemStack assembleRecipe(SingleRecipeInput singleRecipeInput) {
        var stack = singleRecipeInput.item();
        var bricked = (BrickedItemHolder) stack.getItem();
        return bricked.getBrickedItem(stack);
    }

    public static class ClaySoldierSmeltingRecipe extends SmeltingRecipe {

        public ClaySoldierSmeltingRecipe(int cookingTime) {
            super(COMMON_INFO, INFO, Ingredient.of(ModItems.CLAY_SOLDIER.get()), new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get()), 1, cookingTime);
        }

        @Override
        public RecipeSerializer<SmeltingRecipe> getSerializer() {
            return ModRecipes.CLAY_SOLDIER_SMELTING.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return assembleRecipe(input);
        }
    }

    public static class ClaySoldierBlastingRecipe extends BlastingRecipe {

        public ClaySoldierBlastingRecipe(int cookingTime) {
            super(COMMON_INFO, INFO, Ingredient.of(ModItems.CLAY_SOLDIER.get()), new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get()), 1, cookingTime);
        }

        @Override
        public RecipeSerializer<BlastingRecipe> getSerializer() {
            return ModRecipes.CLAY_SOLDIER_BLASTING.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return assembleRecipe(input);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class ClaySoldierCampfireRecipe extends CampfireCookingRecipe {

        public ClaySoldierCampfireRecipe(int cookingTime) {
            super(COMMON_INFO, INFO, Ingredient.of(ModItems.CLAY_SOLDIER.get()), new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get()), 1, cookingTime);
        }

        @Override
        public RecipeSerializer<CampfireCookingRecipe> getSerializer() {
            return ModRecipes.CLAY_SOLDIER_CAMPFIRE.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return assembleRecipe(input);
        }
    }

    public static class ClaySoldierSmokingRecipe extends SmokingRecipe {
        public ClaySoldierSmokingRecipe(int cookingTime) {
            super(COMMON_INFO, INFO, Ingredient.of(ModItems.CLAY_SOLDIER.get()), new ItemStackTemplate(ModItems.BRICKED_CLAY_SOLDIER.get()), 1, cookingTime);
        }

        @Override
        public RecipeSerializer<SmokingRecipe> getSerializer() {
            return ModRecipes.CLAY_SOLDIER_SMOKING.get();
        }

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            return recipeMatches(input);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return assembleRecipe(input);
        }
    }
}
