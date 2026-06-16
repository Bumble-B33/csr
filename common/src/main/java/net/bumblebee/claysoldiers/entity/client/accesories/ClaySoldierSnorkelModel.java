package net.bumblebee.claysoldiers.entity.client.accesories;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

import java.util.Set;

public class ClaySoldierSnorkelModel extends ClaySoldierModel {
    public static final ModelLayerLocation SNORKEL_LAYER_LOCATION = createLayerLocation("snorkel");
    public static final String BAMBOO_STICK_NAME = "bamboo_stick";


    public ClaySoldierSnorkelModel(ModelPart pRoot) {
        super(pRoot);
    }

    public static LayerDefinition createSnorkelLayer() {
        MeshDefinition meshDefinition = createSoldierMesh(SHRINK_DEFORMATION, 0);

        var headPart = meshDefinition.getRoot().clearChild("head");

        headPart.addOrReplaceChild(BAMBOO_STICK_NAME, CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.0F, -16.0F, -6.0F, 2.0F, 12.0F, 2.0F, SHRINK_DEFORMATION, 0.25f, 0.25f),
                PartPose.offset(0.0F, 0, 0.0F));

        meshDefinition.getRoot().retainPartsAndChildren(Set.of(BAMBOO_STICK_NAME));


        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
