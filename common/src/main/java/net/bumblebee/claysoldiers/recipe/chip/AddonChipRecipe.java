package net.bumblebee.claysoldiers.recipe.chip;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AddonChipRecipe extends ChipAssemblyRecipe {
    public static final AddonChipRecipe INSTANCE = new AddonChipRecipe();
    public static final MapCodec<AddonChipRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddonChipRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private AddonChipRecipe() {
        super(new CommonInfo(false), new ChipAssemblyInfo(ChipAssemblyCategory.ADDON, "csr:addons"), 4, 6);
    }

    @Override
    public boolean matches(ChipInput input, Level level) {
        ClaySoldierChip chip = ClaySoldierChipItem.getChipFromItem(input.getChip());
        if (chip == null) {
            return false;
        }
        List<ClaySoldierChipAddon> addons = new ArrayList<>();
        for (ItemStack stack : input.getNonCenterInputs()) {
            ClaySoldierChipAddon addon = stack.get(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON.get());
            if (addon == null) {
                return false;
            }
            addons.add(addon);
        }
        if (addons.isEmpty()) {
            return false;
        }

        return chip.canApplyAddons(addons);
    }

    @Override
    public ItemStack assemble(ChipInput input) {
        ItemStack chipItem = input.getChip().copyWithCount(1);
        ClaySoldierChip chip = ClaySoldierChipItem.getChipFromItem(chipItem);
        if (chip == null) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Matching Recipe with Illegal State");
            return chipItem;
        }
        List<ClaySoldierChipAddon> addons = new ArrayList<>();
        for (ItemStack stack : input.getNonCenterInputs()) {
            ClaySoldierChipAddon addon = stack.get(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON.get());
            if (addon == null) {
                ClaySoldiersCommon.ERROR_HANDLER.warn("Matching Recipe with null addon");
            } else {
                addons.add(addon);
            }
        }
        ClaySoldierChip chipWithAddons = chip.addAddons(addons);
        if (chipWithAddons == null) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Matching Recipe but cannot apply addons");
            return chipItem;
        }
        return ClaySoldierChipItem.addChip(chipItem.copyWithCount(1), chipWithAddons);
    }

    public ChipAssemblyInfo getRecipeInfo() {
        return recipeInfo;
    }

    public CommonInfo getCommonInfo() {
        return info;
    }



    @Override
    public RecipeSerializer<? extends AddonChipRecipe> getSerializer() {
        return ModRecipes.ADDON_CHIP_SERIALIZER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
}
