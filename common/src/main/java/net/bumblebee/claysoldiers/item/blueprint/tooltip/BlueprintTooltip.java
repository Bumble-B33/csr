package net.bumblebee.claysoldiers.item.blueprint.tooltip;

import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record BlueprintTooltip(ResourceKey<BlueprintData> requirements) implements TooltipComponent {
}
