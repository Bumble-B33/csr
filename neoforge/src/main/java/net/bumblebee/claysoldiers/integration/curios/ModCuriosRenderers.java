package net.bumblebee.claysoldiers.integration.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.ClientWearableRenderer;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class ModCuriosRenderers {
    private static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "curios_clay_goggles"), "main");

    public static void registerLayerEvent(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER_LOCATION, () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION), 64, 32));
    }

    public static void init() {
        CuriosRendererRegistry.register(ModItems.CLAY_GOGGLES.get(), CuriosHeadLayer::clayGoggles);
    }

    private static class CuriosHeadLayer implements ICurioRenderer {
        private final ModelPart modelPart;
        private final Model model;
        private final TextureAtlas armorTrimAtlas;
        private final ArmorItem armorItem;
        private final ArmorMaterial armorMaterial;
        private final boolean inner;

        public CuriosHeadLayer(ModelPart part, TextureAtlas armorTrimAtlas, ArmorItem armorItem, boolean inner) {
            this.modelPart = part;
            this.model = new ModelFromModelPart(part);
            this.armorTrimAtlas = armorTrimAtlas;
            this.armorItem = armorItem;
            this.armorMaterial = armorItem.getMaterial().value();
            this.inner = inner;
        }

        private static CuriosHeadLayer clayGoggles() {
            return new CuriosHeadLayer(
                    Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION),
                    Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET),
                    ModItems.CLAY_GOGGLES.get(), false
            );
        }


        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ICurioRenderer.followHeadRotations(slotContext.entity(), modelPart);
            armorMaterial.layers().forEach(layer -> {
                model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(model.renderType(layer.texture(inner))), light, OverlayTexture.NO_OVERLAY);
            });


            ArmorTrim armortrim = stack.get(DataComponents.TRIM);
            if (armortrim != null) {
                ClientWearableRenderer.renderTrim(armorTrimAtlas, armorItem.getMaterial(), matrixStack, renderTypeBuffer, armortrim, light, model, false, -1);
            }

            if (stack.hasFoil()) {
                modelPart.render(matrixStack, renderTypeBuffer.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
            }


        }
    }

    private static class ModelFromModelPart extends Model {
        private final ModelPart modelPart;

        public ModelFromModelPart(ModelPart modelPart) {
            super(RenderType::armorCutoutNoCull);
            this.modelPart = modelPart;
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }
}
