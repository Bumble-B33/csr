package net.bumblebee.claysoldiers.block;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class BatteryContentRenderer {
    private static final Identifier ENERGY_PORT_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/energy_port.png");
    private static final Identifier ENERGY_PORT_OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/energy_port_overlay.png");

    public static final RenderType ENERGY_PORT_REDNER_TYPE = RenderTypes.entityCutout(ENERGY_PORT_TEXTURE);
    public static final RenderType ENERGY_PORT_REDNER_OVERLAY_TYPE = RenderTypes.entityCutout(ENERGY_PORT_OVERLAY_TEXTURE);


    private static final Identifier BATTERY_CONTENT_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final Supplier<TextureAtlasSprite> DEFAULT_TEXTURE = Suppliers.memoize(() -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(BATTERY_CONTENT_TEXTURE));

    private final Supplier<TextureAtlasSprite> sprite;

    private final float batteryHeight;
    private final float batteryWidth;
    private final float batteryDepth;

    private final float x0;
    private final float y0;
    private final float z0;

    public static BatteryContentRenderer ofDefaultTexture(int x, int z, int height) {
        return ofDefaultTexture(x, 0, z, height);
    }

    public static BatteryContentRenderer ofDefaultTexture(float x, float y, float z, int height) {
        return new BatteryContentRenderer(DEFAULT_TEXTURE,
                height, 3f, 3f,
                x, y, z
        );
    }

    public BatteryContentRenderer(Supplier<TextureAtlasSprite> sprite, float batteryHeight, float batteryWidth, float batteryDepth, float x0, float y0, float z0) {
        this.sprite = sprite;

        this.batteryHeight = batteryHeight;
        this.batteryWidth = batteryWidth;
        this.batteryDepth = batteryDepth;

        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
    }

    public void submitBatteryContent(float fillRatio, SubmitNodeCollector nodeCollector, PoseStack poseStack, int packedLight) {
        if (fillRatio <= 0) {
            return;
        }
        fillRatio = Math.min(fillRatio, 1f);


        float fluidTop = y0 + Math.max(0.1f, batteryHeight * fillRatio);

        float x1 = x0 + batteryWidth;
        float z1 = z0 + batteryDepth;

        float u0 = sprite.get().getU0();
        float u1 = sprite.get().getU1();

        float v0 = sprite.get().getV0();
        float v1 = sprite.get().getV1();

        // Scale texture vertically with fill level
        float sideV1 = (v1 - v0) * fillRatio + v0;

        // Optional: use half the texture for the top surface
        float topV1 = (v1 - v0) * 0.5f + v0;
        float topU1 = (u1 - u0) * 0.5f + u0;

        /*
         * TOP
         */
        drawQuad(nodeCollector, poseStack,
                x0, fluidTop, z0, x0, fluidTop, z1, x1, fluidTop, z1, x1, fluidTop, z0,
                u0, v0, topU1, topV1,
                packedLight);

        /*
         * FRONT (z0)
         */
        drawQuad(nodeCollector, poseStack,
                x0, y0, z0, x0, fluidTop, z0, x1, fluidTop, z0, x1, y0, z0,
                u0, v0, u1, sideV1,
                packedLight);

        /*
         * BACK (z1)
         */
        drawQuad(nodeCollector, poseStack,
                x1, y0, z1, x1, fluidTop, z1, x0, fluidTop, z1, x0, y0, z1,
                u0, v0, u1, sideV1,
                packedLight);

        /*
         * LEFT (x0)
         */
        drawQuad(nodeCollector, poseStack,
                x0, y0, z1, x0, fluidTop, z1, x0, fluidTop, z0, x0, y0, z0,
                u0, v0, u1, sideV1,
                packedLight);

        /*
         * RIGHT (x1)
         */
        drawQuad(nodeCollector, poseStack,
                x1, y0, z0, x1, fluidTop, z0, x1, fluidTop, z1, x1, y0, z1,
                u0, v0, u1, sideV1,
                packedLight);
    }


    public void submitBatteryContent(long energyStored, long maxEnergyStored, SubmitNodeCollector nodeCollector, PoseStack poseStack, int packedLight) {
        submitBatteryContent((float) energyStored / maxEnergyStored, nodeCollector, poseStack, packedLight);
    }

    public static void drawQuad(SubmitNodeCollector nodeCollector, PoseStack poseStack,
                                float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
                                float u0, float v0, float u1, float v1,
                                int packedLight) {
        int color = ClaySoldiersCommon.ENERGY_HELPER.getEnergyColor();
        nodeCollector.submitCustomGeometry(poseStack, Minecraft.useShaderTransparency() ? RenderTypes.solidMovingBlock() : RenderTypes.translucentMovingBlock(),

                (pose, vertexConsumer) -> {

                    drawVertex(vertexConsumer, pose, x1 / 16f, y1 / 16f, z1 / 16f, u0, v0, 0, 1, 0, packedLight, color);

                    drawVertex(vertexConsumer, pose, x2 / 16f, y2 / 16f, z2 / 16f, u0, v1, 0, 1, 0, packedLight, color);

                    drawVertex(vertexConsumer, pose, x3 / 16f, y3 / 16f, z3 / 16f, u1, v1, 0, 1, 0, packedLight, color);

                    drawVertex(vertexConsumer, pose, x4 / 16f, y4 / 16f, z4 / 16f, u1, v0, 0, 1, 0, packedLight, color);
                });
    }

    public static void drawVertex(VertexConsumer builder, PoseStack.Pose pose,
                                   float x, float y, float z,
                                   float u, float v,
                                   float n0, float n1, float n2,
                                   int packedLight, int color) {
        builder.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(n0, n1, n2);
    }
}