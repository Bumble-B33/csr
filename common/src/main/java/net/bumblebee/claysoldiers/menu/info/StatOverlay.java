package net.bumblebee.claysoldiers.menu.info;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatOverlay {
    private static StatOverlay INSTANCE;
    private static final int BLACK = 0xFF000000 | ChatFormatting.DARK_GRAY.getColor();
    private static final int MIN_WITH = 75;

    private final int x;
    private final int y;
    private final Font font;
    private final int boxHeight;
    private int tick = 0;
    private int ticksSlowed = 0;
    private Map<ClayMobTeam, Long> cachedMap = Map.of();

    public StatOverlay(int x, int y, Font font) {
        this.x = x;
        this.y = y;
        this.font = font;
        this.boxHeight = 2 + font.lineHeight + 2;
    }

    public static StatOverlay getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatOverlay(10, 10, Minecraft.getInstance().font);
        }
        return INSTANCE;
    }

    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        if (!shouldDisplayStats()) {
            return;
        }
        if (ClaySoldiersCommon.CONFIG.getClientConfig().statItemShowStats()) {
            renderStats(guiGraphics, partialTick);
        }
        if (ClaySoldiersCommon.CONFIG.getClientConfig().statItemShowCount()) {
            renderCounts(guiGraphics, partialTick);
        }
        tick++;
        if (tick % 4 == 0) {
            ticksSlowed++;
        }
    }

    private void renderCounts(GuiGraphicsExtractor guiGraphics, float partialTick) {
        int yAd = y;
        for (var entry : getTeamsAndCount().entrySet()) {
            Component text = entry.getKey().getDisplayName().copy().append(" (" + entry.getValue() + ")");

            int width = Math.max(font.width(text) + 6, MIN_WITH);
            int teamColor = 0xFF000000 | entry.getKey().getColor(0, ticksSlowed, partialTick);

            guiGraphics.horizontalLine(x, x + width, yAd, BLACK);
            guiGraphics.horizontalLine(x, x + width, yAd + boxHeight, BLACK);

            guiGraphics.verticalLine(x, yAd, yAd + boxHeight, BLACK);
            guiGraphics.verticalLine(x + width, yAd, yAd + boxHeight, BLACK);

            guiGraphics.fill(x + 1, yAd + 1, x + width, yAd + boxHeight, teamColor);

            guiGraphics.text(font, text, x + 3, yAd + 3, 0xFFFFFFFF);

            yAd += 2 + boxHeight;
        }
    }

    private void renderStats(GuiGraphicsExtractor guiGraphics, float partialTicks) {
        if (Minecraft.getInstance().crosshairPickEntity instanceof StatInfoDisplay infoDisplay) {
            renderStats(guiGraphics, infoDisplay);
            return;
        }
        Entity entity = Minecraft.getInstance().getCameraEntity();
        HitResult hitresult = entity.pick(20.0F, 0.0F, false);
        if (hitresult.getType() == HitResult.Type.BLOCK) {
            if (Minecraft.getInstance().level.getBlockEntity(((BlockHitResult) hitresult).getBlockPos()) instanceof StatInfoDisplay infoDisplay) {
                renderStats(guiGraphics, infoDisplay);
            }
        }
    }

    private void renderStats(GuiGraphicsExtractor guiGraphics, StatInfoDisplay infoDisplay) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;

        List<Component> components = new ArrayList<>();
        infoDisplay.getStatDisplay(components, Minecraft.getInstance().player);

        guiGraphics.tooltip(
                font,
                components.stream().map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList(),
                width,
                height,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    private Map<ClayMobTeam, Long> getTeamsAndCount() {
        if (tick % 16 == 0) {
            cachedMap = getClaySoldiersNearby().stream()
                    .collect(Collectors.groupingBy(ClayMobEntity::getClayTeam, Collectors.counting()));
        }

        return cachedMap;
    }

    private static List<ClayMobEntity> getClaySoldiersNearby() {
        return Minecraft.getInstance().level.getEntitiesOfClass(ClayMobEntity.class, getBoxAroundPlayer(), ClayMobEntity::showInStatDisplay);
    }

    private static AABB getBoxAroundPlayer() {
        return new AABB(Minecraft.getInstance().player.getOnPos()).inflate(25);
    }

    private static boolean shouldDisplayStats() {
        if (Minecraft.getInstance().options.hideGui) {
            return false;
        }
        for (var test : ClaySoldiersCommon.IS_WEARING_STATOMETER) {
            var player = Minecraft.getInstance().player;
            if (test.test(player)) {
                return true;
            }
        }
        return false;
    }
}
