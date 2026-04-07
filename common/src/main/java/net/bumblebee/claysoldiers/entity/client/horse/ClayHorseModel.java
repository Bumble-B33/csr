package net.bumblebee.claysoldiers.entity.client.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.ClayHorseRenderState;
import net.bumblebee.claysoldiers.entity.common.horse.ClayHorseEntity;
import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ClayHorseModel extends AbstractEquineModel<ClayHorseRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse"), "main");
    public static final ModelLayerLocation ARMOR_LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse"), "armor");


    private static final float DEG_30 = Mth.PI / 6;
    private static final float DEG_90 = Mth.PI / 2;
    protected static final String HEAD_PARTS = "head_parts";
     protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ClayHorseModel(ModelPart pRoot) {
        super(pRoot);
        this.body = pRoot.getChild("body");
        this.headParts = pRoot.getChild(HEAD_PARTS);
        this.rightHindLeg = pRoot.getChild("right_hind_leg");
        this.leftHindLeg = pRoot.getChild("left_hind_leg");
        this.rightFrontLeg = pRoot.getChild("right_front_leg");
        this.leftFrontLeg = pRoot.getChild("left_front_leg");
    }

    public static LayerDefinition createLayerDefinition() {
        return LayerDefinition.create(createClayHorseMesh(new CubeDeformation(ClayHorseEntity.SCALE)), 64, 64);
    }
    public static LayerDefinition createLayerArmorDefinition() {
        return LayerDefinition.create(createClayHorseMesh(new CubeDeformation(ClayHorseEntity.SCALE * 1.1f)), 64, 64);
    }
    protected static MeshDefinition createClayHorseMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = createBodyMesh(cubeDeformation);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition headParts = partdefinition.getChild(HEAD_PARTS);
        headParts.clearChild("mane");
        return meshdefinition;
    }

    @Override
    public void setupAnim(ClayHorseRenderState clayHorseRenderState) {
        super.setupAnim(clayHorseRenderState);

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