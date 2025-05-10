package net.bumblebee.claysoldiers.menu.soldier;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.menu.AbstractClayMobScreen;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClaySoldierScreen extends AbstractClayMobScreen<AbstractClaySoldierEntity, ClaySoldierMenu> {
    public static final String PREVIOUS_CLAY_TEAM_LABEL = "gui.label." + ClaySoldiersCommon.MOD_ID + ".previous_clay_team";
    public static final String REVIVE_TYPE_COOLDOWN = "gui.label." + ClaySoldiersCommon.MOD_ID + ".revive_type_cooldown";
    public static final String REVIVE_TYPE_COOLDOWN_ENTRY = "gui.label." + ClaySoldiersCommon.MOD_ID + ".revive_type_cooldown_entry";
    public static final String SOLDIER_PROPERTIES = "gui.label." + ClaySoldiersCommon.MOD_ID + ".soldier_properties";

    public static final float REVIVE_SCALE = 0.75f;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/gui/container/clay_soldier_inventory.png");

    public ClaySoldierScreen(ClaySoldierMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 192;
        this.teamPropertiesY = 16;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight+2);
        renderSource(guiGraphics, x + 26, y + 18, x + 75, y + 78, 60, 0.0625F, pMouseX, pMouseY);
    }


    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (isHovering(24, 16, 50, 70, pMouseX, pMouseY)) {
            menu.forSourceIfPresent(soldier -> renderProperties(pGuiGraphics, soldier.allProperties(), pMouseX, pMouseY));
        }
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderSpecialTooltip(GuiGraphics pGuiGraphics, ItemStack stack, int mouseX, int mouseY) {
        var soldierProperties = ClaySoldiersCommon.DATA_MAP.getEffect(stack);
        if (soldierProperties == null) {
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        addItemName(tooltip, stack);
        addSlotName(tooltip);
        var predicateDisplayName = soldierProperties.predicate().getDisplayName();
        menu.forSourceIfPresent(soldier -> {
                    ComponentFormating.formatProperties(tooltip, soldierProperties.properties(), List.of(), soldier);
                    if (predicateDisplayName != null) {
                        tooltip.add(predicateDisplayName);
                    }
                    pGuiGraphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
                }
        );
    }

    public void renderProperties(GuiGraphics guiGraphics, SoldierPropertyMapReader properties, int pX, int pY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(SOLDIER_PROPERTIES).withStyle(ChatFormatting.DARK_GRAY));
        ComponentFormating.formatProperties(tooltip, properties, List.of(SoldierPropertyTypes.ATTACK_TYPE.get()), menu.getSource().orElse(null));
        guiGraphics.renderComponentTooltip(font, tooltip, pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void queLabels(List<Label> labels) {
        super.queLabels(labels);
        menu.forSourceIfPresent(claySoldier -> {
            if (claySoldier instanceof ZombieClaySoldierEntity zombie) {
                labels.add(1, new Label(getZombieTeamLabel(zombie), GRAY_COLOR, 0.75f, 6));
            }
            var entrySet = claySoldier.getReviveTypeCooldown().entrySet();
            if (!entrySet.isEmpty()) {
                labels.add(new Label(Component.translatable(REVIVE_TYPE_COOLDOWN), GRAY_COLOR));
                for (var entry : entrySet) {
                    labels.add(new Label(Component.translatable(REVIVE_TYPE_COOLDOWN_ENTRY, entry.getKey().getAnimatedDisplayName(claySoldier), entry.getValue() / 20), GRAY_COLOR, 0.75f, 10));
                }
            }
        });

    }

    protected static Component getZombieTeamLabel(ZombieClaySoldierEntity zombie) {
        return Component.translatable(PREVIOUS_CLAY_TEAM_LABEL, zombie.getPreviousTeam().getDisplayNameWithColor(c -> c.getColor(zombie, 0)));
    }
}
