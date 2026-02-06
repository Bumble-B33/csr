package net.bumblebee.claysoldiers.integration.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class ModCuriosRenderers {
    public ModCuriosRenderers(IEventBus modEventBus) {
        //modEventBus.addListener(this::addLayerEvent);
        //modEventBus.addListener(this::init);

        //NeoForge.EVENT_BUS.addListener(this::tooltipEvent);
    }

    /*public void addLayerEvent(final EntityRenderersEvent.AddLayers event) {
        //CuriosHeadLayer.equipmentRenderer = event.getContext().getEquipmentRenderer();
    }

    public void init(final FMLClientSetupEvent event) {
        //CuriosRendererRegistry.register(ModItems.CLAY_GOGGLES.get(), CuriosHeadLayer::clayGoggles);
        //CuriosRendererRegistry.register(ModItems.CLAY_SOLDIER.get(), SoldierOnHeadLayer::claySoldier);
    }

    public void tooltipEvent(final ItemTooltipEvent event) {
        var player = event.getEntity();
        if (!event.getItemStack().is(ModItems.CLAY_SOLDIER.get()) || player == null) {
            return;
        }
        var team = event.getItemStack().get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT);
        if (team == null) {
            return;
        }
        if (ClaySoldierSpawnItem.canEquipClaySoldier(player, team)) {
            var component = appendDefaultSlotTooltip(event.getItemStack(), player);
            if (component != null) {
                event.getToolTip().add(component);
                event.getToolTip().add(CommonComponents.space().append(Component.translatable(ClaySoldierSpawnItem.DESCRIPTION_LANG).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private static Component appendDefaultSlotTooltip(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return null;
        }
        Map<String, ISlotType> slots = CuriosApi.getItemStackSlots(stack, player);
        slots.remove("curio");

        if (slots.isEmpty()) {
            return null;
        }
        List<String> slotIds = slots.keySet().stream().toList();
        MutableComponent slotsTooltip =
                Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD);

        for (int j = 0; j < slotIds.size(); j++) {
            String id = slotIds.get(j);
            String key = "curios.identifier." + id;
            MutableComponent type =
                    Component.translatableWithFallback(
                            key, Character.toUpperCase(id.charAt(0)) + id.substring(1).toLowerCase());

            if (j < slotIds.size() - 1) {
                type = type.append(", ");
            }
            type = type.withStyle(ChatFormatting.YELLOW);
            slotsTooltip.append(type);
        }
        return slotsTooltip;
    }*/

    /*private static class CuriosHeadLayer implements ICurioRenderer {
        private final HumanoidModel<? extends HumanoidRenderState> model;
        private static EquipmentLayerRenderer equipmentRenderer;

        public CuriosHeadLayer(HumanoidModel<? extends HumanoidRenderState> part) {
            this.model = part;
        }

        private static CuriosHeadLayer clayGoggles() {
            return new CuriosHeadLayer(
                    new HumanoidArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR))
            );
        }

        @Override
        public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, @NotNull MultiBufferSource renderTypeBuffer, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

            if (equippable == null || equipmentRenderer == null) {
                return;
            }
            ICurioRenderer.setupHumanoidAnimations(model, renderState);

            equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.HUMANOID, equippable.assetId().orElseThrow(), model, stack, poseStack, renderTypeBuffer, packedLight);
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        }
    }

    private record SoldierOnHeadLayer(ClaySoldierOnHeadModel<?> model) implements ICurioRenderer {
        private static SoldierOnHeadLayer claySoldier() {
            return new SoldierOnHeadLayer(ClaySoldierOnHeadModel.createModel());
        }

        @Override
        public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, @NotNull MultiBufferSource renderTypeBuffer, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
            var team = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT);
            if (team == null) {
                return;
            }


            ClayMobTeamManger.getOptional(team, slotContext.entity().registryAccess()).ifPresent(t -> {
                if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
                    model.copyHeadRotation(humanoidModel);
                }
                model.setupAnimSoldierAnim(renderState);

                model.render(poseStack, renderTypeBuffer, packedLight, t.getColor(0, renderState.ageInTicks));
            });
        }
    }*/
}
