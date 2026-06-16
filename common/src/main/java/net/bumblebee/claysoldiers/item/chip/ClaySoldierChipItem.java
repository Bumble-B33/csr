package net.bumblebee.claysoldiers.item.chip;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ClaySoldierChipItem extends Item {
    public static final String NO_CHIP_ERROR_LANG = "item.%s.clay_soldier_chip.no_chip";
    public static final String INSTALLED_CHIP_LANG = "item.%s.clay_soldier_chip.chip";
    public static final String INSTALLED_ADDONS_LANG = "item.%s.clay_soldier_chip.addons";
    public static final String NO_ADDON_INSTALLED_LANG = "item.%s.clay_soldier_chip.no_addon";
    public static final String AT_POS_LANG = "item.%s.clay_soldier_chip.at";


    public ClaySoldierChipItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ClaySoldierChip<?> chip) {
        return createTemplate(chip).create();
    }

    public static ItemStackTemplate createTemplate(ClaySoldierChip<?> chip) {
        return new ItemStackTemplate(getItemForChip(chip.getType()),
                DataComponentPatch.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip).build());
    }

    public static ItemStackTemplate createTemplate(Item item, ClaySoldierChip<?> chip) {
        return new ItemStackTemplate(item,
                DataComponentPatch.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip).build());
    }

    public static ItemStack addChip(ItemStack stack, ClaySoldierChip<?> chip) {
        stack.set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip);
        return stack;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        var chip = getChipFromItem(stack);
        if (chip == null || !chip.is(ModTags.ClaySoldierChips.REQUIRES_POI_POS)) {
            return super.canDestroyBlock(stack, state, level, pos, entity);
        }

        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
            ClayBrushItem.setPoiPosAfterClick(serverPlayer, itemInHand, pos);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        var chip = getChipFromItem(stack);
        if (!tooltipDisplay.shows(ModDataComponents.CLAY_SOLDIER_CHIP.get())) {
            return;
        }
        Component chipLang;

        if (chip != null) {
            chipLang = chip.info(ClaySoldiersCommon.clientPlayer != null ? ClaySoldiersCommon.clientPlayer.get() : null);
        } else {
            chipLang = Component.translatable(NO_CHIP_ERROR_LANG).withStyle(ChatFormatting.RED);
        }

        tooltipAdder.accept(Component.translatable(INSTALLED_CHIP_LANG, chipLang).withStyle(ChatFormatting.GRAY));

        BlockPos pos = ClayBrushItem.getPoiPos(stack);
        if (pos != null) {
            tooltipAdder.accept(Component.translatable(AT_POS_LANG, pos.toShortString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (chip != null) {
            var comp = getAddonsFormatted(chip);
            if (comp != null) {
                tooltipAdder.accept(CommonComponents.space().append(Component.translatable(INSTALLED_ADDONS_LANG, comp).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private @Nullable Component getAddonsFormatted(@NotNull ClaySoldierChip<?> chip) {
        List<ClaySoldierChipAddon> addons = chip.getAddons();

        MutableComponent addonList = null;

        for (int i = 0; i < chip.getAllowedAddonsCount(); i++) {
            if (i < addons.size()) {
                if (addonList == null) {
                    addonList = addons.getFirst().getDisplayName().copy();
                } else {
                    addonList.append(Component.literal(", ").append(addons.get(i).getDisplayName()));
                }
            } else {
                if (addonList == null) {
                    addonList = Component.translatable(NO_ADDON_INSTALLED_LANG);
                } else {
                    addonList.append(Component.literal(", ").append(Component.translatable(NO_ADDON_INSTALLED_LANG)));
                }
            }
        }
        return addonList;
    }


    public static @Nullable ClaySoldierChip<?> getChipFromItem(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_SOLDIER_CHIP.get());
    }

    private static Item getItemForChip(ClaySoldierChip.Type<?> type) {
        if (type == ClaySoldierChips.EMPTY_TYPE.get()) {
            return ModItems.BLANK_CHIP.asItem();
        } else if (type == ClaySoldierChips.PLACE_SEEDS_TYPE.get()) {
            return ModItems.PLACE_SEEDS_CHIP.asItem();
        } else if (type == ClaySoldierChips.BREAK_CROPS_TYPE.get()) {
            return ModItems.BREAK_CROPS_CHIP.asItem();
        } else if (type == ClaySoldierChips.PICK_UP_ITEMS_TYPE.get()) {
            return ModItems.PICK_UP_ITEMS_CHIP.asItem();
        } else if (type == ClaySoldierChips.COMBAT_TYPE.get()) {
            return ModItems.COMBAT_CHIP.asItem();
        } else if (type == ClaySoldierChips.DIG_TYPE.get()) {
            return ModItems.DIG_CHIP.asItem();
        } else if (type == ClaySoldierChips.FISHING_TYPE.get()) {
            return ModItems.FISHING_CHIP.asItem();
        } else if (type == ClaySoldierChips.USE_POI.get()) {
            return ModItems.PLACE_SEEDS_CHIP.asItem();
        } else if (type == ClaySoldierChips.BUILD_BLUEPRINT_TYPE.get()) {
            return ModItems.BLUEPRINT_CHIP.asItem();
        }
        ClaySoldiersCommon.ERROR_HANDLER.warn("Chip Type with no Item: " + type.toString());
        return ModItems.BLANK_CHIP.asItem();
    }
}
