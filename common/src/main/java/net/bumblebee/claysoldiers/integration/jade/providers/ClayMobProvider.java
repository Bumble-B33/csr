package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public enum ClayMobProvider implements CommonEntityProvider<ClayMobEntity> {
    INSTANCE;

    public static final String CLAY_MOB_TEAM = JadeRegistry.getLangKey(INSTANCE, "team");

    @Override
    public void appendTooltip(ClayMobEntity entity, CommonTooltipHelper tooltip, boolean detail, CompoundTag tag) {
        if (entity.getType().is(ModTags.EntityTypes.CLAY_BOSS)) {
            return;
        }

        if (entity instanceof AbstractClaySoldierEntity soldier) {
            addAirBubbles(soldier, tooltip::airBubbles);
        }

        tooltip.add(createTeamName(entity));

        var workStatus = entity.getWorkStatus();
        if (workStatus != null) {
            tooltip.add(workStatus);
        }
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.CLAY_MOB;
    }

    @Override
    public Class<ClayMobEntity> getTargetClass() {
        return ClayMobEntity.class;
    }

    public Component createTeamName(ClayMobEntity clayMob) {
        return Component.translatable(ClayMobProvider.CLAY_MOB_TEAM).withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(clayMob.getClayTeam().getDisplayNameWithColor(c -> c.getColor(clayMob, 0)));
    }

    private static void addAirBubbles(AbstractClaySoldierEntity soldier, BiConsumer<Integer, Boolean> adder) {
        if (!soldier.isInWater() || soldier.allProperties().breathHoldDuration() >= BreathHoldPropertyType.MAX_BREATH_HOLD) {
            return;
        }

        int breathPercent = (int) ((float) (soldier.getAirSupply() * 10) / soldier.getMaxAirSupply());
        int breathPercentAfter = (int) ((float) ((soldier.getAirSupply()-1) * 10) / soldier.getMaxAirSupply());
        adder.accept(breathPercent, breathPercent > breathPercentAfter);
    }
}
