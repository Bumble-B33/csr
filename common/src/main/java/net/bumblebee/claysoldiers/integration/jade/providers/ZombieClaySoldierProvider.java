package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityServerAppender;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum ZombieClaySoldierProvider implements CommonEntityProvider<ZombieClaySoldierEntity>, CommonEntityServerAppender<ZombieClaySoldierEntity> {
    INSTANCE;

    private static final String SERVER_DATA_CURABLE_TAG = "Curable";
    public static final String PREVIOUS_CLAY_MOB_TEAM = "jade.plugin." + ClaySoldiersCommon.MOD_ID + ".previous_team";
    public static final String CURABLE = JadeRegistry.getLangKey(INSTANCE, "curable");
    public static final String CURABLE_TRUE = JadeRegistry.getLangKey(INSTANCE, "curable.false");
    public static final String CURABLE_FALSE = JadeRegistry.getLangKey(INSTANCE, "curable.true");

    @Override
    public void appendServerData(CompoundTag tag, ZombieClaySoldierEntity entity) {
        tag.putBoolean(SERVER_DATA_CURABLE_TAG, entity.isCurable());
    }

    @Override
    public void appendTooltip(ZombieClaySoldierEntity entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag serverData) {
        final ClayMobTeam previousTeam = entity.getPreviousTeam();

        tooltip.add(CommonComponents.SPACE);
        tooltip.append(Component.translatable(PREVIOUS_CLAY_MOB_TEAM, previousTeam.getDisplayNameWithColor(c -> c.getColor(entity, 0))).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(CommonComponents.SPACE);
        tooltip.append(Component.translatable(CURABLE).append(": ").withStyle(ChatFormatting.DARK_GRAY));
        if (serverData.isEmpty()) {
            return;
        }

        if (serverData.getBoolean(SERVER_DATA_CURABLE_TAG)) {
            tooltip.append(Component.translatable(CURABLE_TRUE).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.append(Component.translatable(CURABLE_FALSE).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.ZOMBIE_CLAY_SOLDIER;
    }

    @Override
    public Class<ZombieClaySoldierEntity> getTargetClass() {
        return ZombieClaySoldierEntity.class;
    }

    @Override
    public boolean requiresServerData() {
        return true;
    }
}
