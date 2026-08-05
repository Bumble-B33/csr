package net.bumblebee.claysoldiers.integration.jei;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.CombatChip;
import net.bumblebee.claysoldiers.claysoldierchips.PoiChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.claysoldierchips.work.*;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.bumblebee.claysoldiers.recipe.chip.AddonChipRecipe;
import net.bumblebee.claysoldiers.recipe.chip.BasicChipAssemblyRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ChipAssemblerAddonRecipes {
    private ChipAssemblerAddonRecipes() {
    }

    public static List<BasicChipAssemblyRecipe> recipes() {
        List<BasicChipAssemblyRecipe> list = new ArrayList<>();

        list.add(createAddon(PoiChip.create(), ClaySoldierChipAddons.ACCELERATION_ADDON));
        list.add(createAddon(PoiChip.create(), ClaySoldierChipAddons.UPGRADED_ACCELERATION_ADDON));


        list.add(createAddon(CombatChip.create(), ClaySoldierChipAddons.TARGET_ANIMALS_ADDON));
        list.add(createAddon(CombatChip.create(), ClaySoldierChipAddons.TARGET_MONSTER_ADDON));
        list.add(createAddon(CombatChip.create(), ClaySoldierChipAddons.TARGET_IGNORE_BABIES_ADDON, ClaySoldierChipAddons.TARGET_ANIMALS_ADDON));

        list.add(createAddon(PickUpItemsChip.create(), ClaySoldierChipAddons.NO_BREAK_ADDON));
        list.add(createAddon(PickUpItemsChip.create(), ClaySoldierChipAddons.RANGE_ADDON));

        list.add(createAddon(BreakCropsChip.create(), ClaySoldierChipAddons.NO_BREAK_ADDON));
        list.add(createAddon(BreakCropsChip.create(), ClaySoldierChipAddons.RANGE_ADDON));

        list.add(createAddon(PlaceSeedsChip.create(), ClaySoldierChipAddons.NO_BREAK_ADDON));
        list.add(createAddon(PlaceSeedsChip.create(), ClaySoldierChipAddons.RANGE_ADDON));


        list.add(createAddon(FishingChip.create(), ClaySoldierChipAddons.FISH_TREASURE_ADDON));
        list.add(createAddon(FishingChip.create(), ClaySoldierChipAddons.ACCELERATION_ADDON));
        list.add(createAddon(FishingChip.create(), ClaySoldierChipAddons.UPGRADED_ACCELERATION_ADDON));

        list.add(createAddon(FishingChip.create(), ClaySoldierChipAddons.NO_BREAK_ADDON));
        list.add(createAddon(FishingChip.create(), ClaySoldierChipAddons.RANGE_ADDON));

        list.add(createAddon(BeeKeepingChip.create(), ClaySoldierChipAddons.NO_BREAK_ADDON));
        list.add(createAddon(BeeKeepingChip.create(), ClaySoldierChipAddons.RANGE_ADDON));

        list.add(createAddon(ElectricianChip.create(), ClaySoldierChipAddons.ACCELERATION_ADDON));
        list.add(createAddon(ElectricianChip.create(), ClaySoldierChipAddons.UPGRADED_ACCELERATION_ADDON));

        return list.stream().filter(Objects::nonNull).toList();
    }

    private static @Nullable BasicChipAssemblyRecipe createAddon(ClaySoldierChip chip, ClaySoldierChipAddon... addons) {
        if (addons.length == 0) {
            ClaySoldiersCommon.ERROR_HANDLER.error("JEI: Illegal Chip Addon Recipe for " + chip + " No addons present");
            return null;
        }

        AddonChipRecipe addonChipRecipe = AddonChipRecipe.INSTANCE;

        ClaySoldierChip chipWithAddon = chip.addAddons(List.of(addons));
        if (chipWithAddon == null) {
            ClaySoldiersCommon.ERROR_HANDLER.error("JEI: Illegal Chip Addon Recipe for " + chip + " with " + List.of(addons));
            return null;
        }
        List<Ingredient> addonsItems = Arrays.stream(addons).map(s -> {
            Item item = s.asItem();
            return Ingredient.of(item);
        }).toList();

        return new BasicChipAssemblyRecipe(
                addonChipRecipe.getCommonInfo(),
                addonChipRecipe.getRecipeInfo(),
                ClaySoldierChipItem.createTemplate(chipWithAddon),
                Optional.of(Ingredient.of(chip.getType())),
                addonsItems,
                addonChipRecipe.builtSteps(),
                addonChipRecipe.energyCost()
        );
    }
}
