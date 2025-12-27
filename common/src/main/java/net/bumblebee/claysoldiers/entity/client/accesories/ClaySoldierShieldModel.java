package net.bumblebee.claysoldiers.entity.client.accesories;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ClaySoldierShieldModel extends EntityModel<AbstractClaySoldierRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_shield"), "main");

    public ClaySoldierShieldModel(ModelPart root) {
        super(root.getChild("plate"));
    }

    public static LayerDefinition createShieldLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -2.0F, 10.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 15).addBox(-6.0F, -3.0F, -2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 13).addBox(-3.0F, -6.0F, -2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(21, 21).addBox(5.0F, -3.0F, -2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 11).addBox(-3.0F, 5.0F, -2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);


        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
