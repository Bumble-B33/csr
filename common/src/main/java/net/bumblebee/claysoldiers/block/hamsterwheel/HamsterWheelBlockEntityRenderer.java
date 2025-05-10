package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class HamsterWheelBlockEntityRenderer implements BlockEntityRenderer<HamsterWheelBlockEntity> {
    private static final ResourceLocation HAMSTER_WHEEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/block/hamster_wheel.png");
    private static final ResourceLocation BATTERY_CONTENT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");

    private static final RenderType RENDER_TYPE_BLOCK = RenderType.entityCutoutNoCull(HAMSTER_WHEEL_TEXTURE);
    private static final float DEG_90 = Mth.PI / 2;
    private static final float WHEEL_SPEED = Mth.PI * 0.056f;

    public static final ModelLayerLocation STAND_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_stand"), "main");
    public static final ModelLayerLocation POWER_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_power"), "main");


    private final ModelPart stand;
    private final ModelPart powerConnection;
    private final ModelPart batteryLeft;
    private final ModelPart batteryRight;

    private final HamsterWheelModel wheelModel;

    public HamsterWheelBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
        this.stand = pContext.bakeLayer(STAND_LAYER_LOCATION);
        this.powerConnection = pContext.bakeLayer(POWER_LAYER_LOCATION);
        this.wheelModel = new HamsterWheelModel(pContext.bakeLayer(HamsterWheelModel.LAYER_LOCATION));
        this.batteryLeft = pContext.bakeLayer(BatteryType.LEFT.getLayerLocation());
        this.batteryRight = pContext.bakeLayer(BatteryType.RIGHT.getLayerLocation());
    }

    @Override
    public void render(HamsterWheelBlockEntity hamsterWheelBlock, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        var optProfiler = new OptionalProfiler(hamsterWheelBlock.getLevel());
        optProfiler.push("hamsterWheelRender");
        float yRot = hamsterWheelBlock.getBlockState().getValue(HamsterWheelBlock.FACING).getOpposite().toYRot();

        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer wheelBuilder = pBuffer.getBuffer(RENDER_TYPE_BLOCK);
        hamsterWheelBlock.clientTick(pPartialTick);

        stand.render(pPoseStack, wheelBuilder, pPackedLight, pPackedOverlay, -1);


        wheelModel.setUpRotation((hamsterWheelBlock.getRotationTick(pPartialTick) % 251) * WHEEL_SPEED);
        wheelModel.renderToBuffer(pPoseStack, wheelBuilder, pPackedLight, pPackedOverlay, -1);

        if (hamsterWheelBlock.hasEnergyStorage()) {
            optProfiler.push("batteryRender");

            powerConnection.render(pPoseStack, wheelBuilder, pPackedLight, pPackedOverlay, -1);

            batteryLeft.render(pPoseStack, pBuffer.getBuffer(RENDER_TYPE_BLOCK), pPackedLight, pPackedOverlay, -1);
            renderBatterContent(hamsterWheelBlock, pBuffer, pPoseStack, pPackedLight);

            if (hamsterWheelBlock.hasSecondBattery()) {
                batteryRight.render(pPoseStack, pBuffer.getBuffer(RENDER_TYPE_BLOCK), pPackedLight, pPackedOverlay, -1);

                pPoseStack.pushPose();
                pPoseStack.translate(0.5625f, 0, 0);

                renderBatterContent(hamsterWheelBlock, pBuffer, pPoseStack, pPackedLight);

                pPoseStack.popPose();
            }



            optProfiler.pop();
        }
        HamsterWheelSoldierData data = hamsterWheelBlock.getSoldierData();
        if (data != null) {
            optProfiler.push("soldierRender");

            pPoseStack.translate(0.5f, 0.1f, 0.5f);
            pPoseStack.mulPose(Axis.YP.rotation(DEG_90));
            data.getClientSoldier().render(0, pPartialTick, pPoseStack, pBuffer, pPackedLight);

            optProfiler.pop();
        }

        optProfiler.pop();

    }

    private static void renderBatterContent(HamsterWheelBlockEntity entity, MultiBufferSource buffer, PoseStack pPoseStack, int pPackedLight) {
        IHamsterWheelEnergyStorage energy = entity.getEnergyStorage(null);
        if (energy == null || energy.energyStored() == 0) {
            return;
        }
        float height = Math.max(0.1f, 6f * energy.energyStored() / energy.maxEnergyStored());


        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(BATTERY_CONTENT_TEXTURE);
        VertexConsumer builder = buffer.getBuffer(Minecraft.useShaderTransparency() ? RenderType.solid() : RenderType.translucent());


        float textureScale = height / 6f;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        // Texture Width
        u1 = (u1 - u0) * 0.5f + u0;


        //Top
        if (height <= 5) {
            drawQuad(builder, pPoseStack, 2, height, 12, 5, height, 15, u0, v0, u1, ((v1 - v0) * 0.5f + v0), pPackedLight);
        }
        if (height <= 1) {
            return;
        }

        // Scaling Texture Height
        v1 = (v1 - v0) * textureScale + v0;

        //Front
        drawQuad(builder, pPoseStack, 2, 0, 12, 5, height, 12, u0, v0, u1, v1, pPackedLight);


        // Back
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        pPoseStack.translate(-0.4375f, 0, -1.875f);
        drawQuad(builder, pPoseStack, 2, 0, 15, 5, height, 15, u0, v0, u1, v1, pPackedLight);
        pPoseStack.popPose();

        //Side
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
        pPoseStack.translate(-1.6875f, 0, 0);
        drawQuad(builder, pPoseStack, 12, 0, 2, 15, height, 2, u0, v0, u1, v1, pPackedLight);
        pPoseStack.popPose();

        //Side other
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
        pPoseStack.translate(0, 0, -0.625f);
        drawQuad(builder, pPoseStack, 12, 0, 5, 15, height, 5, u0, v0, u1, v1, pPackedLight);
        pPoseStack.popPose();

    }

    private static void drawQuad(VertexConsumer builder, PoseStack poseStack,
                                 int x0, float y0, int z0,
                                 int x1, float y1, int z1,
                                 float u0, float v0, float u1,
                                 float v1, int packedLight) {

        drawVertex(builder, poseStack, x0/16f, y0/16f, z0/16f, u0, v0, 0, 1, 0, packedLight);
        drawVertex(builder, poseStack, x0/16f, y1/16f, z1/16f, u0, v1, 0, 1, 0, packedLight);
        drawVertex(builder, poseStack, x1/16f, y1/16f, z1/16f, u1, v1, 0, 1, 0, packedLight);
        drawVertex(builder, poseStack, x1/16f, y0/16f, z0/16f, u1, v0, 0, 1, 0, packedLight);

    }

    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, float n0, float n1, float n2, int packedLight) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(0xF73FFFE4)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(n0, n1, n2);
    }

    public static LayerDefinition createStandLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition stand = partdefinition.addOrReplaceChild("stand", CubeListBuilder.create()
                        .texOffs(14, 15).addBox(-1.0F, -3.1715F, 0.5F, 2.0F, 9.0F, 2.0F)
                        .texOffs(0, 0).addBox(-2.0F, 5.3285F, -3.501F, 4.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(8F, 6.1715F, 11.5F, 0, 0, Mth.PI));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createPowerLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("power_connection", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-4.0F, -12.0F, 7.999F, 8.0F, 8.0F, 0.0F)
                        .texOffs(16, 8).addBox(-3.0F, -11.0F, 6.997F, 6.0F, 6.0F, 1.0F)
                        .texOffs(6, 21).addBox(0.5F, -6.0F, 6.998F, 1.0F, 6.0F, 1.0F)
                        .texOffs(0, 0).addBox(-1.5F, -6.0F, 6.998F, 1.0F, 6.0F, 1.0F)
                        .texOffs(24, 15).addBox(-0.75F, -8.535F, 6.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(8, 0.0F, 8, 0, 0, Mth.PI));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    private static LayerDefinition createBatteryBase(BatteryType type) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(type.name, CubeListBuilder.create()
                        .texOffs(20, 23).addBox(-2.0F, -6.01F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.01f))
                        .texOffs(8, 30).addBox(-1.0F, -6.5F, 0.0F, 1.0F, 1.0F, 1.0F)
                        .texOffs(4, 30).addBox(-3.011F, -0.75F, -0.75F, 1.0F, 1.0F, 1.0F)
                        .texOffs(0, 30).addBox(-3.011F, -0.75F, 0.75F, 1.0F, 1.0F, 1.0F),
                type.offsetAndRot
        );

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public enum BatteryType {
        LEFT("battery_left", PartPose.offsetAndRotation(3.0F, 0.001F, 13.0F, 0, 0, Mth.PI)),
        RIGHT("battery_right", PartPose.offsetAndRotation(13.0F, 0.001F, 14.0F, 0, Mth.PI, Mth.PI));

        private final String name;
        private final PartPose offsetAndRot;
        private final ModelLayerLocation layerLocation;


        BatteryType(String name, PartPose offsetAndRot) {
            this.name = name;
            this.offsetAndRot = offsetAndRot;
            this.layerLocation = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name + "_layer"), "main");
        }

        public ModelLayerLocation getLayerLocation() {
            return layerLocation;
        }

        public LayerDefinition createLayerDefinition() {
            return createBatteryBase(this);
        }
    }

    private static class OptionalProfiler {
        private static final Consumer<String> PUSH_EMPTY = (s) -> {};
        private static final Runnable POP_EMPTY = () -> {};
        private final Runnable pop;
        private final Consumer<String> push;

        public OptionalProfiler(Level level) {
            if (level == null) {
                this.pop = POP_EMPTY;
                this.push = PUSH_EMPTY;
            } else {
                var profiler = level.getProfiler();
                this.push = profiler::push;
                this.pop = profiler::pop;
            }
        }

        private void push(String name) {
            push.accept(name);
        }

        private void pop() {
            pop.run();
        }
    }
}
