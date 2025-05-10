package net.bumblebee.claysoldiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;

public enum BlueprintPackSource implements PackSource {
    INSTANCE;

    @Override
    public Component decorate(Component name) {
        if (name.getString().equals(ClaySoldiersCommon.MOD_ID)) {
            return Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_DESCRIPTION);
        }
        return Component.translatable("pack.nameAndSource", name, Component.translatable(ClaySoldiersCommon.BLUEPRINT_PACK_SOURCE)).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public boolean shouldAddAutomatically() {
        return false;
    }
}
