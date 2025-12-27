package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.client.accesories.AccessoryRenderLayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;

public abstract class AbstractClaySoldierRenderer extends HumanoidMobRenderer<AbstractClaySoldierEntity, AbstractClaySoldierRenderState, ClaySoldierModel> {
    public static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;

    protected AbstractClaySoldierRenderer(EntityRendererProvider.Context pContext, ClaySoldierModel model) {
        super(pContext, model, model, 0.5f * SCALE, new CustomHeadLayer.Transforms(SCALE, SCALE, SCALE));
        this.layers.removeIf(layer -> layer.getClass() == CustomHeadLayer.class || layer.getClass() == ItemInHandLayer.class);


        this.addLayer(new ClaySoldierArmorLayer(this,
                new ClaySoldierModel(pContext.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ClaySoldierModel(pContext.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                pContext.getModelManager(),
                pContext.getEquipmentAssets()
        ));
        this.addLayer(new AccessoryRenderLayer(this, pContext.getModelSet(), pContext.getEquipmentAssets()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, pContext.getEntityRenderDispatcher(), e -> e.isInSittingPose, s -> s.shouldShowStatus, s -> s.workStatus, s -> s.statusAttachmentPoint));
        this.addLayer(new WaxedRenderLayer<>(this));
        this.addLayer(new ClaySoldierItemInHandLayer(this));
    }

    @Override
    protected void scale(AbstractClaySoldierRenderState renderState, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        scaleExplode(renderState, poseStack);
    }

    @Override
    public void render(AbstractClaySoldierRenderState claySoldier, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        if (claySoldier.hasPose(Pose.SLEEPING)) {
            Direction direction = claySoldier.bedOrientation;
            if (direction != null) {
                float f3 = claySoldier.eyeHeight - 0.1F;
                pPoseStack.translate((float) (-direction.getStepX()) * f3, 0.0F, (float) (-direction.getStepZ()) * f3);
            }
        }

        float scale = claySoldier.scale;
        pPoseStack.scale(scale, scale, scale);
        this.setupRotations(claySoldier, pPoseStack, claySoldier.bodyRot, 1f);
        pPoseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(claySoldier, pPoseStack);
        pPoseStack.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(claySoldier);

        if (claySoldier.ridingPose == AbstractClaySoldierEntity.RidingPose.FIREWORK) {
            pPoseStack.translate(0, 0.5, 0);
        }

        boolean bodyVisible = this.isBodyVisible(claySoldier);
        boolean isInvisible = !bodyVisible;
        RenderType rendertype = this.getRenderType(claySoldier, bodyVisible, isInvisible, claySoldier.appearsGlowing);

        if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            int overlay = getOverlayCoords(claySoldier, this.getWhiteOverlayProgress(claySoldier));
            int color = getVariantForColor(claySoldier);
            if (claySoldier.veryAngry) {
                color = shiftColorAngry(color);
            }
            renderModel(claySoldier, pPoseStack, vertexconsumer, pPackedLight, overlay, color, isInvisible ? 0x26 : 0xFF);
        }

        if (shouldRenderLayers(claySoldier)) {
            for (RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> renderlayer : this.layers) {
                renderlayer.render(pPoseStack, pBuffer, pPackedLight, claySoldier, claySoldier.yRot, claySoldier.xRot);
            }
        }

        renderCarried(claySoldier, pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);

        pPoseStack.popPose();

        if (claySoldier.nameTag != null) {
            this.renderNameTag(claySoldier, claySoldier.nameTag, pPoseStack, pBuffer, pPackedLight);
        }
    }

    @Override
    public AbstractClaySoldierRenderState createRenderState() {
        return new AbstractClaySoldierRenderState();
    }

    @Override
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        ClayMobRenderState.extractClayMobRenderState(claySoldierEntity, claySoldierRenderState, partialTick);

        claySoldierRenderState.isZombie = claySoldierEntity.isZombie();
        claySoldierRenderState.isAggressive = claySoldierEntity.isAggressive();
        claySoldierRenderState.hasShieldInOffhand = claySoldierEntity.hasShieldInHand(InteractionHand.OFF_HAND);
        claySoldierRenderState.hasShieldInMainHand = claySoldierEntity.hasShieldInHand(InteractionHand.MAIN_HAND);

        claySoldierRenderState.carriedItemStack = claySoldierEntity.getCarriedStack();
        this.itemModelResolver.updateForLiving(claySoldierRenderState.carriedItemRenderState, claySoldierRenderState.carriedItemStack, ItemDisplayContext.FIXED, false, claySoldierEntity);

        claySoldierRenderState.ridingPose = claySoldierEntity.getRidingPose();
        claySoldierRenderState.id = claySoldierEntity.getId();
        claySoldierRenderState.skinVariantId = claySoldierEntity.getSkinVariant();

        claySoldierRenderState.offhandOccupied = claySoldierEntity.handsOccupied(SoldierEquipmentSlot.OFFHAND);
        claySoldierRenderState.mainhandOccupied = claySoldierEntity.handsOccupied(SoldierEquipmentSlot.MAINHAND);

        claySoldierRenderState.isAlive = claySoldierEntity.isAlive();
        claySoldierRenderState.isFalling = claySoldierEntity.isFalling();

        claySoldierRenderState.swelling = claySoldierEntity.getSwelling(partialTick);

        claySoldierRenderState.offsetColor = claySoldierEntity.getOffsetColor().getColor(claySoldierEntity, partialTick);

        claySoldierRenderState.allProperties = claySoldierEntity.allProperties();

        claySoldierRenderState.gliderSlot = claySoldierEntity.gliderSlot();
        claySoldierRenderState.isFallingWithGlider = claySoldierEntity.isFallingWithGlider();
        claySoldierRenderState.veryAngry = claySoldierEntity.isVeryAngry();

        claySoldierRenderState.setUpInventory(claySoldierEntity);
        claySoldierRenderState.fallFlyingTimeInTicks = (float)claySoldierEntity.getFallFlyingTicks() + partialTick;
        AbstractClaySoldierRenderState.extractCloakState(claySoldierEntity, claySoldierRenderState, partialTick);
        AbstractClaySoldierRenderState.extractAccessoryRenderState(claySoldierEntity, claySoldierRenderState, itemModelResolver);
    }

    /**
     * Renders the ClaySoldierModel
     */
    protected void renderModel(AbstractClaySoldierRenderState soldier, PoseStack pPoseStack, VertexConsumer vertexConsumer, int pPackedLight, int overlayCords, int color, int alpha) {
        this.model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, overlayCords, ARGB.color(alpha, color));
    }

    /**
     * Returns the {@code ClayMobTeam} used for coloring the model.
     */
    protected int getVariantForColor(AbstractClaySoldierRenderState claySoldier) {
        return claySoldier.clayTeamColor;
    }

    private void renderCarried(AbstractClaySoldierRenderState soldier, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverleay) {
        if (!soldier.carriedItemRenderState.isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0, -0.55, 0);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(135F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

            pPoseStack.scale(1.5f, 1.5f, 1.5f);
            soldier.carriedItemRenderState.render(pPoseStack, pBuffer, pPackedLight, pPackedOverleay);
            pPoseStack.popPose();

        }
    }

    private void scaleExplode(AbstractClaySoldierRenderState claySoldier, PoseStack pPoseStack) {
        float swelling = claySoldier.swelling;
        float f1 = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float f2 = (1.0F + swelling * 0.4F) * f1;
        float f3 = (1.0F + swelling * 0.1F) / f1;
        pPoseStack.scale(f2, f3, f2);
    }

    @Override
    protected float getWhiteOverlayProgress(AbstractClaySoldierRenderState claySoldier) {
        float swelling = claySoldier.swelling;
        return (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
    }

    @Override
    protected int getBlockLightLevel(AbstractClaySoldierEntity pEntity, BlockPos pPos) {
        return pEntity.allProperties().isGlowing() ? 15 : super.getBlockLightLevel(pEntity, pPos);
    }

    @Override
    protected boolean isBodyVisible(AbstractClaySoldierRenderState livingEntity) {
        return super.isBodyVisible(livingEntity) && !livingEntity.allProperties.isInvisible();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public static int shiftColorAngry(int color) {
        int red = Math.clamp((color >> 16 & 255) + 25, 0, 255);
        int green = Math.clamp((color >> 8 & 255) - 0, 0, 255);
        int blue = Math.clamp((color & 255) - 0, 0, 255);

        return (red << 16) | (green << 8) | blue;
    }

    private static class ClaySoldierItemInHandLayer extends ItemInHandLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
        public ClaySoldierItemInHandLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer) {
            super(renderer);
        }

        @Override
        protected void renderArmWithItem(AbstractClaySoldierRenderState renderState, ItemStackRenderState itemStackRenderState, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            if (arm == HumanoidArm.LEFT && renderState.offhandOccupied) {
                return;
            }
            if (arm == HumanoidArm.RIGHT && renderState.mainhandOccupied) {
                return;
            }
            super.renderArmWithItem(renderState, itemStackRenderState, arm, poseStack, bufferSource, packedLight);
        }


    }
}
