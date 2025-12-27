package net.bumblebee.claysoldiers.entity.client.accesories;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public class ClaySoldierCapeModel extends ClaySoldierModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_cape"), "cloak");

    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    private static final CubeDeformation SHRINK_DEFORMATION = new CubeDeformation(SCALE, SCALE, SCALE);
    private static final String CLOAK = "cape";

    private final ModelPart cloak = this.body.getChild(CLOAK);

    public ClaySoldierCapeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createSoldierMesh() {
        /*MeshDefinition meshDefinition = ClaySoldierModel.createMesh();
        PartDefinition partdefinition = meshDefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "cloak",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, SHRINK_DEFORMATION),
                PartPose.offset(0.0F, 0.0F, 2.0F)
        );
        return LayerDefinition.create(meshDefinition, 32, 32);*/
        MeshDefinition meshDefinition = ClaySoldierModel.createSoldierMesh(SHRINK_DEFORMATION, 0);
        PartDefinition partdefinition = meshDefinition.getRoot();
        PartDefinition headPart = partdefinition.clearChild("head");
        headPart.clearChild("hat");
        partdefinition.clearChild(ClaySoldierModel.BAMBOO_STICK_NAME);
        PartDefinition bodyPart = partdefinition.clearChild("body");
        partdefinition.clearChild("left_arm");
        partdefinition.clearChild("right_arm");
        partdefinition.clearChild("left_leg");
        partdefinition.clearChild("right_leg");
        bodyPart.addOrReplaceChild(CLOAK, CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, (float) Math.PI, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(AbstractClaySoldierRenderState pEntity) {
        super.setupAnim(pEntity);
        this.cloak.rotateBy(
                new Quaternionf()
                        .rotateY((float) -Math.PI)
                        .rotateX((6.0F + pEntity.capeLean / 2.0F + pEntity.capeFlap) * (float) (Math.PI / 180.0))
                        .rotateZ(pEntity.capeLean2 / 2.0F * (float) (Math.PI / 180.0)).rotateY((180.0F - pEntity.capeLean2 / 2.0F) * (float) (Math.PI / 180.0)));
    }
}
