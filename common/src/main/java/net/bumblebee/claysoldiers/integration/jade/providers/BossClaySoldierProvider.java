package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public enum BossClaySoldierProvider implements CommonEntityProvider<BossClaySoldierEntity> {
    INSTANCE;

    public static final String CLAY_SOLDIER_BOSS_NAME = JadeRegistry.getLangKey(INSTANCE, "boss_name");


    @Override
    public void appendTooltip(BossClaySoldierEntity entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag serverData) {
        tooltip.add(createTeamName(entity));

        var workStatus = entity.getWorkStatus();
        if (workStatus != null) {
            tooltip.add(workStatus);
        }

        if (ClaySoldiersCommon.PLATFORM.isDevEnv()) {
            tooltip.add(Component.translatable(ClaySoldierProvider.SOLDIER_PROPERTIES).append(":").withStyle(ChatFormatting.DARK_GRAY));
            ArrayList<Component> components = new ArrayList<>();
            ComponentFormating.formatProperties(components, entity.allProperties(), List.of(
                    SoldierPropertyTypes.PROTECTION.get(),
                    SoldierPropertyTypes.ATTACK_TYPE.get(),
                    SoldierPropertyTypes.SIZE.get(),
                    SoldierPropertyTypes.CAN_SWIM.get(),
                    SoldierPropertyTypes.SEE_INVISIBILITY.get()
            ), entity);
            components.forEach(tooltip::add);
        }
    }

    private Component createTeamName(BossClaySoldierEntity clayMob) {
        return Component.translatable(CLAY_SOLDIER_BOSS_NAME)
                .withColor(clayMob.getClayTeam().getColor(clayMob, 0));
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.BOSS_CLAY_SOLDIER;
    }

    @Override
    public Class<BossClaySoldierEntity> getTargetClass() {
        return BossClaySoldierEntity.class;
    }
}
