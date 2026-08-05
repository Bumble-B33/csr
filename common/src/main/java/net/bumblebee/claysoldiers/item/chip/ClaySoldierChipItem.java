package net.bumblebee.claysoldiers.item.chip;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClaySoldierChipItem extends Item {
    public static final String NO_CHIP_ERROR_LANG = "item.%s.clay_soldier_chip.no_chip";
    public static final String INSTALLED_CHIP_LANG = "item.%s.clay_soldier_chip.chip";
    public static final String INSTALLED_ADDONS_LANG = "item.%s.clay_soldier_chip.addons";
    public static final String NO_ADDON_INSTALLED_LANG = "item.%s.clay_soldier_chip.no_addon";
    public static final String AT_POS_LANG = "item.%s.clay_soldier_chip.at";


    public ClaySoldierChipItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ClaySoldierChip chip) {
        return createTemplate(chip).create();
    }

    public static ItemStackTemplate createTemplate(ClaySoldierChip chip) {
        return new ItemStackTemplate(chip.getType().asItem(),
                DataComponentPatch.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip).build());
    }

    public static ItemStackTemplate createTemplate(Item item, ClaySoldierChip chip) {
        return new ItemStackTemplate(item,
                DataComponentPatch.builder().set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip).build());
    }

    public static ItemStack addChip(ItemStack stack, ClaySoldierChip chip) {
        stack.set(ModDataComponents.CLAY_SOLDIER_CHIP.get(), chip);
        return stack;
    }

    public static void setPoiInfo(ItemStack stack, @NonNull PoiPosInfo poiInfo) {
        ClayBrushItem.setPoiPos(stack, poiInfo);
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
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            ClayBrushItem.withSide(itemInHand, context.getClickedPos(), context.getClickedFace(), player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            ClayBrushItem.withSide(itemInHand, null, null, player);
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        appendHoverText(stack, tooltipDisplay::shows, tooltipAdder);
    }

    public static void appendHoverText(ItemStack stack, Predicate<DataComponentType<ClaySoldierChip>> show, Consumer<Component> tooltipAdder) {
        ClaySoldierChip chip = getChipFromItem(stack);
        if (!show.test(ModDataComponents.CLAY_SOLDIER_CHIP.get())) {
            return;
        }
        Component chipLang;

        if (chip != null) {
            chipLang = chip.info(ClaySoldiersCommon.clientPlayer.get());
        } else {
            chipLang = Component.translatable(NO_CHIP_ERROR_LANG).withStyle(ChatFormatting.RED);
        }

        tooltipAdder.accept(Component.translatable(INSTALLED_CHIP_LANG, chipLang).withStyle(ChatFormatting.GRAY));

        PoiPosInfo pos = ClayBrushItem.getPoiPos(stack);
        if (!pos.isEmpty()) {
            tooltipAdder.accept(Component.translatable(AT_POS_LANG, pos.shortDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (chip != null) {
            var comp = getAddonsFormatted(chip);
            if (comp != null) {
                tooltipAdder.accept(CommonComponents.space().append(Component.translatable(INSTALLED_ADDONS_LANG, comp).withStyle(ChatFormatting.GRAY)));
            }

            chip.appendItemHoverText(stack, tooltipAdder);
        }
    }

    private static @Nullable Component getAddonsFormatted(@NotNull ClaySoldierChip chip) {
        List<ClaySoldierChipAddon> addons = chip.getAddons();
        Map<ClaySoldierChipAddon, Integer> map = new HashMap<>();
        addons.forEach(a -> map.compute(a, (_, i) -> i == null ? 1 : i + 1));

        MutableComponent addonList = null;

        for (var entry : map.entrySet()) {
            if (addonList == null) {
                addonList = entry.getKey().getDisplayName().copy();
            } else {
                addonList.append(Component.literal(", ").append(entry.getKey().getDisplayName()));
            }
            if (entry.getValue() > 1) {
                addonList.append(" x" + entry.getValue());
            }
        }
        int emptyCount = chip.getAllowedAddonsCount() - addons.size();

        if (emptyCount > 0) {
            if (addonList == null) {
                addonList = Component.translatable(NO_ADDON_INSTALLED_LANG);
            } else {
                addonList.append(Component.literal(", ").append(Component.translatable(NO_ADDON_INSTALLED_LANG)));
            }
            if (emptyCount > 1) {
                addonList.append(" x" + emptyCount);
            }
        }


        return addonList;
    }


    public static @Nullable ClaySoldierChip getChipFromItem(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_SOLDIER_CHIP.get());
    }
}
