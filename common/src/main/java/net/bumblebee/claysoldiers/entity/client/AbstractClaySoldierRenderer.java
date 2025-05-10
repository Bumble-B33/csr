package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.client.accesories.AccessoryRenderLayer;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractClaySoldierRenderer extends HumanoidMobRenderer<AbstractClaySoldierEntity, ClaySoldierModel> {
    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    private final ItemInHandRenderer itemInHandRenderer;

    protected AbstractClaySoldierRenderer(EntityRendererProvider.Context pContext, ClaySoldierModel model) {
        super(pContext, model, 0.5f * SCALE, SCALE, SCALE, SCALE);
        this.layers.removeIf(layer -> layer.getClass() == CustomHeadLayer.class || layer.getClass() == ItemInHandLayer.class);

        this.itemInHandRenderer = pContext.getItemInHandRenderer();

        this.addLayer(new ClaySoldierArmorLayer(this,
                new ClaySoldierModel(pContext.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ClaySoldierModel(pContext.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                pContext.getModelManager(),
                pContext.getItemRenderer()
        ));
        this.addLayer(new AccessoryRenderLayer(this, pContext.getModelSet(), itemInHandRenderer, pContext.getItemRenderer()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, pContext.getEntityRenderDispatcher(), pContext.getItemRenderer()));
        this.addLayer(new WaxedRenderLayer<>(this));
        this.addLayer(new ClaySoldierItemInHandLayer(this, itemInHandRenderer));
    }

    @Override
    protected void scale(AbstractClaySoldierEntity claySoldier, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(SCALE, SCALE, SCALE);
        scaleExplode(claySoldier, poseStack, partialTickTime);
    }

    @Override
    public void render(AbstractClaySoldierEntity claySoldier, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(claySoldier, pPartialTicks);

        boolean shouldSit = claySoldier.isPassenger() && (claySoldier.getVehicle() != null && ClaySoldiersCommon.COMMON_HOOKS.shouldRiderSit(claySoldier.getVehicle()));
        this.model.riding = shouldSit;
        this.model.young = claySoldier.isBaby();
        float yBodyRot = Mth.rotLerp(pPartialTicks, claySoldier.yBodyRotO, claySoldier.yBodyRot);
        float yHeadRot = Mth.rotLerp(pPartialTicks, claySoldier.yHeadRotO, claySoldier.yHeadRot);
        float netHeadYaw = yHeadRot - yBodyRot;
        if (shouldSit && claySoldier.getVehicle() instanceof LivingEntity) {
            Entity vehicle = claySoldier.getVehicle();
            if (vehicle instanceof LivingEntity livingentity) {
                yBodyRot = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
                netHeadYaw = yHeadRot - yBodyRot;
                float f6 = Mth.wrapDegrees(netHeadYaw);
                if (f6 < -85.0F) {
                    f6 = -85.0F;
                }

                if (f6 >= 85.0F) {
                    f6 = 85.0F;
                }

                yBodyRot = yHeadRot - f6;
                if (f6 * f6 > 2500.0F) {
                    yBodyRot += f6 * 0.2F;
                }

                netHeadYaw = yHeadRot - yBodyRot;
            }
        }

        float headPitch = Mth.lerp(pPartialTicks, claySoldier.xRotO, claySoldier.getXRot());
        if (isEntityUpsideDown(claySoldier)) {
            headPitch *= -1.0F;
            netHeadYaw *= -1.0F;
        }
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);

        if (claySoldier.hasPose(Pose.SLEEPING)) {
            Direction direction = claySoldier.getBedOrientation();
            if (direction != null) {
                float f3 = claySoldier.getEyeHeight(Pose.STANDING) - 0.1F;
                pPoseStack.translate((float) (-direction.getStepX()) * f3, 0.0F, (float) (-direction.getStepZ()) * f3);
            }
        }

        float ageInTicks = this.getBob(claySoldier, pPartialTicks);
        float scale = claySoldier.getScale();
        pPoseStack.scale(scale, scale, scale);
        this.setupRotations(claySoldier, pPoseStack, ageInTicks, yBodyRot, pPartialTicks, 1f);
        pPoseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(claySoldier, pPoseStack, pPartialTicks);
        pPoseStack.translate(0.0F, -1.501F, 0.0F);
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && claySoldier.isAlive()) {
            limbSwingAmount = claySoldier.getWalkAnimation().speed(pPartialTicks);
            limbSwing = claySoldier.getWalkAnimation().position(pPartialTicks);
            if (claySoldier.isBaby()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }

        this.model.prepareMobModel(claySoldier, limbSwing, limbSwingAmount, pPartialTicks);
        this.model.setupAnim(claySoldier, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (claySoldier.getRidingPose() == AbstractClaySoldierEntity.RidingPose.FIREWORK) {
            pPoseStack.translate(0, 0.5, 0);
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(claySoldier);
        boolean isInvisible = !bodyVisible;
        boolean shouldGlow = minecraft.shouldEntityAppearGlowing(claySoldier);
        RenderType rendertype = this.getRenderType(claySoldier, bodyVisible, isInvisible, shouldGlow);

        if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            int overlay = getOverlayCoords(claySoldier, this.getWhiteOverlayProgress(claySoldier, pPartialTicks));
            ClayMobTeam variant = getVariantForColor(claySoldier);
            renderModel(claySoldier, pPoseStack, vertexconsumer, pPackedLight, overlay, variant.getColor(claySoldier, pPartialTicks), isInvisible ? 0x26 : 0xFF);
        }

        for (RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel> renderlayer : this.layers) {
            renderlayer.render(pPoseStack, pBuffer, pPackedLight, claySoldier, limbSwing, limbSwingAmount, pPartialTicks, ageInTicks, netHeadYaw, headPitch);
        }
        renderCarried(claySoldier, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.popPose();
        if (shouldShowName(claySoldier)) {
            renderNameTag(claySoldier, claySoldier.getDisplayName(), pPoseStack, pBuffer, pPackedLight, pPartialTicks);
        }

    }

    /**
     * Renders the ClaySoldierModel
     */
    protected void renderModel(AbstractClaySoldierEntity soldier, PoseStack pPoseStack, VertexConsumer vertexConsumer, int pPackedLight, int overlayCords, int color, int alpha) {
        this.model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, overlayCords, FastColor.ARGB32.color(alpha, color));
    }

    /**
     * Returns the {@code ClayMobTeam} used for coloring the model.
     */
    protected ClayMobTeam getVariantForColor(AbstractClaySoldierEntity claySoldier) {
        return claySoldier.getClayTeam();
    }

    private void renderCarried(AbstractClaySoldierEntity soldier, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (!soldier.getCarriedStack().isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0, -0.55, 0);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(135F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

            pPoseStack.scale(1.5f, 1.5f, 1.5f);
            itemInHandRenderer.renderItem(soldier, soldier.getCarriedStack(), ItemDisplayContext.FIXED, false, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();

        }
    }

    private void scaleExplode(AbstractClaySoldierEntity claySoldier, PoseStack pPoseStack, float pPartialTickTime) {
        float swelling = claySoldier.getSwelling(pPartialTickTime);
        float f1 = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float f2 = (1.0F + swelling * 0.4F) * f1;
        float f3 = (1.0F + swelling * 0.1F) / f1;
        pPoseStack.scale(f2, f3, f2);
    }

    @Override
    protected float getWhiteOverlayProgress(AbstractClaySoldierEntity claySoldier, float pPartialTicks) {
        float swelling = claySoldier.getSwelling(pPartialTicks);
        return (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
    }

    @Override
    protected int getBlockLightLevel(AbstractClaySoldierEntity pEntity, BlockPos pPos) {
        return pEntity.allProperties().isGlowing() ? 15 : super.getBlockLightLevel(pEntity, pPos);
    }

    @Override
    protected boolean isBodyVisible(AbstractClaySoldierEntity livingEntity) {
        return super.isBodyVisible(livingEntity) && !livingEntity.allProperties().isInvisible();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    private static class ClaySoldierItemInHandLayer extends ItemInHandLayer<AbstractClaySoldierEntity, ClaySoldierModel> {
        public ClaySoldierItemInHandLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> renderer, ItemInHandRenderer itemInHandRenderer) {
            super(renderer, itemInHandRenderer);
        }

        @Override
        protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            if (arm == HumanoidArm.LEFT && ((AbstractClaySoldierEntity) livingEntity).handsOccupied(SoldierEquipmentSlot.OFFHAND)) {
                return;
            }
            if (arm == HumanoidArm.RIGHT && ((AbstractClaySoldierEntity) livingEntity).handsOccupied(SoldierEquipmentSlot.MAINHAND)) {
                return;
            }

            super.renderArmWithItem(livingEntity, itemStack, displayContext, arm, poseStack, buffer, packedLight);
        }
    }
}
