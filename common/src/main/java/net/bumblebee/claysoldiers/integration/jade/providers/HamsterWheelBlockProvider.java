package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelSoldierData;
import net.bumblebee.claysoldiers.entity.client.ClientClaySoldierEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonBlockProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public enum HamsterWheelBlockProvider implements CommonBlockProvider {
    INSTANCE;

    public static final String HAMSTER_WHEEL_SPEED = JadeRegistry.getLangKey(INSTANCE, "soldier_speed");
    public static final String GENERATING = JadeRegistry.getLangKey(INSTANCE, "generation");


    @Override
    public void appendTooltip(BlockData data, CommonTooltipHelper tooltip, boolean detail) {
        HamsterWheelBlockEntity blockEntity = (HamsterWheelBlockEntity) data.entity();
        HamsterWheelSoldierData soldierBlockData = blockEntity.getSoldierData();

        if (soldierBlockData != null) {
            ClientClaySoldierEntity soldier = soldierBlockData.getClientSoldier();
            tooltip.addHorizontalLine();
            if (soldier.isWaxed()) {
                tooltip.addCompoundItemStack(soldier.getPickResult(), Items.HONEYCOMB.getDefaultInstance());
            } else {
                tooltip.addItemStack(soldier.getPickResult());
            }

            tooltip.appendMultilineText(
                    soldier.getDisplayName().copy().withStyle(ChatFormatting.WHITE),
                    ClayMobProvider.INSTANCE.createTeamName(soldier),
                    Component.translatable(HAMSTER_WHEEL_SPEED, soldierBlockData.getAdjustedSpeed()).withStyle(ChatFormatting.DARK_GRAY)
            );
            tooltip.addHorizontalLine();
        }
        if (detail && blockEntity.hasEnergyStorage()) {
            int generating = 0;
            if (soldierBlockData != null) {
                long speed = ClaySoldiersCommon.COMMON_HOOKS.getHamsterWheelSpeed();
                generating = (int) Math.max(1, speed * soldierBlockData.getAdjustedSpeed());
            }

            tooltip.add(Component.translatable(GENERATING, generating));
        }
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.HAMSTER_WHEEL_BLOCK;
    }

    @Override
    public Class<? extends Block> getTargetClass() {
        return HamsterWheelBlock.class;
    }
}
