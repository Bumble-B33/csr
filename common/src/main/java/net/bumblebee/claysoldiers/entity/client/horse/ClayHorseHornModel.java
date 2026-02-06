package net.bumblebee.claysoldiers.entity.client.horse;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class ClayHorseHornModel extends ClayHorseModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse"), "horn");

    private static final String HORN_NAME = "horn";

    public ClayHorseHornModel(ModelPart pRoot) {
        super(pRoot);
    }

    public static LayerDefinition createHornLayer() {
        MeshDefinition meshDefinition = createClayHorseMesh(new CubeDeformation(ClayHorseEntity.SCALE));
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.retainPartsAndChildren(Set.of(HEAD_PARTS));

        PartDefinition headParts = partDefinition.clearChild(HEAD_PARTS);

        PartDefinition head = headParts.clearChild("head");

        head.addOrReplaceChild(HORN_NAME, CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1f, -17, -0f, 2, 6, 2),
                PartPose.offset(0, 0, 0)
        );

        headParts.retainPartsAndChildren(Set.of(HORN_NAME));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
