package net.bumblebee.claysoldiers.entity.client;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class ClaySoldierModel extends HumanoidModel<AbstractClaySoldierRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "main");
    public static final ModelLayerLocation HELMET_LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "helmet");
    public static final ModelLayerLocation CHESTPLATE_LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "chestplate");
    public static final ModelLayerLocation LEGGINGS_LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "leggings");
    public static final ModelLayerLocation BOOTS_LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), "boots");

    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    protected static final CubeDeformation SHRINK_DEFORMATION = new CubeDeformation(SCALE, SCALE, SCALE);

    public ClaySoldierModel(ModelPart pRoot) {
        super(pRoot);
    }

    public static LayerDefinition createSoldierLayer() {
        return LayerDefinition.create(createSoldierMesh(SHRINK_DEFORMATION, 0), 64, 64);
    }

    protected static MeshDefinition createSoldierMesh(CubeDeformation cubeDeformation, float pYOffset) {
        return createMesh(cubeDeformation, pYOffset);
    }


    @Override
    public void setupAnim(AbstractClaySoldierRenderState claySoldier) {
        super.setupAnim(claySoldier);

        if (claySoldier.isZombie) {
            AnimationUtils.animateZombieArms(leftArm, rightArm, claySoldier.isAggressive, claySoldier.attackTime, claySoldier.ageInTicks);
        }
        animateArms(claySoldier);

        setUpRidingPose(claySoldier);
        setSittingPose(claySoldier);
    }

    private void animateArms(AbstractClaySoldierRenderState claySoldier) {
        boolean isRightHanded = claySoldier.mainArm == HumanoidArm.RIGHT;
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
        if (claySoldier.isFallingWithGlider || !claySoldier.carriedItemStack.isEmpty()) {
            this.leftArm.xRot = -Mth.PI;
            this.rightArm.xRot = -Mth.PI;
            this.rightArm.yRot = 0;
            this.leftArm.yRot = 0;
        }
    }

    private void setUpRidingPose(AbstractClaySoldierRenderState claySoldier) {
        if (claySoldier.ridingPose == AbstractClaySoldierEntity.RidingPose.RABBIT) {
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

    private void setSittingPose(AbstractClaySoldierRenderState claySoldier) {
        if (claySoldier.isInSittingPose && !claySoldier.isPassenger) {
            if (claySoldier.id % 2 == 0) {
                sittingPose1();
            } else {
                sittingPose2();
            }
        }
    }
    public void sittingPose1() {
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