package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HamsterWheelModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel"), "main");
    private final ModelPart wheel;

    public HamsterWheelModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.wheel = root.getChild("wheel");
    }

    public static LayerDefinition createWheelLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create(),
                PartPose.offsetAndRotation(8, 7.965F, 9F, 0, 0, Mth.PI));

        wheel.addOrReplaceChild("beam_r1", CubeListBuilder.create().texOffs(20, 15).addBox(-0.5F, -0.5F, 0.498F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 16).addBox(-1.0F, -5.465F, -0.502F, 2.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition path = wheel.addOrReplaceChild("path", CubeListBuilder.create().texOffs(11, 0).addBox(-10.5F, 5.5355F, 6.0F, 5.0F, 0.0F, 4.0F)
                .texOffs(11, 0).addBox(-10.5F, -5.5355F, 6.0F, 5.0F, 0.0F, 4.0F)
                .texOffs(6, 12).addBox(-2.7145F, -2.5F, 6.0F, 0.0F, 5.0F, 4.0F)
                .texOffs(6, 12).addBox(-13.5355F, -2.5F, 6.0F, 0.0F, 5.0F, 4.0F), PartPose.offset(8.0F, 0.035F, -9.0F));

        path.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(6, 12).addBox(-5.5355F, -2.5F, -2.0F, 0.0F, 5.0F, 4.0F)
                .texOffs(6, 12).addBox(5.5355F, -2.5F, -2.0F, 0.0F, 5.0F, 4.0F)
                .texOffs(11, 0).addBox(-2.5F, -5.5355F, -2.0F, 5.0F, 0.0F, 4.0F)
                .texOffs(11, 0).addBox(-2.5F, 5.5355F, -2.0F, 5.0F, 0.0F, 4.0F), PartPose.offsetAndRotation(-8.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition ring_back = wheel.addOrReplaceChild("ring_back", CubeListBuilder.create().texOffs(15, 4).addBox(-10.5F, 5.0355F, 9.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(15, 4).addBox(-10.5F, -6.0355F, 9.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(10, 21).addBox(-2.9645F, -2.5F, 9.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(10, 21).addBox(-14.0355F, -2.5F, 9.0F, 1.0F, 5.0F, 1.0F), PartPose.offset(8.0F, 0.035F, -8.0F));

        ring_back.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(10, 21).addBox(-6.0355F, -2.5F, 0.999F, 1.0F, 5.0F, 1.0F)
                .texOffs(10, 21).addBox(5.0355F, -2.5F, 0.999F, 1.0F, 5.0F, 1.0F)
                .texOffs(15, 4).addBox(-2.5F, -6.0355F, 0.999F, 5.0F, 1.0F, 1.0F)
                .texOffs(15, 4).addBox(-2.5F, 5.0355F, 0.999F, 5.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-8.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition ring_front = wheel.addOrReplaceChild("ring_front", CubeListBuilder.create().texOffs(15, 4).addBox(-10.5F, 5.0355F, 6.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(15, 4).addBox(-10.5F, -6.0355F, 6.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(10, 21).addBox(-2.9645F, -2.5F, 6.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(10, 21).addBox(-14.0355F, -2.5F, 6.0F, 1.0F, 5.0F, 1.0F), PartPose.offset(8.0F, 0.035F, -10.0F));

        ring_front.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(10, 21).addBox(-6.0355F, -2.5F, -1.999F, 1.0F, 5.0F, 1.0F)
                .texOffs(10, 21).addBox(5.0355F, -2.5F, -1.999F, 1.0F, 5.0F, 1.0F)
                .texOffs(15, 4).addBox(-2.5F, -6.0355F, -1.999F, 5.0F, 1.0F, 1.0F)
                .texOffs(15, 4).addBox(-2.5F, 5.0355F, -1.999F, 5.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-8.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void setUpRotation(float rotation) {
        wheel.zRot = rotation;
    }
}
