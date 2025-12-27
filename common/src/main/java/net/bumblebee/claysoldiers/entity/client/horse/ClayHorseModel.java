package net.bumblebee.claysoldiers.entity.client.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ClayHorseModel extends AbstractEquineModel<ClayHorseRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse"), "main");

    private static final float DEG_30 = Mth.PI / 6;
    private static final float DEG_90 = Mth.PI / 2;
    protected static final String HEAD_PARTS = "head_parts";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public ClayHorseModel(ModelPart pRoot) {
        super(pRoot);
        this.body = pRoot.getChild("body");
        this.headParts = pRoot.getChild(HEAD_PARTS);
        this.rightHindLeg = pRoot.getChild("right_hind_leg");
        this.leftHindLeg = pRoot.getChild("left_hind_leg");
        this.rightFrontLeg = pRoot.getChild("right_front_leg");
        this.leftFrontLeg = pRoot.getChild("left_front_leg");
        ModelPart modelpart = this.body.getChild(SADDLE);
        ModelPart modelpart1 = this.headParts.getChild(LEFT_SADDLE_MOUTH);
        ModelPart modelpart2 = this.headParts.getChild(RIGHT_SADDLE_MOUTH);
        ModelPart modelpart3 = this.headParts.getChild(LEFT_SADDLE_LINE);
        ModelPart modelpart4 = this.headParts.getChild(RIGHT_SADDLE_LINE);
        ModelPart modelpart5 = this.headParts.getChild(HEAD_SADDLE);
        ModelPart modelpart6 = this.headParts.getChild(MOUTH_SADDLE_WRAP);
        this.saddleParts = new ModelPart[]{modelpart, modelpart1, modelpart2, modelpart5, modelpart6};
        this.ridingParts = new ModelPart[]{modelpart3, modelpart4};
    }

    public static LayerDefinition createLayerDefinition() {
        return LayerDefinition.create(createClayHorseMesh(new CubeDeformation(ClayHorseEntity.SCALE)), 64, 64);
    }
    public static LayerDefinition createLayerArmorDefinition() {
        return LayerDefinition.create(createClayHorseMesh(new CubeDeformation(ClayHorseEntity.SCALE * 1.1f)), 64, 64);
    }
    private static MeshDefinition createClayHorseMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, 11.0F, 5.0F)
        );
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
                HEAD_PARTS,
                CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, DEG_30, 0.0F, 0.0F)
        );
        PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, cubeDeformation), PartPose.ZERO
        );

        partdefinition2.addOrReplaceChild(
                "upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
                "left_hind_leg",
                CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubeDeformation),
                PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_hind_leg",
                CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubeDeformation),
                PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubeDeformation),
                PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubeDeformation),
                PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        CubeDeformation cubedeformation = cubeDeformation.extend(0.0F, 5.5F, 0.0F);
        partdefinition.addOrReplaceChild(
                "left_hind_baby_leg",
                CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
                PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_hind_baby_leg",
                CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
                PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
                "left_front_baby_leg",
                CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
                PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        partdefinition.addOrReplaceChild(
                "right_front_baby_leg",
                CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
                PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        partdefinition1.addOrReplaceChild(
                "tail",
                CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, cubeDeformation),
                PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
                SADDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
                LEFT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
                RIGHT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
                LEFT_SADDLE_LINE,
                CubeListBuilder.create().texOffs(32, 2).addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
                PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
                RIGHT_SADDLE_LINE,
                CubeListBuilder.create().texOffs(32, 2).addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
                PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
                HEAD_SADDLE, CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.22F)), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
                MOUTH_SADDLE_WRAP,
                CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
                "left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
                "right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        return meshdefinition;
    }

    @Override
    public void setupAnim(ClayHorseRenderState clayHorseRenderState) {
        super.setupAnim(clayHorseRenderState);
        boolean saddled = clayHorseRenderState.isSaddled;

        for(ModelPart modelpart : this.saddleParts) {
            modelpart.visible = saddled;
        }

        for(ModelPart modelpart1 : this.ridingParts) {
            modelpart1.visible = clayHorseRenderState.isRidden && saddled;
        }

        this.body.y = 11.0F;

        setSittingPose(clayHorseRenderState);
    }

    private void setSittingPose(ClayHorseRenderState horse) {
        if (horse.isInSittingPose && !horse.isRidden) {
            this.rightHindLeg.xRot = -DEG_90;
            this.leftHindLeg.xRot = -DEG_90;
            this.rightFrontLeg.xRot = DEG_90;
            this.leftFrontLeg.xRot = DEG_90;
        }
    }
}