package net.bumblebee.claysoldiers.integration.jei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class JEIShapelessRecipe implements CraftingRecipe {
    public static final MapCodec<JEIShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter((shapedRecipe) -> shapedRecipe.group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedRecipe) -> shapedRecipe.category),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(s -> s.result),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(s -> s.ingredients),
                    Codec.list(SlotDisplay.CODEC).fieldOf("display").forGetter((shapedRecipe) -> shapedRecipe.displays),
                    SlotDisplay.CODEC.fieldOf("result_display").forGetter((shapedRecipe) -> shapedRecipe.result_display))
            .apply(instance, JEIShapelessRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JEIShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, s -> s.group,
            CraftingBookCategory.STREAM_CODEC, s -> s.category,
            ItemStackTemplate.STREAM_CODEC, s -> s.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), s -> s.ingredients,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), s -> s.displays,
            SlotDisplay.STREAM_CODEC, s -> s.result_display,
            JEIShapelessRecipe::new
    );
    public static final RecipeSerializer<JEIShapelessRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);


    private final String group;
    private final CraftingBookCategory category;
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final List<SlotDisplay> displays;
    private final SlotDisplay result_display;

    public JEIShapelessRecipe(String group, CraftingBookCategory category, ItemStackTemplate result, List<Ingredient> ingredients, List<SlotDisplay> displays, SlotDisplay results) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.displays = displays;
        this.result_display = results;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1 ? ((Ingredient) this.ingredients.getFirst()).test(input.getItem(0)) : input.stackedContents().canCraft(this, (StackedContents.Output) null);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(
                ingredients
        );
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public List<RecipeDisplay> display() {

        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        displays,
                        result_display,
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
                )
        );
    }
}
