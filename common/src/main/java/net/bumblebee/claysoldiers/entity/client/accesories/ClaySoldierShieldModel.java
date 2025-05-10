package net.bumblebee.claysoldiers.entity.client.accesories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ClaySoldierShieldModel extends EntityModel<AbstractClaySoldierEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_shield"), "main");
    private final ModelPart shield;

    public ClaySoldierShieldModel(ModelPart root) {
        this.shield = root.getChild("plate");
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

    @Override
    public void setupAnim(AbstractClaySoldierEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int color) {
        shield.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
    }
}
