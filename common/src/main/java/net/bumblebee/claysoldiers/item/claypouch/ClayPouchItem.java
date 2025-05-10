package net.bumblebee.claysoldiers.item.claypouch;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Optional;

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
            if (toAbsorb.isEmpty()) {
                if (ClayPouchContent.onPouch(pouch, c -> c.takeStack(slot::safeInsert, player.registryAccess()))) {
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
                    used = Math.min(toAbsorb.getCount(), ClayPouchContent.MAX_CAPACITY);
                    newContent = new ClayPouchContent(multi, used, multi.requiredForPouch(toAbsorb));
                } else {
                    used = Math.min(toAbsorb.getCount(), content.maxRemaining());
                    newContent = content.increment(used);
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
            if (content == null) {
                if (other.getItem() instanceof MultiSpawnItem<?> multi) {
                    int used = Math.min(other.getCount(), ClayPouchContent.MAX_CAPACITY);
                    pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), new ClayPouchContent(multi, used, multi.requiredForPouch(other)));
                    other.shrink(used);
                    return true;
                }
                return false;
            } else {
                if (other.isEmpty()) {
                    pouch.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), content.takeStack(access::set, player.registryAccess()));
                    this.playRemoveOneSound(player);
                } else {
                    content.insert(other, (c, i) -> {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content != null && tooltipFlag.isAdvanced()) {
            tooltipComponents.add(Component.translatable(FULLNESS_LANG, content.getCount(), ClayPouchContent.MAX_CAPACITY).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return !stack.has(DataComponents.HIDE_TOOLTIP) && !stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
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
        if (content == null || ClaySoldiersCommon.clientPlayer == null || ClaySoldiersCommon.clientPlayer.get() == null) {
            return BAR_COLOR;
        }
        return content.getColor(ClaySoldiersCommon.clientPlayer.get());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ClayPouchContent content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        return content == null ? 0 : content.getFillPercent();
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

        itemInHand.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), content.shrink(res));


        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
