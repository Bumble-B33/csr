package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityServerAppender;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum ClayWraithProvider implements CommonEntityProvider<ClayWraithEntity>, CommonEntityServerAppender<ClayWraithEntity> {
    INSTANCE;

    private static final String SERVER_DATA_LIMITED_LIFE_TAG = "Limited";
    private static final String SERVER_DATA_LIMITED_LIFE_TIME_TAG = "LimitedTime";
    public static final String LIMITED_LIFE = JadeRegistry.getLangKey(INSTANCE, "limited_life");
    public static final String LIMITED_LIFE_FALSE = JadeRegistry.getLangKey(INSTANCE, "limited_life.false");
    public static final String LIMITED_LIFE_TIME = JadeRegistry.getLangKey(INSTANCE, "limited_life_time");


    @Override
    public void appendTooltip(ClayWraithEntity entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag serverData) {
        tooltip.add(Component.translatable(LIMITED_LIFE).append(": ").withStyle(ChatFormatting.DARK_GRAY));
        if (serverData.getBoolean(SERVER_DATA_LIMITED_LIFE_TAG)) {
            tooltip.append(Component.translatable(LIMITED_LIFE_TIME, serverData.getInt(SERVER_DATA_LIMITED_LIFE_TIME_TAG)).withStyle(ChatFormatting.RED));
        } else {
            tooltip.append(Component.translatable(LIMITED_LIFE_FALSE).withStyle(ChatFormatting.GREEN));
        }
    }


    @Override
    public void appendServerData(CompoundTag tag, ClayWraithEntity wraithEntity) {
        boolean limitedLife = wraithEntity.hasLimitedLife();
        int limitedLifeTime = wraithEntity.getLimitedLifeTicks() / 20;
        tag.putBoolean(SERVER_DATA_LIMITED_LIFE_TAG, limitedLife);
        if (limitedLife) {
            tag.putInt(SERVER_DATA_LIMITED_LIFE_TIME_TAG, limitedLifeTime);
        }

    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.CLAY_WRAITH;
    }

    @Override
    public Class<ClayWraithEntity> getTargetClass() {
        return ClayWraithEntity.class;
    }

    @Override
    public boolean requiresServerData() {
        return true;
    }
}
