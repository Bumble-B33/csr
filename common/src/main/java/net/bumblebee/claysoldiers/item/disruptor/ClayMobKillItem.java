package net.bumblebee.claysoldiers.item.disruptor;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.ClayMobContainer;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class ClayMobKillItem extends Item {
    public static final String RANGE_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range";
    public static final String RANGE_UNLIMITED_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range.unlimited";
    public static final String RANGE_ERROR_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range.error";


    public ClayMobKillItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(Level pLevel, Player player, InteractionHand usedHand) {
        if (pLevel instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && usedHand == InteractionHand.MAIN_HAND) {
            ItemStack itemInHand = player.getItemInHand(usedHand);
            int amountKilled = killSoldiers(itemInHand, serverLevel, player.getOnPos(), serverPlayer);
            ModCriterions.DISRUPTOR_KILL_TRIGGER.get().trigger(serverPlayer, amountKilled);
            itemInHand.hurtAndBreak(1, player, usedHand);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var range = stack.get(ModDataComponents.DISRUPTOR_KILL_RANGE.get());
        if (range == null) {
            tooltipAdder.accept(Component.translatable(RANGE_ERROR_LANG).withStyle(ChatFormatting.RED));
        } else if (flag.isAdvanced()) {
            tooltipAdder.accept(CommonComponents.space().append(range.appendRangeToComponent(RANGE_LANG, RANGE_UNLIMITED_LANG)).withStyle(ChatFormatting.GRAY));
        }
    }

    private int killSoldiers(ItemStack stack, ServerLevel level, BlockPos center, ServerPlayer player) {
        var killRange = stack.get(ModDataComponents.DISRUPTOR_KILL_RANGE.get());
        if (killRange == null) {
            return -1;
        }
        List<? extends ClayMobEntity> clayMobEntities = killRange.getEntitiesInRange(level, player, center);
        for(ClayMobEntity entity : clayMobEntities) {
            entity.kill(level);
        }

        List<ClayMobContainer> soldierContainer = killRange.getClaySoldierContainers(level, player, center);
        soldierContainer.forEach(blockEntity -> blockEntity.killSoldier(level, player));

        return clayMobEntities.size() + soldierContainer.size();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity p_344979_) {
        return 10;
    }
}
