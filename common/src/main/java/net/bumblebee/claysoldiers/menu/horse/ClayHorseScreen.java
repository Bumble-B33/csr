package net.bumblebee.claysoldiers.menu.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseItemMap;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.menu.AbstractClayMobScreen;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClayHorseScreen extends AbstractClayMobScreen<AbstractClayHorse, ClayHorseMenu> {
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/gui/container/clay_horse_inventory.png");
    public static final String CLAY_RIDER_TEAM_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".rider_clay_team";


    public ClayHorseScreen(ClayHorseMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.teamPropertiesX = 85;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderSource(pGuiGraphics, x + 26, y + 18, x + 78, y + 70, 51, 0.25F, pMouseX, pMouseY);
    }


    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected Component getTeamLabel(ClayMobEntity clayMob) {
        if (clayMob.isVehicle()) {
            return Component.translatable(CLAY_RIDER_TEAM_LABEL, clayMob.getClayTeam().getDisplayName());
        }
        return Component.empty();
    }

    @Override
    protected void renderSpecialTooltip(GuiGraphics pGuiGraphics, ItemStack stack, int mouseX, int mouseY) {
        var clayHorseProperties = ClayHorseItemMap.get(stack);
        if (clayHorseProperties == null) {
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        addItemName(tooltip, stack);
        addSlotName(tooltip);
        ComponentFormating.formatClayHorseProperties(clayHorseProperties, tooltip);
        pGuiGraphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
    }
}
