package net.bumblebee.claysoldiers.entity.client.programmable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClayMobAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProgrammableClaySoldierRenderer extends ClaySoldierRenderer {
    public static final Identifier FISHING_ROD_CAST_MODEL = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_fishing_rod_cast");

    private final FishingHookRenderer fishingHookRenderer;
    private final ItemStackRenderState fishingRodItemCast;
    private final ItemStackRenderState fishingRodItemCastWithClint;


    public ProgrammableClaySoldierRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.addLayer(new ClaySoldierChipRenderLayer(this, context.getModelSet()));
        this.fishingHookRenderer = new FishingHookRenderer(context);
        this.fishingRodItemCast = new ItemStackRenderState();
        this.fishingRodItemCastWithClint = new ItemStackRenderState();
        this.addLayer(new ClaySoldierFishingRenderLayer(this, this::getFishingRodModel));
    }

    @Override
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        if (claySoldierEntity instanceof ProgrammableClayMobAccess programmable) {
            var chip = programmable.getInstalledChip();
            if (chip != null) {
                claySoldierRenderState.moduleTexture = chip.assetId();
            }
        }
        if (claySoldierEntity instanceof ProgrammableClaySoldierEntity soldier) {
            if (soldier.isFishingAnker()) {
                claySoldierRenderState.renderCarried = false;
            }
            claySoldierRenderState.isFishingHookEnchanted = claySoldierEntity.getCarriedStack().isEnchanted();
            claySoldierRenderState.isFishingAnker = soldier.isFishingAnker();

            if (soldier.isFishing()) {
                claySoldierRenderState.isFishing = true;
                claySoldierRenderState.fishingHookDis = soldier.getRelativeClientBobberPos();
                claySoldierRenderState.fishingHookRenderState.lineOriginOffset = claySoldierRenderState.fishingHookDis.multiply(-1, -1, -1).add(0, 0.3, 0);
            }
        }

        if (fishingRodItemCast.isEmpty()) {
            ItemStack itemStack = Items.FISHING_ROD.getDefaultInstance();
            itemStack.set(DataComponents.ITEM_MODEL, FISHING_ROD_CAST_MODEL);
            this.itemModelResolver.updateForLiving(fishingRodItemCast, itemStack, ItemDisplayContext.FIXED, claySoldierEntity);

        }
        if (fishingRodItemCastWithClint.isEmpty()) {
            ItemStack itemStack = Items.FISHING_ROD.getDefaultInstance();
            itemStack.set(DataComponents.ITEM_MODEL, FISHING_ROD_CAST_MODEL);
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            this.itemModelResolver.updateForLiving(fishingRodItemCastWithClint, itemStack, ItemDisplayContext.FIXED, claySoldierEntity);

        }
    }

    @Override
    public void submit(AbstractClaySoldierRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
        if (!renderState.isFishing) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(renderState.fishingHookDis);

        fishingHookRenderer.submit(renderState.fishingHookRenderState, poseStack, nodeCollector, cameraRenderState);

        poseStack.popPose();
    }

    public ItemStackRenderState getFishingRodModel(AbstractClaySoldierRenderState renderState) {
        return renderState.isFishingHookEnchanted ? fishingRodItemCastWithClint : fishingRodItemCast;
    }
}
