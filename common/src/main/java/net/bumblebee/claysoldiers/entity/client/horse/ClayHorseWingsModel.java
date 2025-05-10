package net.bumblebee.claysoldiers.entity.client.horse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.entity.horse.ClayHorseEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ClayHorseWingsModel extends EntityModel<AbstractClayHorse> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_horse_wings"), "main");

    private static final float SCALE = ClayHorseEntity.SCALE;
    private static final CubeDeformation SHRINK_DEFORMATION = new CubeDeformation(SCALE, 0, SCALE);
    private final ModelPart wings;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public ClayHorseWingsModel(ModelPart root) {
        this.wings = root.getChild("wings");
        this.leftWing = this.wings.getChild("left_wing");
        this.rightWing = this.wings.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wings = partdefinition.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(0.0F, 0, -5));

        wings.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(-22, 0).mirror()
                .addBox(-16, 0.0F, -11, 16.0F, 0.0F, 22.0F, SHRINK_DEFORMATION).mirror(false), PartPose.offset(-5, 0, -1));

        wings.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-22, 0)
                .addBox(0, 0.0F, -11, 16.0F, 0.0F, 22.0F, SHRINK_DEFORMATION), PartPose.offset(5F, 0, -1));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    public void setUpWingAnim(AbstractClayHorse pEntity, float pAgeInTicks) {
        this.leftWing.z = 5;
        this.rightWing.z = 5;
        this.leftWing.y = 5.1F;
        this.rightWing.y = 5.1F;
        this.leftWing.zRot = 0;
        this.rightWing.zRot = 0;

        if (pEntity.onGround()) {
            this.leftWing.zRot = 0.85f;
            this.rightWing.zRot = -0.85f;
        } else {
            float timePoint = pAgeInTicks * 56f * ((float)Math.PI / 180F);
            this.rightWing.zRot = Mth.cos(timePoint) * (float)Math.PI * 0.15F;
            this.leftWing.zRot = -this.rightWing.zRot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int color) {
        wings.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, color);
    }

    @Override
    public void setupAnim(AbstractClayHorse pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {}
}
