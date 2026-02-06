package net.bumblebee.claysoldiers.item.claymobspawn;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Function;

public class ClaySoldierOnHeadModel<T extends HumanoidRenderState> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_on_head"), "main");
    private static final String SOLDIER_NAME = "soldier";
    private static final String SOLDIER_LEFT_ARM = "soldier_left_arm";
    private static final String SOLDIER_RIGHT_ARM = "soldier_right_arm";
    private static final ResourceLocation SOLDIER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/clay.png");
    public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(SOLDIER_TEXTURE);
    public final ModelPart soldierHead;
    public final ModelPart soldierLeftArm;
    public final ModelPart soldierRightArm;


    public ClaySoldierOnHeadModel(ModelPart root) {
        super(root);
        ModelPart soldier = root.getChild("head").getChild(SOLDIER_NAME);
        this.soldierHead = soldier.getChild("soldier_head");
        this.soldierLeftArm = soldier.getChild(SOLDIER_LEFT_ARM);
        this.soldierRightArm = soldier.getChild(SOLDIER_RIGHT_ARM);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int color) {
        this.renderToBuffer(poseStack, buffer.getBuffer(ClaySoldierOnHeadModel.RENDER_TYPE), packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    public void setupAnimSoldierAnim(LivingEntityRenderState p_361833_) {
        float f1 = p_361833_.walkAnimationPos;
        float f2 = p_361833_.walkAnimationSpeed;
        this.rightArm.xRot = Mth.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f2 * 0.5F;
        this.leftArm.xRot = Mth.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F;
    }

    public void copyHeadRotation(HumanoidModel<?> toCopyFrom) {
        this.head.xRot = toCopyFrom.head.xRot;
        this.head.yRot = toCopyFrom.head.yRot;
        this.head.zRot = toCopyFrom.head.zRot;
    }

    public static ClaySoldierOnHeadModel<HumanoidRenderState> createModel() {
        return createModel(Minecraft.getInstance().getEntityModels()::bakeLayer);
    }

    public static ClaySoldierOnHeadModel<HumanoidRenderState> createModel(Function<ModelLayerLocation, ModelPart> bakery) {
        return new ClaySoldierOnHeadModel<>(bakery.apply(ClaySoldierOnHeadModel.LAYER_LOCATION));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition headPart = root.clearChild("head");
        headPart.clearChild("hat");

        PartDefinition soldier = headPart.addOrReplaceChild(SOLDIER_NAME, CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -16.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 1.0F));

        soldier.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, -0.3054F, 0.0F));
        soldier.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.3054F, 0.0F));
        soldier.addOrReplaceChild("soldier_head", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));
        soldier.addOrReplaceChild("soldier_left_arm", CubeListBuilder.create().texOffs(8, 4).addBox(-2.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));
        soldier.addOrReplaceChild("soldier_right_arm", CubeListBuilder.create().texOffs(8, 4).addBox(1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));


        root.clearChild("body");
        root.clearChild("left_arm");
        root.clearChild("right_arm");
        root.clearChild("left_leg");
        root.clearChild("right_leg");
        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
