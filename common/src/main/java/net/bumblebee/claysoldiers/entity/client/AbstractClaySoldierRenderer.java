package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bumblebee.claysoldiers.datamap.HoldingPose;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.client.accesories.AccessoryRenderLayer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayMobRenderState;
import net.bumblebee.claysoldiers.entity.common.VampiricClayMob;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClayMobAccess;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractClaySoldierRenderer extends HumanoidMobRenderer<AbstractClaySoldierEntity, AbstractClaySoldierRenderState, ClaySoldierModel> {
    public static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;

    protected AbstractClaySoldierRenderer(EntityRendererProvider.Context context, ClaySoldierModel model) {
        super(context, model, model, 0.5f * SCALE, new CustomHeadLayer.Transforms(SCALE, SCALE, SCALE));
        this.layers.removeIf(layer -> layer.getClass() == CustomHeadLayer.class || layer.getClass() == ItemInHandLayer.class);
        this.addLayer(new ClaySoldierArmorLayer(this,
                ArmorModelSet.bake(new ArmorModelSet<>(
                        ClaySoldierModel.HELMET_LAYER_LOCATION,
                        ClaySoldierModel.CHESTPLATE_LAYER_LOCATION,
                        ClaySoldierModel.LEGGINGS_LAYER_LOCATION,
                        ClaySoldierModel.BOOTS_LAYER_LOCATION
                ), context.getModelSet(), ClaySoldierModel::new),
                context.getAtlas(AtlasIds.ARMOR_TRIMS),
                context.getEquipmentAssets()
        ));
        this.addLayer(SlimeRootLayer.ofSoldier(this, context.getItemModelResolver()));
        this.addLayer(new AccessoryRenderLayer(this, context.getModelSet(), context.getEquipmentAssets(), context.getPlayerSkinRenderCache()));
        this.addLayer(new ClayMobStatusRenderlayer<>(this, e -> e.isInSittingPose, s -> s.shouldShowStatus, s -> s.workStatus, s -> s.statusAttachmentPoint));
        this.addLayer(new WaxedRenderLayer<>(this));
        this.addLayer(new ClaySoldierItemInHandLayer(this));
    }

    @Override
    protected void scale(AbstractClaySoldierRenderState renderState, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        scaleExplode(renderState, poseStack);
    }

    @Override
    public void submit(AbstractClaySoldierRenderState claySoldier, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        if (claySoldier.ridingPose == AbstractClaySoldierEntity.RidingPose.FIREWORK) {
            poseStack.translate(0, -0.33, 0);
        }
        poseStack.pushPose();
        if (claySoldier.hasPose(Pose.SLEEPING)) {
            Direction direction = claySoldier.bedOrientation;
            if (direction != null) {
                float f3 = claySoldier.eyeHeight - 0.1F;
                poseStack.translate((float) (-direction.getStepX()) * f3, 0.0F, (float) (-direction.getStepZ()) * f3);
            }
        }

        float scale = claySoldier.scale;
        poseStack.scale(scale, scale, scale);
        this.setupRotations(claySoldier, poseStack, claySoldier.bodyRot, 1f);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(claySoldier, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);


        boolean bodyVisible = this.isBodyVisible(claySoldier);
        boolean isInvisible = !bodyVisible;
        RenderType rendertype = this.getRenderType(claySoldier, bodyVisible, isInvisible, claySoldier.appearsGlowing());

        if (rendertype != null) {
            int overlay = getOverlayCoords(claySoldier, this.getWhiteOverlayProgress(claySoldier));
            int color = getColor(claySoldier);
            nodeCollector.submitModel(this.model, claySoldier, poseStack, rendertype, claySoldier.lightCoords, overlay, ARGB.color(isInvisible ? 0x26 : 0xFF, color), null, claySoldier.outlineColor, null);
        }

        if (shouldRenderLayers(claySoldier) && !this.layers.isEmpty()) {
            this.model.setupAnim(claySoldier);

            for (RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> renderlayer : this.layers) {
                renderlayer.submit(poseStack, nodeCollector, claySoldier.lightCoords, claySoldier, claySoldier.yRot, claySoldier.xRot);
            }
        }

        submitCarried(claySoldier, poseStack, nodeCollector, claySoldier.lightCoords, OverlayTexture.NO_OVERLAY);
        submitSpecialHoldingPose(claySoldier, poseStack, nodeCollector, claySoldier.lightCoords, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        poseStack.popPose();

        if (claySoldier.nameTag != null) {
            submitNameDisplay(claySoldier, poseStack, nodeCollector, cameraRenderState);
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

        claySoldierRenderState.disableOffhandRender = claySoldierEntity.handsOccupied(SoldierEquipmentSlot.OFFHAND);
        claySoldierRenderState.disableMainHandRender = claySoldierEntity.handsOccupied(SoldierEquipmentSlot.MAINHAND);

        claySoldierRenderState.ridingPose = claySoldierEntity.getRidingPose();
        claySoldierRenderState.id = claySoldierEntity.getId();
        claySoldierRenderState.skinVariantId = claySoldierEntity.getSkinVariant();


        claySoldierRenderState.isAlive = claySoldierEntity.isAlive();
        claySoldierRenderState.isFalling = claySoldierEntity.isFalling();

        claySoldierRenderState.swelling = claySoldierEntity.getSwelling(partialTick);

        claySoldierRenderState.offsetColor = claySoldierEntity.getOffsetColor().getColor(claySoldierEntity, partialTick);
        claySoldierRenderState.hasOffsetColor = claySoldierEntity.hasOffsetColor();

        claySoldierRenderState.allProperties = claySoldierEntity.allProperties();

        claySoldierRenderState.gliderSlot = claySoldierEntity.gliderSlot();
        claySoldierRenderState.isFallingWithGlider = claySoldierEntity.isFallingWithGlider();
        claySoldierRenderState.veryAngry = claySoldierEntity.isVeryAngry();

        claySoldierRenderState.setUpInventory(claySoldierEntity);
        claySoldierRenderState.fallFlyingTimeInTicks = (float) claySoldierEntity.getFallFlyingTicks() + partialTick;

        this.itemModelResolver.updateForLiving(claySoldierRenderState.carriedItemRenderState, claySoldierEntity.getCarriedStack(), ItemDisplayContext.FIXED, claySoldierEntity);
        claySoldierRenderState.renderCarried = true;


        ItemStackWithEffect twoHandedItem = ItemStackWithEffect.EMPTY;

        if (claySoldierEntity.getItemBySlot(SoldierEquipmentSlot.MAINHAND).getHoldingPose() == HoldingPose.TWO_HANDED_BLOCK) {
            twoHandedItem = claySoldierEntity.getItemBySlot(SoldierEquipmentSlot.MAINHAND);
            claySoldierRenderState.disableMainHandRender = true;
            claySoldierRenderState.disableOffhandRender = true;
        } else if (claySoldierEntity.getItemBySlot(SoldierEquipmentSlot.OFFHAND).getHoldingPose() == HoldingPose.TWO_HANDED_BLOCK) {
            twoHandedItem = claySoldierEntity.getItemBySlot(SoldierEquipmentSlot.OFFHAND);
            claySoldierRenderState.disableMainHandRender = true;
            claySoldierRenderState.disableOffhandRender = true;
        }

        this.itemModelResolver.updateForLiving(claySoldierRenderState.twoHandedItem, twoHandedItem.stack(), ItemDisplayContext.FIXED, claySoldierEntity);


        AbstractClaySoldierRenderState.extractCloakState(claySoldierEntity, claySoldierRenderState, partialTick);
        AbstractClaySoldierRenderState.extractAccessoryRenderState(claySoldierEntity, claySoldierRenderState, itemModelResolver);

        if (claySoldierEntity instanceof ProgrammableClayMobAccess programmable) {
            var chip = programmable.getInstalledChip();
            if (chip != null) {
                claySoldierRenderState.moduleTexture = chip.assetId();
            }
        }
        if (claySoldierEntity instanceof VampiricClayMob vampire) {
            claySoldierRenderState.isNightForVampire = vampire.isNightForVampire();
        }
    }

    /**
     * Returns the Color of the Clay Soldier. Alpha will be overwritten.
     */
    protected int getColor(AbstractClaySoldierRenderState soldier) {
        int color = getVariantForColor(soldier);
        if (soldier.veryAngry) {
            color = shiftColorAngry(color);
        }
        return color;
    }

    /**
     * Returns the {@code ClayMobTeam} used for coloring the model.
     */
    protected int getVariantForColor(AbstractClaySoldierRenderState claySoldier) {
        return claySoldier.clayTeamColor;
    }

    private void submitCarried(AbstractClaySoldierRenderState soldier, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, int pPackedOverleay) {
        if (!soldier.renderCarried || soldier.carriedItemRenderState.isEmpty()) {
            return;
        }

        pPoseStack.pushPose();
        pPoseStack.translate(0, -0.55, 0);

        pPoseStack.mulPose(Axis.YP.rotationDegrees(135F));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

        pPoseStack.scale(1.5f, 1.5f, 1.5f);

        soldier.carriedItemRenderState.submit(pPoseStack, nodeCollector, pPackedLight, pPackedOverleay, 0);
        pPoseStack.popPose();
    }

    private void submitSpecialHoldingPose(AbstractClaySoldierRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay) {
        if (state.twoHandedItem.isEmpty()) {
            return;
        }
        poseStack.pushPose();

        this.model.translateToHand(state, HumanoidArm.RIGHT, poseStack);

        poseStack.mulPose(Axis.ZP.rotation(ClaySoldierModel.TWO_HANDED_Y_ROT));

        poseStack.translate(0.3f, 0.7f, 0f);


        state.twoHandedItem.submit(poseStack, nodeCollector, packedLight, packedOverlay, state.outlineColor);

        poseStack.popPose();
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
        protected void submitArmWithItem(AbstractClaySoldierRenderState renderState, ItemStackRenderState stackRenderState, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight) {
            if (arm == HumanoidArm.LEFT && renderState.disableOffhandRender) {
                return;
            }
            if (arm == HumanoidArm.RIGHT && renderState.disableMainHandRender) {
                return;
            }

            super.submitArmWithItem(renderState, stackRenderState, itemStack, arm, poseStack, nodeCollector, packedLight);
        }
    }
}
