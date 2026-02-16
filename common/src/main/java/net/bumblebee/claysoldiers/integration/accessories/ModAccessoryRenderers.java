package net.bumblebee.claysoldiers.integration.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderState;
import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
import io.wispforest.accessories.api.client.renderers.SimpleAccessoryRenderer;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ModAccessoryRenderers {
    public static void init() {
        AccessoriesRendererRegistry.bindItemToArmorRenderer(ModItems.CLAY_GOGGLES.get());
        AccessoriesRendererRegistry.registerRenderer(ClaySoldierOnHeadRenderer.ID, () -> new ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel.createModel()));
        AccessoriesRendererRegistry.bindItemToRenderer(ModItems.CLAY_SOLDIER.get(), ClaySoldierOnHeadRenderer.ID);

        AccessoriesRendererRegistry.registerRenderer(StatometerRenderer.ID, StatometerRenderer::new);
        AccessoriesRendererRegistry.bindItemToRenderer(ModItems.STATOMETER.get(), StatometerRenderer.ID);

    }

    private static class ClaySoldierOnHeadRenderer implements AccessoryRenderer {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_on_head");
        private final ClaySoldierOnHeadModel<HumanoidRenderState> soldierModel;
        private RegistryAccess registryAccess;

        public ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel<HumanoidRenderState> model) {
            this.soldierModel = model;
        }

        @Override
        public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryRenderState, S s, EntityModel<S> entityModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            var team = accessoryRenderState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK).get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
            if (team == null) {
                return;
            }

            if (registryAccess == null) {
                registryAccess = Minecraft.getInstance().level.registryAccess();
            }


            ClayMobTeamManger.getOptional(team, registryAccess).ifPresent(t -> {
                if (entityModel instanceof HumanoidModel<?> humanoidModel) {
                    soldierModel.copyHeadRotation(humanoidModel);
                }
                if (s instanceof HumanoidRenderState humanoidRenderState) {
                    submitNodeCollector.submitModel(
                            soldierModel,
                            (HumanoidRenderState) humanoidRenderState,
                            poseStack,
                            ClaySoldierOnHeadModel.RENDER_TYPE,
                            s.lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            t.getColor(0, s.ageInTicks),
                            null,
                            s.outlineColor,
                            null);
                }
            });
        }
    }

    private static class StatometerRenderer implements SimpleAccessoryRenderer {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "on_belt");
        private static final float DEG_180 = Mth.PI;
        private static final float DEG_90 = Mth.PI / 2;

        @Override
        public <S extends LivingEntityRenderState> void align(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, PoseStack poseStack) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(Axis.XP.rotation(DEG_180));
            poseStack.translate(0.52f, -1.15f, 0.1f);
            poseStack.mulPose(Axis.YP.rotation(DEG_90));
        }
    }
}
