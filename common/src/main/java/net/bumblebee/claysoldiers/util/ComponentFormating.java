package net.bumblebee.claysoldiers.util;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.clayremovalcondition.RemovalCondition;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseWearableProperties;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.translation.ITranslatableProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class ComponentFormating {
    public static final String SOLDIER_PROPERTIES_EQUIP = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_properties_equip";
    public static final String SOLDIER_PROPERTIES_EQUIP_AND_PREDICATE = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_properties_equip_predicate";
    public static final String CLAY_HORSE_PROPERTIES_EQUIP = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".clay_horse_properties_equip";
    public static final String SOLDIER_BECOMES_ATTACK_TYPE = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_becomes_attack_type";

    public static final String SOLDIER_POI_ITEM = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_poi_item";
    public static final String SOLDIER_POI_BLOCK = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_poi_block";

    public static final String SOLDIER_POI_PREDICATE = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_poi_predicate";
    public static final String SOLDIER_POI_EFFECT = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_poi_effect";
    public static final String SOLDIER_ITEM_PICKUP_EFFECT = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_pickup_effect";
    public static final String SOLDIER_ITEM_PICKUP_REMOVE = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".soldier_removal_condition";

    public static final String CLAY_HORSE_PROTECTION = "item.tooltip." + ClaySoldiersCommon.MOD_ID + ".clay_horse.protection";

    private static final String DOUBLE_SPACE = "  ";

    public static void addHoldableTooltip(@Nullable SoldierHoldableEffect holdableEffect, List<Component> tooltip) {
        if (holdableEffect == null) {
            return;
        }
        var predicateDisplayName = holdableEffect.predicate().getDisplayName();
        if (predicateDisplayName != null) {
            tooltip.add(CommonComponents.space().append(Component.translatable(ComponentFormating.SOLDIER_PROPERTIES_EQUIP_AND_PREDICATE).withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(CommonComponents.space().append(CommonComponents.SPACE).append(predicateDisplayName).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable(ComponentFormating.SOLDIER_PROPERTIES_EQUIP).withStyle(ChatFormatting.DARK_GRAY));
        }
        var attackTypeLang = holdableEffect.properties().attackType().getAnimatedDisplayName(Minecraft.getInstance().player);
        if (attackTypeLang != null) {
            tooltip.add(Component.translatable(SOLDIER_BECOMES_ATTACK_TYPE, attackTypeLang).withStyle(ChatFormatting.DARK_GRAY));
        }

        ComponentFormating.formatProperties(tooltip, holdableEffect.properties(), List.of(SoldierPropertyTypes.ATTACK_TYPE.get()), null);
        ComponentFormating.formatListDisplayNamesMultiLine(tooltip, holdableEffect.getOnPickUpFunctions(), Component.translatable(ComponentFormating.SOLDIER_ITEM_PICKUP_EFFECT), clayPoiFunction -> clayPoiFunction.getDisplayNameDynamic(Minecraft.getInstance().player));
        ComponentFormating.formatListDisplayNames(tooltip, holdableEffect.getRemovalConditions(), Component.translatable(ComponentFormating.SOLDIER_ITEM_PICKUP_REMOVE), RemovalCondition::getDisplayName);
    }

    public static void addPoiTooltip(@Nullable SoldierPoi poi, List<Component> tooltip) {
        if (poi == null) {
            return;
        }
        var predicateDisplayName = poi.getPredicate().getDisplayName();
        if (predicateDisplayName != null) {
            tooltip.add(CommonComponents.space().append(Component.translatable(ComponentFormating.SOLDIER_POI_PREDICATE).withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.literal("  ").append(predicateDisplayName).withStyle(ChatFormatting.DARK_GRAY));
        }
        ComponentFormating.formatListDisplayNamesMultiLine(tooltip, poi.getEffects(), Component.translatable(ComponentFormating.SOLDIER_POI_EFFECT),
                clayPoiFunction -> clayPoiFunction.getDisplayNameDynamic(Minecraft.getInstance().player));
    }

    /**
     * Formats all given properties.
     * @param properties all properties
     * @param hiddenProperties properties to ignore
     */
    public static void formatProperties(List<Component> tooltip, Iterable<SoldierProperty<?>> properties, List<SoldierPropertyType<?>> hiddenProperties, @Nullable ClaySoldierInventoryQuery soldier) {
        for (SoldierProperty<?> soldierProperty : properties) {
            if (hiddenProperties.contains(soldierProperty.type())) {
                continue;
            }
            formatProperty(tooltip, soldierProperty, soldier);
        }
    }
    private static void formatProperty(List<Component> tooltip, SoldierProperty<?> property, @Nullable ClaySoldierInventoryQuery soldier) {
        var propertyValue = property.value();
        var propertyComponent = CommonComponents.space().withStyle(ChatFormatting.DARK_GRAY);

        if (property.type().is(ModTags.SoldierPropertyTypes.REQUIRES_OWNER) && soldier != null && soldier.getClayTeamOwner() != null) {
            return;
        }

        tooltip.add(propertyComponent);
        if (propertyValue instanceof Iterable<?> listValue) {
            propertyComponent.append(property.type().getDisplayName());
            propertyComponent.append(Component.literal(":"));
            formatList(tooltip, listValue);

        } else if (propertyValue instanceof ITranslatableProperty soldierProperty) {
            var displayName = soldierProperty.getAnimatedDisplayName(Minecraft.getInstance().player);
            if (displayName != null) {
                propertyComponent.append(displayName);
            }
        } else {
            var compList = ComponentFormating.formatProperty(property, soldier);
            if (compList.isEmpty()) {
                tooltip.removeLast();
                return;
            }

            propertyComponent.append(compList.getFirst());
            for (int i = 1; i < compList.size();i++) {
                tooltip.add(compList.get(i));
            }
        }
    }
    private static void formatList(List<Component> tooltip, Iterable<?> values) {
        for (Object value : values) {
            var propertyComponent = CommonComponents.space();
            tooltip.add(propertyComponent);
            propertyComponent.append(CommonComponents.SPACE);
            propertyComponent.append(CommonComponents.SPACE).withStyle(ChatFormatting.DARK_GRAY);
            if (value instanceof ITranslatableProperty soldierProperty) {
                var displayName = soldierProperty.getAnimatedDisplayName(Minecraft.getInstance().player);
                if (displayName != null) {
                    propertyComponent.append(displayName);
                }
            } else {
                propertyComponent.append(Component.literal(value.toString()));
            }
        }
    }
    private static <T> List<Component> formatProperty(SoldierProperty<T> property, @Nullable ClaySoldierInventoryQuery soldier) {
        return property.type().getDisplayNameWithValue(property.value(), soldier);
    }

    public static void formatClayHorseProperties(@Nullable ClayHorseWearableProperties effect, List<Component> tooltip) {
        if (effect == null) {
            return;
        }
        tooltip.add(Component.translatable(ComponentFormating.CLAY_HORSE_PROPERTIES_EQUIP).withStyle(ChatFormatting.DARK_GRAY));
        if (effect.protection() != 0) {
            tooltip.add(CommonComponents.space().append(Component.translatable(CLAY_HORSE_PROTECTION, effect.protection())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * Will formated as a list like this:
     * <pre>
     * 1. Heading
     * 2.  Effect1
     * 3.  Effect2
     * ...</pre>
     */
    public static <T> void formatListDisplayNames(List<Component> tooltip, Collection<T> effects, Component heading, Function<T, Component> displayNameGetter) {
        if (!effects.isEmpty()) {
            tooltip.add(CommonComponents.space().append(heading).withStyle(ChatFormatting.DARK_GRAY));
        }
        for (var effect : effects) {
            Component displayName = displayNameGetter.apply(effect);
            if (displayName != null) {
                tooltip.add(Component.literal(DOUBLE_SPACE).append(displayName).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    public static <T> void formatListDisplayNamesMultiLine(List<Component> tooltip, Collection<T> effects, Component heading, Function<T, List<Component>> displayNameGetter) {
        if (!effects.isEmpty()) {
            tooltip.add(CommonComponents.space().append(heading).withStyle(ChatFormatting.DARK_GRAY));
        }
        for (var effect : effects) {
            displayNameGetter.apply(effect).forEach(component -> tooltip.add(Component.literal(DOUBLE_SPACE).append(component).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }
}
