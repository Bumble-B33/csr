package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.entity.soldier.VampireClaySoldierEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum VampireClaySoldierProvider implements CommonEntityProvider<VampireClaySoldierEntity> {
    INSTANCE;

    public static final String POWER = JadeRegistry.getLangKey(INSTANCE, "power");

    private static float roundPower(float power) {
        return ((int) (power * 10)) / 10f;
    }

    @Override
    public void appendTooltip(VampireClaySoldierEntity entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag tag) {
        tooltip.add(Component.translatable(POWER, roundPower(entity.getPowerMultiplier())));
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.VAMPIRE_SOLDIER;
    }

    @Override
    public Class<VampireClaySoldierEntity> getTargetClass() {
        return VampireClaySoldierEntity.class;
    }
}
