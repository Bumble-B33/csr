package net.bumblebee.claysoldiers.item.claystaff;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClayStaffModel extends Model<ClayStaffRenderState> {
    private static final float DEG_2 = Mth.PI / 90f;
    private static final float DEG_22 = Mth.PI / 4;
    private static final float CUBE_Y = -5.5f;
    private static final float SOLDIER_Y = -3f;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff"), "main");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/item/clay_staff_in_hand.png");

    public static final ModelLayerLocation SOLDIER_LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff_soldier"), "main");
    public static final Identifier SOLDIER_TEXTURE = Identifier.withDefaultNamespace("textures/block/clay.png");
    private static final RenderType DOLL_RENDER_TYPE = RenderTypes.entityCutout(SOLDIER_TEXTURE);

    private final ModelPart cube;
    private final ModelPart doll;

    public ClayStaffModel(ModelPart root, ModelPart doll) {
        super(root, RenderTypes::entitySolid);
        this.cube = root.getChild("cube");
        this.doll = doll;

    }

    public static ClayStaffModel create(Function<ModelLayerLocation, ModelPart> bakery) {
        return new ClayStaffModel(bakery.apply(LAYER_LOCATION), bakery.apply(SOLDIER_LAYER_LOCATION));
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

    @Override
    public void setupAnim(ClayStaffRenderState renderState) {
        super.setupAnim(renderState);
        if (renderState.hideAmmo) {
            hideCube(true);
        } else {
            hideCube(renderState.hasDoll);
            scale(renderState.scale, renderState.hasDoll);
            if (renderState.hasDoll) {
                setCubeRotation(renderState.cubeRotation);
            }
        }

    }

    public void getExtents(Consumer<Vector3fc> set) {
        root.getExtentsForGui(new PoseStack(), set);
    }

    public static ClayStaffRenderState extractRenderState(ItemStack stack) {
        boolean doll = ClayStaffItem.getEnchantmentLevel(stack, ModEnchantments.SOLDIER_PROJECTILE, Minecraft.getInstance().level.registryAccess()) > 0;

        return new ClayStaffRenderState(
                false,
                Math.min(1f, ((float) Minecraft.getInstance().player.getTicksUsingItem()) / ClayStaffItem.getMaxPower(stack, Minecraft.getInstance().level.registryAccess())),
                doll,
                ((Minecraft.getInstance().level.getGameTime() + getPartialTick()) % 360) * DEG_2
        );
    }

    public void submitAsItem(ClayStaffRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean foil, int outline) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);


        if (state.hasDoll) {
            this.submitDoll(poseStack, nodeCollector, packedLight, packedOverlay);

        }
        nodeCollector.submitModel(this, state, poseStack, renderType(ClayStaffModel.TEXTURE), packedLight, packedOverlay, 0, null);

        if (foil) {
            nodeCollector.order(1)
                    .submitModel(
                            this,
                            state,
                            poseStack,
                            ItemFeatureRenderer.getFoilRenderType(renderType(ClayStaffModel.TEXTURE), false),
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            outline,
                            null
                    );
        }


        poseStack.popPose();
    }

    public void submitDoll(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay) {
        ItemStack ammo = ClayStaffItem.getClayStaffAmmo(ClayStaffItem.SOLDIER_PREDICATE, Minecraft.getInstance().player);
        int color = -1;
        if (ammo != null) {
            var team = ClayMobTeamManger.get(ammo.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), Minecraft.getInstance().level.registryAccess());
            if (team.isPresent()) {
                color = team.orElseThrow().value().getColor().getColor(Minecraft.getInstance().player, getPartialTick());
            }
        }
        nodeCollector.submitModelPart(doll, poseStack, DOLL_RENDER_TYPE, packedLight, packedOverlay, null, color, null);
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
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}
