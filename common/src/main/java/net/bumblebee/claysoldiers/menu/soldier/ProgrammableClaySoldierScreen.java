package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ProgrammableClaySoldierScreen extends ClaySoldierScreen<ProgrammableClaySoldierMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/gui/container/programmable_clay_soldier_inventory.png");

    public ProgrammableClaySoldierScreen(ProgrammableClaySoldierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected Identifier getBackGroundTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderSpecialTooltip(GuiGraphicsExtractor guiGraphics, ItemStack stack, int mouseX, int mouseY) {
        if (hoveredSlot instanceof CarriedClaySoldierMenuSlot) {
            List<Component> tooltip = new ArrayList<>();
            addSlotName(tooltip);
            tooltip.addAll(getTooltipFromContainerItem(stack));
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
        } else if (hoveredSlot instanceof ChipClaySoldierSlot) {
            List<Component> tooltip = new ArrayList<>();
            ClaySoldierChipItem.appendHoverText(stack, _ -> true, tooltip::add);
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
        } else {
            super.renderSpecialTooltip(guiGraphics, stack, mouseX, mouseY);
        }
    }
}
