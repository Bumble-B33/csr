package net.bumblebee.claysoldiers.item.claypouch;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;
import java.util.function.Consumer;

public class ClayPouchItem extends Item {
    public static final String FULLNESS_LANG = "item.csr.clay_pouch.fullness";
    private static final int BAR_COLOR = ColorHelper.DEFAULT_CLAY_COLOR;

    public ClayPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pouch, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack toAbsorb = slot.getItem();
            int maxCapacity = getMaxCapacity(pouch, player.registryAccess());

            if (toAbsorb.isEmpty()) {
                if (ClayPouchContent.onPouch(pouch, c -> c.takeStack(slot::safeInsert, maxCapacity, player.registryAccess()))) {
                    playRemoveOneSound(player);
                    return true;
                }
                return false;
            } else {
                if (!(toAbsorb.getItem() instanceof MultiSpawnItem<?> multi)) {
                    return false;
                }
                ClayPouchContent content = pouch.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
                if (content != null && !content.isFor(toAbsorb)) {
                    return false;
                }
                ClayPouchContent newContent;
                int used;
                if (content == null) {
                    used = Math.min(toAbsorb.getCount(), maxCapacity);
                    newContent = new ClayPouchContent(multi, used, maxCapacity, multi.requiredForPouch(toAbsorb));
                } else {
                    used = Math.min(toAbsorb.getCount(), content.maxRemaining(maxCapacity));
                    newContent = content.increment(used, maxCapacity);
                }
                this.playInsertSound(player);


                slot.remove(used);
                pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), newContent);

                return true;
            }
        }
    }


    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouch, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            ClayPouchContent content = pouch.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
            int maxCapacity = getMaxCapacity(pouch, player.registryAccess());

            if (content == null) {
                if (other.getItem() instanceof MultiSpawnItem<?> multi) {
                    int used = Math.min(other.getCount(), maxCapacity);
                    pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), new ClayPouchContent(multi, used, maxCapacity, multi.requiredForPouch(other)));
                    other.shrink(used);
                    return true;
                }
                return false;
            } else {
                if (other.isEmpty()) {
                    pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), content.takeStack(access::set, maxCapacity, player.registryAccess()));
                    this.playRemoveOneSound(player);
                } else {
                    content.insert(other, maxCapacity, (c, i) -> {
                        other.shrink(i);
                        pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), c);
                        this.playInsertSound(player);
                    });
                }

                return true;
            }
        } else {
            return false;
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content != null && flag.isAdvanced()) {
            tooltipAdder.accept(Component.translatable(FULLNESS_LANG, content.getCount(), content.getMaxCapacity()).withStyle(ChatFormatting.GRAY));
            tooltipAdder.accept(Component.translatable(FULLNESS_LANG, content.getCount(), getMaxCapacity(stack, context.registries())).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        return tooltipDisplay.shows(ModDataComponents.CLAY_POUCH_CONTENT.get())
                ? Optional.ofNullable(stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get()))
                : Optional.empty();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModDataComponents.CLAY_POUCH_CONTENT.get());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content == null || ClaySoldiersCommon.clientPlayer.get() == null) {
            return BAR_COLOR;
        }
        return content.getColor(ClaySoldiersCommon.clientPlayer.get());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ClayPouchContent content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        return content == null ? 0 : content.getFillPercent(getMaxCapacity(stack));
    }



    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ClayPouchContent content = itemEntity.getItem().get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content != null) {
            itemEntity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            ItemUtils.onContainerDestroyed(itemEntity, content.copyItems());
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        ClayPouchContent content = itemInHand.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content == null) {
            return super.useOn(context);
        }
        MultiSpawnItem<?> multi = content.getItem();
        ItemStack doll = new ItemStack(multi);
        content.dataComponents().ifPresent(doll::applyComponents);
        int toSpawn;
        Player player = context.getPlayer();
        if (player != null && player.isCrouching()) {
            toSpawn = 1;
        } else {
            toSpawn = content.getCount();
        }
        var res = multi.spawnWithCount(doll, context, toSpawn);
        if (res < 0) {
            return InteractionResult.FAIL;
        }

        itemInHand.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), content.shrink(res, getMaxCapacity(itemInHand, context.getLevel().registryAccess())));

        return InteractionResult.SUCCESS;
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static int getMaxCapacity(ItemStack stack, HolderLookup.Provider registryAccess) {
        return getMaxCapacity(stack);
    }

    public static int getMaxCapacity(ItemStack stack) {
        return ClayPouchContent.verifyMaxCapacity(256);
    }
}
