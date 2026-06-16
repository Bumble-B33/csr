package net.bumblebee.claysoldiers.entity.client.programmable;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.Set;

public class ClaySoldierChipModel extends ClaySoldierModel {
    public static final ModelLayerLocation LAYER_LOCATION = createLayerLocation("chip");
    public static final String CHIP_NAME = "chip";


    public ClaySoldierChipModel(ModelPart pRoot) {
        super(pRoot);
    }

    public static LayerDefinition createChipLayer() {
        MeshDefinition meshDefinition = createSoldierMesh(SHRINK_DEFORMATION, 0);

        var headPart = meshDefinition.getRoot().clearChild("head");

        PartDefinition chip = headPart.addOrReplaceChild(CHIP_NAME, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-2.75F, 0.5F, -2.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 9).addBox(-2.75F, 0.5F, -1.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-2.75F, 0.5F, 0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-2.75F, 0.5F, 1.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 13).addBox(1.75F, 0.5F, 1.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 11).addBox(1.75F, 0.5F, 0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 9).addBox(1.75F, 0.5F, -1.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 7).addBox(1.75F, 0.5F, -2.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -9.0F, 0.0F));

        meshDefinition.getRoot().retainPartsAndChildren(Set.of(CHIP_NAME));


        return LayerDefinition.create(meshDefinition, 32, 32);
    }
}
