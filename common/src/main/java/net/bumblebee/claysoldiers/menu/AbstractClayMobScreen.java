package net.bumblebee.claysoldiers.menu;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClayMobScreen<C extends ClayMobEntity, T extends AbstractClayMobMenu<C>> extends AbstractContainerScreen<T> {
    public static final String CLAY_TEAM_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".clay_team";
    public static final String CLAY_TEAM_LOYAL_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".clay_team_loyalty";
    public static final String SLOT_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".slot";

    protected static final int GRAY_COLOR = 0x404040;
    protected int teamPropertiesY = 6;
    protected int teamPropertiesX = 104;

    public AbstractClayMobScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GRAY_COLOR, false);
        List<Label> labels = new ArrayList<>();
        queLabels(labels);
        int labelY = teamPropertiesY;
        for (Label label : labels) {
            label.render(pGuiGraphics, this.font, teamPropertiesX, labelY);
            labelY += label.height;
        }
    }

    protected Component getTeamLabel(ClayMobEntity clayMob) {
        return Component.translatable(CLAY_TEAM_LABEL, clayMob.getClayTeam().getDisplayName());
    }

    protected void queLabels(List<Label> labels) {
        menu.forSourceIfPresent(clayMob -> {
            if (!clayMob.getType().is(ModTags.EntityTypes.CLAY_BOSS)) {
                labels.add(new Label(getTeamLabel(clayMob), clayMob.getTeamColor()));
            }
            if (clayMob.hasClayTeamOwner()) {
                labels.add(new Label(Component.translatable(CLAY_TEAM_LOYAL_LABEL, clayMob.getOwnerDisplayName()), GRAY_COLOR, 0.75f, 1));
            }
        });
    }

    /**
     * Renders the ClayMob the menu.
     */
    protected void renderSource(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale, float yOffset, int mouseX, int mouseY) {
        menu.forSourceIfPresent(
                clayMob -> InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x1, y1, x2, y2, scale, yOffset, mouseX, mouseY, clayMob)
        );
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(8, 84 + menu.inventoryYOffset, 161, 75, mouseX, mouseY)) {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
            return;
        }
        if (!menu.getCarried().isEmpty() || hoveredSlot == null) {
            return;
        }

        if (this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            renderSpecialTooltip(guiGraphics, itemstack, mouseX, mouseY);
        } else if (hoveredSlot instanceof AbstractClayMenuSlot clayMenuSlot) {
            guiGraphics.renderTooltip(this.font, Component.translatable(SLOT_LABEL, clayMenuSlot.getDisplayName()).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        }

    }

    /**
     * Renders the Special tooltip of the give Item. Usually displaying the bonus for the ClayMob
     */
    protected abstract void renderSpecialTooltip(GuiGraphics pGuiGraphics, ItemStack stack, int mouseX, int mouseY);

    /**
     * Adds the name of the Item to the give tooltip
     */
    protected void addItemName(List<Component> tooltip, ItemStack stack) {
        MutableComponent itemName = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            itemName.withStyle(ChatFormatting.ITALIC);
        }
        tooltip.add(itemName);
    }

    /**
     * Adds the currently hovered slot name to the tooltip
     */
    protected void addSlotName(List<Component> tooltip) {
        if (hoveredSlot instanceof AbstractClayMenuSlot slot) {
            tooltip.add(Component.translatable(SLOT_LABEL, slot.getDisplayName()).withStyle(ChatFormatting.GRAY));
        }
    }

    public record Label(Component text, int color, float scale, int height) {
        public Label(Component text, int color) {
            this(text,  color, 1f, 10);
        }

        public void render(GuiGraphics guiGraphics, Font font, int x, int y) {
            if (scale != 1f) {
                var pose = guiGraphics.pose();
                pose.pushPose();
                pose.scale(scale, scale, 1);
                guiGraphics.drawString(font, text, (int) (x / scale), (int) (y / scale), color, false);
                pose.popPose();
            } else {
                guiGraphics.drawString(font, text, x, y, color, false);
            }
        }
    }
}
