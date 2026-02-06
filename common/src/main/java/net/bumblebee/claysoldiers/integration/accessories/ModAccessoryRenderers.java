package net.bumblebee.claysoldiers.integration.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderState;
import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotPath;
import io.wispforest.accessories.api.slot.SlotReference;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ModAccessoryRenderers {
    public static void init() {
        AccessoriesRendererRegistry.bindItemToArmorRenderer(ModItems.CLAY_GOGGLES.get());
        AccessoriesRendererRegistry.registerRenderer(ClaySoldierOnHeadRenderer.ID, () -> new ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel.createModel()));
        AccessoriesRendererRegistry.bindItemToRenderer(ModItems.CLAY_SOLDIER.get(), ClaySoldierOnHeadRenderer.ID);
    }

    private static class ClaySoldierOnHeadRenderer implements AccessoryRenderer {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_on_head");
        private final ClaySoldierOnHeadModel<HumanoidRenderState> soldierModel;
        private final RegistryAccess registryAccess;

        public ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel<HumanoidRenderState> model) {
            this.soldierModel = model;
            this.registryAccess = Minecraft.getInstance().level.registryAccess();
        }

        @Override
        public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryRenderState, S s, EntityModel<S> entityModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            var team = accessoryRenderState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK).get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
            if (team == null) {
                return;
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



                //soldierModel.render(matrices, multiBufferSource, light, t.getColor(0, (int) renderState.ageInTicks, partialTicks));
            });
        }
    }
}
