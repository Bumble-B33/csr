package net.bumblebee.claysoldiers.integration.jade.providers;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.VampireClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.integration.jade.CommonEntityProvider;
import net.bumblebee.claysoldiers.integration.jade.CommonTooltipHelper;
import net.bumblebee.claysoldiers.integration.jade.JadeRegistry;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public enum ClaySoldierProvider implements CommonEntityProvider<AbstractClaySoldierEntity> {
    INSTANCE;

    public static final String SOLDIER_PROPERTIES = JadeRegistry.getLangKey(INSTANCE, "soldier_properties");
    public static final String ALPHA_PREFIX = JadeRegistry.getLangKey(INSTANCE, "alpha_prefix");
    public static final String OFFSET_COLOR = JadeRegistry.getLangKey(INSTANCE, "offset_color");

    @Override
    public void appendTooltip(AbstractClaySoldierEntity claySoldier, CommonTooltipHelper tooltip, boolean detail, CompoundTag tag) {
        if (claySoldier.getType().is(ModTags.EntityTypes.CLAY_BOSS)) {
            return;
        }

        formatAttackTypeProperty(tooltip, claySoldier.getAttackType(), claySoldier);
        formatOffsetColor(tooltip, claySoldier);

        if (detail) {
            tooltip.add(Component.translatable(SOLDIER_PROPERTIES).append(":").withStyle(ChatFormatting.DARK_GRAY));
            ArrayList<Component> components = new ArrayList<>();
            ComponentFormating.formatProperties(components, claySoldier.allProperties(), List.of(
                    SoldierPropertyTypes.PROTECTION.get(),
                    SoldierPropertyTypes.ATTACK_TYPE.get()
            ), claySoldier);
            components.forEach(tooltip::add);
        }
    }


    private static void formatAttackTypeProperty(CommonTooltipHelper tooltip, AttackTypeProperty property, AbstractClaySoldierEntity claySoldier) {
        var name = property.getAnimatedDisplayName(claySoldier);
        if (name != null) {
            if (claySoldier instanceof VampireClaySoldierEntity vampireClaySoldier && vampireClaySoldier.isAlpha()) {
                tooltip.add(Component.translatable(ALPHA_PREFIX).withStyle(AttackTypeProperty.VAMPIRE.getAnimatedStyle(claySoldier)));
                tooltip.append(CommonComponents.SPACE);
                tooltip.append(name);
            } else {
                tooltip.add(name);
            }
        }
    }
    private static void formatOffsetColor(CommonTooltipHelper tooltip, AbstractClaySoldierEntity soldier) {
       if (soldier.hasOffsetColor()) {
           tooltip.add(CommonComponents.SPACE);
           tooltip.append(Component.translatable(OFFSET_COLOR).append(": "));
           var color = soldier.getOffsetColor();
           tooltip.append(color.formatDynamic(soldier));
       }
    }

    @Override
    public ResourceLocation getUniqueId() {
        return JadeRegistry.CLAY_SOLDIER;
    }

    @Override
    public Class<AbstractClaySoldierEntity> getTargetClass() {
        return AbstractClaySoldierEntity.class;
    }
}