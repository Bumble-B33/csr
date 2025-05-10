package net.bumblebee.claysoldiers.entity.client.accesories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ClaySoldierCapeModel extends EntityModel<AbstractClaySoldierEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_cape"), "cloak");

    private static final float SCALE = AbstractClaySoldierEntity.DEFAULT_SCALE;
    private static final CubeDeformation SHRINK_DEFORMATION = new CubeDeformation(SCALE, SCALE, SCALE);
    private final ModelPart cloak;

    public ClaySoldierCapeModel(ModelPart root) {
        this.cloak = root.getChild("cloak");

    }

    public static LayerDefinition createSoldierMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partdefinition = meshDefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "cloak",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, SHRINK_DEFORMATION),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(AbstractClaySoldierEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (pEntity.getItemBySlot(SoldierEquipmentSlot.CHEST).isEmpty()) {
            if (pEntity.isCrouching()) {
                this.cloak.z = 1.4F;
                this.cloak.y = 1.85F;
            } else {
                this.cloak.z = 0.0F;
                this.cloak.y = 0.0F;
            }
        } else if (pEntity.isCrouching()) {
            this.cloak.z = 0.3F;
            this.cloak.y = 0.8F;
        } else {
            this.cloak.z = -1.1F;
            this.cloak.y = -0.85F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int color) {
        cloak.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
    }
}
