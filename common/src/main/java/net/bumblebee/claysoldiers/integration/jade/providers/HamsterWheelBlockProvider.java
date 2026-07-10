package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlock;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.block.soldiercontainer.OccupantSoldierData;
import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.bumblebee.claysoldiers.integration.jade.CommonBlockProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public enum HamsterWheelBlockProvider implements CommonBlockProvider {
    INSTANCE;

    public static final String HAMSTER_WHEEL_SPEED = JadeRegistry.getLangKey(INSTANCE, "soldier_speed");
    public static final String GENERATING = JadeRegistry.getLangKey(INSTANCE, "generation");


    @Override
    public void appendTooltip(BlockData data, CommonTooltipHelper tooltip, boolean detail) {
        HamsterWheelBlockEntity blockEntity = (HamsterWheelBlockEntity) data.entity();
        OccupantSoldierData soldierBlockData = blockEntity.getSoldierData();

        if (soldierBlockData != null) {
            FakeClaySoldierAccess soldier = soldierBlockData.getClientSoldier();
            tooltip.addHorizontalLine();
            if (soldier.isWaxed()) {
                tooltip.addCompoundItemStack(soldier.getAsItem(), Items.HONEYCOMB.getDefaultInstance());
            } else {
                tooltip.addItemStack(soldier.getAsItem());
            }

            tooltip.appendMultilineText(
                    soldier.displayName().copy().withStyle(ChatFormatting.WHITE),
                    ClayMobProvider.INSTANCE.createTeamName(soldier.getClayTeam(), soldier.colorOffset(), soldier.tickCount()),
                    Component.translatable(HAMSTER_WHEEL_SPEED, soldierBlockData.getAdjustedSpeed()).withStyle(ChatFormatting.DARK_GRAY)
            );
            tooltip.addHorizontalLine();
        }
        if (detail && blockEntity.hasEnergyStorage()) {
            long generating = 0;
            if (soldierBlockData != null) {
                generating = HamsterWheelEnergyStorage.energyGeneratedPerTick(soldierBlockData.getAdjustedSpeed());
            }

            tooltip.add(Component.translatable(GENERATING, generating));
        }
    }

    @Override
    public Identifier getUniqueId() {
        return JadeRegistry.HAMSTER_WHEEL_BLOCK;
    }

    @Override
    public Class<? extends Block> getTargetClass() {
        return HamsterWheelBlock.class;
    }
}
