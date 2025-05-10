package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

public class ClaySoldierModel extends HumanoidModel<AbstractClaySoldierEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "main");
    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    protected static final CubeDeformation SHRINK_DEFORMATION = new CubeDeformation(SCALE, SCALE, SCALE);
    @Nullable
    public final ModelPart bambooStick;

    public ClaySoldierModel(ModelPart pRoot) {
        super(pRoot);
        if (pRoot.hasChild("bamboo_stick")) {
            this.bambooStick = pRoot.getChild("bamboo_stick");
        } else {
            this.bambooStick = null;
        }
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createSoldierMesh(SHRINK_DEFORMATION, 0), 64, 64);
    }

    private static MeshDefinition createSoldierMesh(CubeDeformation cubeDeformation, float pYOffset) {
        MeshDefinition meshDefinition = createMesh(cubeDeformation, pYOffset);
        PartDefinition partdefinition = meshDefinition.getRoot();
        partdefinition.addOrReplaceChild("bamboo_stick",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -16.0F, -6.0F, 2.0F, 12.0F, 2.0F, cubeDeformation, 0.25f, 0.25f), PartPose.offset(0.0F, pYOffset, 0.0F));

        return meshDefinition;
    }

    @Override
    public void setupAnim(AbstractClaySoldierEntity claySoldier, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(claySoldier, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

        if (claySoldier.isZombie()) {
            AnimationUtils.animateZombieArms(leftArm, rightArm, claySoldier.isAggressive(), this.attackTime, pAgeInTicks);
        }
        animateArms(claySoldier);

        setUpRidingPose(claySoldier);
        setSittingPose(claySoldier);

        if (bambooStick != null) {
            this.bambooStick.copyFrom(this.getHead());
        }
    }

    private void animateArms(AbstractClaySoldierEntity claySoldier) {
        boolean isRightHanded = claySoldier.getMainArm() == HumanoidArm.RIGHT;
        boolean right = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND) : claySoldier.hasShieldInHand(InteractionHand.OFF_HAND);
        boolean left = isRightHanded ? claySoldier.hasShieldInHand(InteractionHand.OFF_HAND) : claySoldier.hasShieldInHand(InteractionHand.MAIN_HAND);
        if (right) {
            this.rightArm.xRot = -Mth.PI / (2.45F);
            this.rightArm.yRot = -0.5f;
        }
        if (left) {
            this.leftArm.xRot = -Mth.PI / (2.45F);
            this.leftArm.yRot = 0.5f;
        }
        if (claySoldier.isFallingWithGlider() || !claySoldier.getCarriedStack().isEmpty()) {
            this.leftArm.xRot = -Mth.PI;
            this.rightArm.xRot = -Mth.PI;
            this.rightArm.yRot = 0;
            this.leftArm.yRot = 0;
        }
    }

    public void renderBambooStick(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay) {
        if (bambooStick == null) {
            return;
        }
        this.bambooStick.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void setUpRidingPose(AbstractClaySoldierEntity claySoldier) {
        if (claySoldier.getRidingPose() == AbstractClaySoldierEntity.RidingPose.RABBIT) {
            setRabbitRidingPose();
        } else {
            body.z = 0;
            head.z = 0;
        }

    }

    private void setRabbitRidingPose() {
        body.y = 15;
        body.z = 2;
        body.xRot = (float) (Math.PI / 3f);
        head.y = 15;
        head.z = 2;
        leftArm.y = 16;
        rightArm.y = 16;
        leftArm.z = 3.5f;
        rightArm.z = 3.5f;

        leftLeg.y = 21;
        rightLeg.y = 21;
        leftLeg.z = 12;
        rightLeg.z = 12;

        leftLeg.xRot = (float) -Math.PI * 1.7f;
        rightLeg.xRot = (float) -Math.PI * 1.7f;

        leftLeg.yRot = 0.2f;
        rightLeg.yRot = -0.2f;
    }

    private void setSittingPose(AbstractClaySoldierEntity claySoldier) {
        if (claySoldier.isInSittingPose() && !claySoldier.isPassenger()) {
            if (claySoldier.getId() % 2 == 0) {
                sittingPose1();
            } else {
                sittingPose2();
            }
        }
    }
    private void sittingPose1() {
        head.y = 7.5f;
        leftArm.y = 9.5f;
        rightArm.y = 9.5f;
        body.y = 7.5f;

        rightLeg.y = 22;
        leftLeg.y = 22;
        rightLeg.z = 1.8f;
        leftLeg.z = 1.8f;
        setLegSittingRot();
    }
    private void sittingPose2() {
        head.y = 11f;
        head.z = 5f;
        head.x = 0.01f;
        leftArm.y = 13.5f;
        rightArm.y = 13.5f;
        leftArm.z = 5f;
        rightArm.z = 5f;
        leftArm.xRot = 0;
        leftArm.yRot = 0;
        leftArm.zRot = 0;
        rightArm.xRot = 0;
        rightArm.yRot = 0;
        rightArm.zRot = 0;

        body.y = 11f;
        body.xRot = -0.6f;
        body.z = 5.5f;

        rightLeg.y = 22;
        leftLeg.y = 22;
        rightLeg.z = 1.3f;
        leftLeg.z = 1.3f;
        setLegSittingRot();

    }
    private void setLegSittingRot() {
        rightLeg.yRot = (float) (Math.PI / 10);
        leftLeg.yRot = (float) (-Math.PI / 10);
        rightLeg.xRot = -1.4137167F;
        leftLeg.xRot = -1.4137167F;
    }
}