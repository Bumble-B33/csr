package net.bumblebee.claysoldiers.entity.client.wraith;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.client.ClayMobStatusRenderlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class WraithRenderer extends MobRenderer<ClayWraithEntity, WraithModel> {
    private static final ResourceLocation WRAITH_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_wraith/wraith.png");
    private static final ResourceLocation WRAITH_CHARGING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png");
    private static final float SCALE = ClayWraithEntity.WRAITH_SCALE;

    public WraithRenderer(EntityRendererProvider.Context context) {
        super(context, new WraithModel(context.bakeLayer(WraithModel.LAYER_LOCATION)), 0.3f * SCALE);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, context.getEntityRenderDispatcher(), context.getItemRenderer()));

    }

    @Override
    protected void scale(ClayWraithEntity pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        pPoseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(ClayWraithEntity pEntity) {
        return WRAITH_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(ClayWraithEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public void render(ClayWraithEntity wraith, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(wraith, pPartialTicks);

        boolean shouldSit = wraith.isPassenger() && (wraith.getVehicle() != null && ClaySoldiersCommon.COMMON_HOOKS.shouldRiderSit(wraith.getVehicle()));
        this.model.riding = shouldSit;
        this.model.young = wraith.isBaby();
        float bodyRot = Mth.rotLerp(pPartialTicks, wraith.yBodyRotO, wraith.yBodyRot);
        float f1 = Mth.rotLerp(pPartialTicks, wraith.yHeadRotO, wraith.yHeadRot);
        float f2 = f1 - bodyRot;
        if (shouldSit && wraith.getVehicle() instanceof LivingEntity) {
            Entity vehicle = wraith.getVehicle();
            if (vehicle instanceof LivingEntity livingentity) {
                bodyRot = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
                f2 = f1 - bodyRot;
                float f6 = Mth.wrapDegrees(f2);
                if (f6 < -85.0F) {
                    f6 = -85.0F;
                }

                if (f6 >= 85.0F) {
                    f6 = 85.0F;
                }

                bodyRot = f1 - f6;
                if (f6 * f6 > 2500.0F) {
                    bodyRot += f6 * 0.2F;
                }

                f2 = f1 - bodyRot;
            }
        }

        float f5 = Mth.lerp(pPartialTicks, wraith.xRotO, wraith.getXRot());
        if (isEntityUpsideDown(wraith)) {
            f5 *= -1.0F;
            f2 *= -1.0F;
        }

        if (wraith.hasPose(Pose.SLEEPING)) {
            Direction direction = wraith.getBedOrientation();
            if (direction != null) {
                float f3 = wraith.getEyeHeight(Pose.STANDING) - 0.1F;
                pPoseStack.translate((float) (-direction.getStepX()) * f3, 0.0F, (float) (-direction.getStepZ()) * f3);
            }
        }

        float f7 = this.getBob(wraith, pPartialTicks);
        float scale = wraith.getScale();
        pPoseStack.scale(scale, scale, scale);
        this.setupRotations(wraith, pPoseStack, f7, bodyRot, pPartialTicks, 1f);
        pPoseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(wraith, pPoseStack, pPartialTicks);
        pPoseStack.translate(0.0F, -1.501F, 0.0F);
        float f8 = 0.0F;
        float f4 = 0.0F;
        if (!shouldSit && wraith.isAlive()) {
            f8 = wraith.walkAnimation.speed(pPartialTicks);
            f4 = wraith.walkAnimation.position(pPartialTicks);
            if (wraith.isBaby()) {
                f4 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.model.prepareMobModel(wraith, f4, f8, pPartialTicks);
        this.model.setupAnim(wraith, f4, f8, f7, f2, f5);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(wraith);
        boolean isInvisible = !bodyVisible;
        boolean shouldGlow = minecraft.shouldEntityAppearGlowing(wraith);
        RenderType rendertype = this.getRenderType(wraith, bodyVisible, isInvisible, shouldGlow);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            int overlay = getOverlayCoords(wraith, this.getWhiteOverlayProgress(wraith, pPartialTicks));
            this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, overlay, FastColor.ARGB32.color(getAlpha(wraith), wraith.getClayTeam().getColor(wraith, pPartialTicks)));
        }

        for (RenderLayer<ClayWraithEntity, WraithModel> renderlayer : this.layers) {
            renderlayer.render(pPoseStack, pBuffer, pPackedLight, wraith, f4, f8, pPartialTicks, f7, f2, f5);
        }

        pPoseStack.popPose();
    }
    private int getAlpha(ClayWraithEntity wraith) {
        return Math.max(0, (wraith.getLifePoint() * 3) - 10);
    }
}
