package net.bumblebee.claysoldiers.item.blueprint.tooltip;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record BlueprintTooltip(ResourceLocation requirements) implements TooltipComponent {
}
