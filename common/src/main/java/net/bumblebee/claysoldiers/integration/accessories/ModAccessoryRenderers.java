package net.bumblebee.claysoldiers.integration.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class ModAccessoryRenderers {
    public static void init() {
        AccessoriesRendererRegistry.registerArmorRendering(ModItems.CLAY_GOGGLES.get());
        AccessoriesRendererRegistry.registerRenderer(ModItems.CLAY_SOLDIER.get(), () -> new ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel.createModel()));
    }

    private static class ClaySoldierOnHeadRenderer implements AccessoryRenderer {
        private final ClaySoldierOnHeadModel<?> soldierModel;

        public ClaySoldierOnHeadRenderer(ClaySoldierOnHeadModel<?> model) {
            this.soldierModel = model;
        }

        @Override
        public <S extends LivingEntityRenderState> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<S> model, S renderState, MultiBufferSource multiBufferSource, int light, float partialTicks) {
            var team = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
            if (team == null) {
                return;
            }


            ClayMobTeamManger.getOptional(team, reference.entity().registryAccess()).ifPresent(t -> {
                if (model instanceof HumanoidModel<?> humanoidModel) {
                    soldierModel.copyHeadRotation(humanoidModel);
                }

                soldierModel.render(matrices, multiBufferSource, light, t.getColor(0, (int) renderState.ageInTicks, partialTicks));
            });
        }
    }
}
