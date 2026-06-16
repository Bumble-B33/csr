package net.bumblebee.claysoldiers.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, ClaySoldierCrafting.createRecipes());
        registration.addRecipes(RecipeTypes.CRAFTING, ClaySoldierCrafting.createClaySoldierRevive());
        if (ClaySoldiersCommon.CONFIG.getCommonConfig().shearBladeRecipeEnabled()) {
            registration.addRecipes(RecipeTypes.CRAFTING, createShearBladeRecipe());
        }

        registration.addRecipes(RecipeTypes.SMELTING, ClaySoldierCookingRecipe.createCookingRecipe(SmeltingRecipe::new, 100));
        registration.addRecipes(RecipeTypes.BLASTING, ClaySoldierCookingRecipe.createCookingRecipe(BlastingRecipe::new, 50));
        registration.addRecipes(RecipeTypes.CAMPFIRE_COOKING, ClaySoldierCookingRecipe.createCookingRecipe(CampfireCookingRecipe::new, 200));
        registration.addRecipes(RecipeTypes.SMOKING, ClaySoldierCookingRecipe.createCookingRecipe(SmokingRecipe::new, 300));


        BuiltInRegistries.ITEM.get(ModTags.Items.SOLDIER_HOLDABLE).ifPresentOrElse(set -> {
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getEffect, ComponentFormating::addHoldableTooltip);
        }, () -> ClaySoldiersCommon.LOGGER.error("Could not load JEI Info for Clay Soldier Holdable Items"));

        BuiltInRegistries.ITEM.get(ModTags.Items.SOLDIER_POI).ifPresentOrElse(set -> {
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getItemPoi, (poi, list) -> {
                list.add(Component.translatable(ComponentFormating.SOLDIER_POI_ITEM).withStyle(ChatFormatting.DARK_GRAY));
                ComponentFormating.addPoiTooltip(poi, list);
            });
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getBlockPoi, (poi, list) -> {
                list.add(Component.translatable(ComponentFormating.SOLDIER_POI_BLOCK).withStyle(ChatFormatting.DARK_GRAY));
                ComponentFormating.addPoiTooltip(poi, list);
            });
        }, () -> ClaySoldiersCommon.LOGGER.error("Could not load JEI Info for Clay Soldier POIs "));

        ClaySoldiersCommon.CLIENT_RECIPE_ACCESS.whenBasicRecipesAreLoaded(r -> registration.addRecipes(ChipAssemblyRecipeCategory.TYPE, r));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ChipAssemblyRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ChipAssemblyRecipeCategory.TYPE, ModBlocks.CHIP_ASSEMBLER);
    }

    private static <T> void addItemToInfo(IRecipeRegistration registration, Stream<Item> items, Function<Item, T> effectGetter, BiConsumer<T, List<Component>> getDescription) {
        items.forEach(item -> {
            T effect = effectGetter.apply(item);
            if (effect != null) {
                List<Component> tooltip = new ArrayList<>();

                getDescription.accept(effect, tooltip);

                registration.addIngredientInfo(item, tooltip.toArray(Component[]::new));
            }
        });

    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.CLAY_SOLDIER.get(), DataComponentSubtypeInterpreter.CLAY_SOLDIER_PUPPET);
        registration.registerSubtypeInterpreter(ModItems.BRICKED_CLAY_SOLDIER.get(), DataComponentSubtypeInterpreter.CLAY_SOLDIER_PUPPET);

        registration.registerSubtypeInterpreter(ModItems.BLUEPRINT.get(), DataComponentSubtypeInterpreter.BLUEPRINT);
        registration.registerSubtypeInterpreter(ModItems.BLANK_CHIP.get(), DataComponentSubtypeInterpreter.CLAY_SOLDIER_CHIP);
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "jei_plugin");
    }

    public static List<RecipeHolder<CraftingRecipe>> createShearBladeRecipe() {
        return List.of(new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "jei.shear_blade")),
                new ShapelessRecipe(
                        new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, "%s.shear_blade".formatted(ClaySoldiersCommon.MOD_ID)),
                        new ItemStackTemplate(ModItems.SHEAR_BLADE.get()),
                        List.of(Ingredient.of(Items.SHEARS))
                )));
    }

    private enum DataComponentSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
        CLAY_SOLDIER_PUPPET(ModDataComponents.CLAY_MOB_TEAM_COMPONENT::get),
        BLUEPRINT(ModDataComponents.BLUEPRINT_DATA::get),
        CLAY_SOLDIER_CHIP(ModDataComponents.CLAY_SOLDIER_CHIP::get);

        private final Supplier<DataComponentType<?>> dataComponent;

        DataComponentSubtypeInterpreter(Supplier<DataComponentType<?>> dataComponent) {
            this.dataComponent = dataComponent;
        }

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return ingredient.get(dataComponent.get());
        }
    }
}
