package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlock;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSingleSoldier;
import net.bumblebee.claysoldiers.block.soldiercontainer.OccupantSoldierData;
import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.bumblebee.claysoldiers.integration.jade.CommonBlockProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public enum SugarCaneHammockProvider implements CommonBlockProvider {
    INSTANCE;

    @Override
    public void appendTooltip(BlockData data, CommonTooltipHelper tooltip, boolean detail) {
        BlockEntityWithSingleSoldier blockEntity = (BlockEntityWithSingleSoldier) data.entity();
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
                    ClayMobProvider.INSTANCE.createTeamName(soldier.getClayTeam(), soldier.colorOffset(), soldier.tickCount())
            );
        }
    }

    @Override
    public Identifier getUniqueId() {
        return JadeRegistry.SUGAR_CANE_HAMMOCK_BLOCK;
    }

    @Override
    public Class<? extends Block> getTargetClass() {
        return SugarCaneHammockBlock.class;
    }
}
