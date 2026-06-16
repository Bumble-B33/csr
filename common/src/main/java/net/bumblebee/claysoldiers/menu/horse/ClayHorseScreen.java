package net.bumblebee.claysoldiers.menu.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.menu.AbstractClayMobScreen;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClayHorseScreen extends AbstractClayMobScreen<AbstractClayHorse, ClayHorseMenu> {
    private static final Identifier HORSE_INVENTORY_LOCATION = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/gui/container/clay_horse_inventory.png");
    public static final String CLAY_RIDER_TEAM_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".rider_clay_team";


    public ClayHorseScreen(ClayHorseMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.teamPropertiesX = 85;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, HORSE_INVENTORY_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderSource(pGuiGraphics, x + 26, y + 18, x + 78, y + 70, 51, 0.25F, pMouseX, pMouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected Component getTeamLabel(ClayMobEntity clayMob) {
        if (clayMob.isVehicle()) {
            return Component.translatable(CLAY_RIDER_TEAM_LABEL, clayMob.getClayTeam().getDisplayName());
        }
        return Component.empty();
    }

    @Override
    protected void renderSpecialTooltip(GuiGraphicsExtractor pGuiGraphics, ItemStack stack, int mouseX, int mouseY) {
        var clayHorseProperties = ClaySoldiersCommon.DATA_MAP.getHorseArmor(stack);
        if (clayHorseProperties == null) {
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        addItemName(tooltip, stack);
        addSlotName(tooltip);
        ComponentFormating.formatClayHorseProperties(clayHorseProperties, tooltip);
        pGuiGraphics.setTooltipForNextFrame(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
    }
}
