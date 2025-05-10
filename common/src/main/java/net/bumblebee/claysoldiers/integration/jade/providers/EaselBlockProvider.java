package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.block.blueprint.EaselBlock;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonBlockProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public enum EaselBlockProvider implements CommonBlockProvider {
    INSTANCE;

    public static final String EASEL_REMAINING_LANG = JadeRegistry.getLangKey(INSTANCE, "remaining");
    public static final String EASEL_FINISHED_LANG = JadeRegistry.getLangKey(INSTANCE, "finished");

    @Override
    public void appendTooltip(BlockData data, CommonTooltipHelper tooltip, boolean detail) {
        EaselBlockEntity blockEntity = ((EaselBlockEntity) data.entity());
        if (blockEntity.hasBlueprintData()) {
            tooltip.add(blockEntity.getBlueprintData().getDisplayName());
            if (blockEntity.isFinished()) {
                tooltip.add(Component.translatable(EASEL_FINISHED_LANG));
            } else {
                tooltip.add(Component.translatable(EASEL_REMAINING_LANG));
            }
        }

        var items = blockEntity.getRequiredItems();
        for (int i = 0; i < items.size(); i++) {
            if (i % 6 == 0) {
                tooltip.addItemStack(items.get(i));
            } else {
                tooltip.appendItemStack(items.get(i));
            }
        }
    }



    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.EASEL_BLOCK;
    }

    @Override
    public Class<? extends Block> getTargetClass() {
        return EaselBlock.class;
    }
}
