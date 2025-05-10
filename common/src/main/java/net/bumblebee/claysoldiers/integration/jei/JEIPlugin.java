package net.bumblebee.claysoldiers.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, ClaySoldierCrafting.createRecipes());
        registration.addRecipes(RecipeTypes.CRAFTING, ClaySoldierCrafting.createClaySoldierRevive());
        registration.addRecipes(RecipeTypes.SMELTING, ClaySoldierCookingRecipe.createCookingRecipe(SmeltingRecipe::new, 100));
        registration.addRecipes(RecipeTypes.BLASTING, ClaySoldierCookingRecipe.createCookingRecipe(BlastingRecipe::new, 50));
        registration.addRecipes(RecipeTypes.CAMPFIRE_COOKING, ClaySoldierCookingRecipe.createCookingRecipe(CampfireCookingRecipe::new, 200));
        registration.addRecipes(RecipeTypes.SMOKING, ClaySoldierCookingRecipe.createCookingRecipe(SmokingRecipe::new, 300));

        BuiltInRegistries.ITEM.getTag(ModTags.Items.SOLDIER_HOLDABLE).ifPresentOrElse(set -> {
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getEffect, ComponentFormating::addHoldableTooltip);
        }, () -> ClaySoldiersCommon.LOGGER.error("Could not load JEI Info for Clay Soldier Holdable Items"));

        BuiltInRegistries.ITEM.getTag(ModTags.Items.SOLDIER_POI).ifPresentOrElse(set -> {
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getItemPoi, (poi, list) -> {
                list.add(Component.translatable(ComponentFormating.SOLDIER_POI_ITEM).withStyle(ChatFormatting.DARK_GRAY));
                ComponentFormating.addPoiTooltip(poi, list);
            });
            addItemToInfo(registration, set.stream().map(Holder::value), ClaySoldiersCommon.DATA_MAP::getBlockPoi, (poi, list) -> {
                list.add(Component.translatable(ComponentFormating.SOLDIER_POI_BLOCK).withStyle(ChatFormatting.DARK_GRAY));
                ComponentFormating.addPoiTooltip(poi, list);
            });
        }, () -> ClaySoldiersCommon.LOGGER.error("Could not load JEI Info for Clay Soldier POIs "));

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
        registration.registerSubtypeInterpreter(ModItems.CLAY_SOLDIER.get(), ClaySoldierPuppetInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.BLUEPRINT.get(), BlueprintInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.BRICKED_CLAY_SOLDIER.get(), ClaySoldierPuppetInterpreter.INSTANCE);
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "jei_plugin");
    }

    private enum ClaySoldierPuppetInterpreter implements ISubtypeInterpreter<ItemStack> {
        INSTANCE;

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return ingredient.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return ingredient.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()).toString();
        }
    }

    private enum BlueprintInterpreter implements ISubtypeInterpreter<ItemStack> {
        INSTANCE;

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return ingredient.get(ModDataComponents.BLUEPRINT_DATA.get());
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            var data = ingredient.get(ModDataComponents.BLUEPRINT_DATA.get());
            return data == null ? "" : data.toString();
        }
    }
}
