package net.bumblebee.claysoldiers.item.claystaff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModEnchantments;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class ClayStaffModel extends Model {
    private static final float DEG_2 = Mth.PI / 90f;
    private static final float DEG_22 = Mth.PI / 4;
    private static final float CUBE_Y = -5.5f;
    private static final float SOLDIER_Y = -3f;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/item/clay_staff.png");

    public static final ModelLayerLocation SOLDIER_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff_soldier"), "main");
    public static final ResourceLocation SOLDIER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/clay.png");
    private static final RenderType DOLL_RENDER_TYPE = RenderType.entityCutout(SOLDIER_TEXTURE);

    private final ModelPart root;
    private final ModelPart cube;
    private final ModelPart doll;

    public ClayStaffModel(ModelPart root, ModelPart doll) {
        super(RenderType::entitySolid);
        this.root = root;
        this.cube = root.getChild("cube");
        this.doll = doll;

    }

    public static ClayStaffModel create(Function<ModelLayerLocation, ModelPart> bakery) {
        return new ClayStaffModel(bakery.apply(LAYER_LOCATION), bakery.apply(SOLDIER_LAYER_LOCATION));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void setCubeRotation(float radian) {
        cube.xRot = radian;
    }

    public void hideCube(boolean hide) {
        cube.visible = !hide;
    }

    public void scale(float percent, boolean scaleDoll) {
        if (scaleDoll) {
            percent *= 0.5f;
            doll.xScale = percent;
            doll.yScale = percent;
            doll.zScale = percent;
            doll.y = SOLDIER_Y + 1.5f - (percent * 1.5f);
        } else {
            cube.xScale = percent;
            cube.yScale = percent;
            cube.zScale = percent;
            cube.y = CUBE_Y + 3 - (percent * 3);
        }
    }

    public static void renderAsItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (ClaySoldiersClient.clayStaffModel == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);


        boolean doll = ClayStaffItem.getEnchantmentLevel(pStack, ModEnchantments.SOLDIER_PROJECTILE, Minecraft.getInstance().level.registryAccess()) > 0;

        if (pDisplayContext.firstPerson() || pDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || pDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            float scale = ((float) Minecraft.getInstance().player.getTicksUsingItem()) / ClayStaffItem.getMaxPower(pStack, Minecraft.getInstance().level.registryAccess());
            ClaySoldiersClient.clayStaffModel.hideCube(doll);
            ClaySoldiersClient.clayStaffModel.scale(Math.min(scale, 1), doll);
            if (doll) {
                ClaySoldiersClient.clayStaffModel.renderDoll(poseStack, buffer, packedLight, packedOverlay);
            } else {
                ClaySoldiersClient.clayStaffModel.setCubeRotation(((Minecraft.getInstance().level.getGameTime() + getPartialTick()) % 360) * DEG_2);
            }
        } else {
            ClaySoldiersClient.clayStaffModel.hideCube(true);
        }

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                buffer, ClaySoldiersClient.clayStaffModel.renderType(ClayStaffModel.TEXTURE), false, pStack.hasFoil()
        );
        ClaySoldiersClient.clayStaffModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);


        poseStack.popPose();
    }

    public void renderDoll(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack ammo = ClayStaffItem.getClayStaffAmmo(ClayStaffItem.SOLDIER_PREDICATE, Minecraft.getInstance().player);
        int color = -1;
        if (ammo != null) {
            var team = ClayMobTeamManger.getFromKey(ammo.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), Minecraft.getInstance().level.registryAccess());
            if (team != null) {
                color = team.getColor(Minecraft.getInstance().player, getPartialTick());
            }
        }
        doll.render(poseStack, buffer.getBuffer(DOLL_RENDER_TYPE), packedLight, packedOverlay, color);
    }

    public static LayerDefinition createStaffLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();

        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition staff = partdefinition.addOrReplaceChild("staff",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 21.0F, 1.0F),
                PartPose.ZERO
        );
        staff.addOrReplaceChild("wing",
                CubeListBuilder.create()
                        .texOffs(12, 6).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.ZERO);


        staff.addOrReplaceChild("right_r1", CubeListBuilder.create().texOffs(8, 6)
                        .addBox(-1.0F, -9.0F, -1.001F, 1.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(0.5F, 3F, 0.5F, 0.0F, 0.0F, 0.3927F));

        staff.addOrReplaceChild("left_r1", CubeListBuilder.create().texOffs(4, 6)
                        .addBox(0.0F, -9.0F, -1.002F, 1.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(-0.5F, 3F, 0.5F, 0.0F, 0.0F, -0.4451F));


        PartDefinition cube = partdefinition.addOrReplaceChild("cube", CubeListBuilder.create(),
                PartPose.offset(0, CUBE_Y, 0));

        PartDefinition wrapper = cube.addOrReplaceChild("wrapper", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F));

        wrapper.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createSoldierDollLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();

        PartDefinition partdefinition = meshdefinition.getRoot();


        PartDefinition doll = partdefinition.addOrReplaceChild("soldier", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0, SOLDIER_Y, 0, 0, 0f, DEG_22));


        doll.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(8, 7).addBox(-0.5F, -1.5F, -1F, 1.0F, 3.0F, 2.0F),
                PartPose.ZERO);
        doll.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.5F, -2F, 1.0F, 3.0F, 4.0F),
                PartPose.ZERO);
        doll.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 7).addBox(-1F, -6.5F, -1F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static float getPartialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
}
