package net.bumblebee.claysoldiers.entity.client.accesories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.RenderableAccessoryMap;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class AccessoryRenderLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> implements IAccessoryRenderLayer {
    public final ClaySoldierCapeModel capeModel;
    public final ClaySoldierShieldModel shieldModel;
    public final ItemInHandRenderer itemInHandRenderer;
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;
    private final EquipmentAssetManager equipmentAssetManager;
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final ClaySoldierModel snorkelModel;


    public AccessoryRenderLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRendererParent, EntityModelSet entityModelSet, EquipmentAssetManager equipmentAssetManager, PlayerSkinRenderCache renderCache) {
        super(pRendererParent);
        this.itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        this.modelByType = Util.memoize(type -> SkullBlockRenderer.createModel(entityModelSet, type));
        this.equipmentAssetManager = equipmentAssetManager;
        this.playerSkinRenderCache = renderCache;

        this.shieldModel = new ClaySoldierShieldModel(entityModelSet.bakeLayer(ClaySoldierShieldModel.LAYER_LOCATION));
        this.capeModel = new ClaySoldierCapeModel(entityModelSet.bakeLayer(ClaySoldierCapeModel.LAYER_LOCATION));
        this.snorkelModel = new ClaySoldierModel(entityModelSet.bakeLayer(ClaySoldierSnorkelModel.SNORKEL_LAYER_LOCATION));
    }

    @Override
    public EquipmentAssetManager getEquipmentAssets() {
        return equipmentAssetManager;
    }

    @Override
    public PlayerSkinRenderCache getPlayerSkinRenderCache() {
        return playerSkinRenderCache;
    }

    @Override
    public ClaySoldierModel getSnorkelModel() {
        return snorkelModel;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState claySoldier, float yRot, float xRot) {
        for (var acc : claySoldier.accessoryRenderState.renderableAccessories.entrySet()) {
            RenderableAccessoryMap.submit(acc.getKey(), acc.getValue(), this, poseStack, nodeCollector, packedLight, claySoldier.accessoryRenderState);
        }
    }


    @Nullable
    public SkullModelBase getSkullBase(SkullBlock.Type type) {
        return modelByType.apply(type);
    }

    @Override
    public ClaySoldierModel getSoldierModel() {
        return this.getParentModel();
    }
    @Override
    public ClaySoldierCapeModel getCapeModel() {
        return capeModel;
    }

    @Override
    public ClaySoldierShieldModel getShieldModel() {
        return shieldModel;
    }
}
