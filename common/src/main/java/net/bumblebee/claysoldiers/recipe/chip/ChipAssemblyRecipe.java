package net.bumblebee.claysoldiers.recipe.chip;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.block.chipassembler.ChipEnergyStorage;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;

public abstract class ChipAssemblyRecipe implements Recipe<ChipInput> {
    protected final CommonInfo info;
    protected final ChipAssemblyInfo recipeInfo;
    protected final int builtSteps;
    protected final int energyPerTick;
    public static final int BUILT_TIME_MULTIPLIER = 15;

    public ChipAssemblyRecipe(CommonInfo info, ChipAssemblyInfo recipeInfo, int builtSteps, int energyPerTick) {
        this.info = info;
        this.recipeInfo = recipeInfo;
        this.builtSteps = builtSteps;
        this.energyPerTick = energyPerTick;
    }

    /**
     * @return the adjusted built time for smooth recipe steps
     */
    public int adjustedBuiltTime() {
        return builtSteps * BUILT_TIME_MULTIPLIER;
    }

    public int builtSteps() {
        return builtSteps;
    }

    public int energyCost() {
        return energyPerTick;
    }

    public int totalEnergyCost() {
        return energyPerTick * adjustedBuiltTime();
    }



    @Override
    public boolean showNotification() {
        return info.showNotification();
    }

    @Override
    public RecipeType<? extends ChipAssemblyRecipe> getType() {
        return ModRecipes.CHIP_ASSEMBLY_TYPE;
    }

    @Override
    public String group() {
        return recipeInfo.group();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (recipeInfo.category()) {
            case CHIP -> ModRecipes.CHIP_ASSEMBLY;
            case ADDON -> ModRecipes.ADDON_ASSEMBLY;
            case MISC -> ModRecipes.MISC_ASSEMBLY;
        };
    }

    public record ChipAssemblyInfo(ChipAssemblyCategory category, String group) implements Recipe.BookInfo<ChipAssemblyCategory> {
        public static final MapCodec<ChipAssemblyInfo> MAP_CODEC = BookInfo.mapCodec(ChipAssemblyCategory.CODEC, ChipAssemblyCategory.MISC, ChipAssemblyInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ChipAssemblyInfo> STREAM_CODEC = BookInfo.streamCodec(ChipAssemblyCategory.STREAM_CODEC, ChipAssemblyInfo::new);
    }
}
