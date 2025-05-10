package net.bumblebee.claysoldiers.item.disruptor;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.ClayMobContainer;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class ClayMobKillItem extends Item {
    public static final String RANGE_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range";
    public static final String RANGE_UNLIMITED_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range.unlimited";
    public static final String RANGE_ERROR_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".disruptor.range.error";


    public ClayMobKillItem(Properties pProperties, DisruptorKillRange range) {
        super(pProperties.component(ModDataComponents.DISRUPTOR_KILL_RANGE.get(), range));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide() && pUsedHand == InteractionHand.MAIN_HAND) {
            killSoldiers(pPlayer.getItemInHand(pUsedHand), (ServerLevel) pLevel, pPlayer.getOnPos(), pPlayer);
            return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var range = stack.get(ModDataComponents.DISRUPTOR_KILL_RANGE.get());
        if (range == null) {
            tooltipComponents.add(Component.translatable(RANGE_ERROR_LANG).withStyle(ChatFormatting.RED));
        } else if (tooltipFlag.isAdvanced()) {
            tooltipComponents.add(CommonComponents.space().append(range.appendRangeToComponent(RANGE_LANG, RANGE_UNLIMITED_LANG)).withStyle(ChatFormatting.GRAY));
        }
    }

    private int killSoldiers(ItemStack stack, ServerLevel level, BlockPos center, Player player) {
        var killRange = stack.get(ModDataComponents.DISRUPTOR_KILL_RANGE.get());
        if (killRange == null) {
            return -1;
        }
        List<? extends ClayMobEntity> clayMobEntities = killRange.getEntitiesInRange(level, center);
        for(ClayMobEntity entity : clayMobEntities) {
            kill(level, player, entity);
        }

        List<ClayMobContainer> soldierContainer = killRange.getClaySoldierContainers(level, center);
        soldierContainer.forEach(blockEntity -> blockEntity.killSoldier(level, player));

        return clayMobEntities.size() + soldierContainer.size();
    }

    private static void kill(ServerLevel level, Player player, ClayMobEntity clayMob) {
        var owner = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(clayMob.getClayTeamType());
        if (owner == null || owner.is(player)) {
            clayMob.kill();
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity p_344979_) {
        return 10;
    }
}
