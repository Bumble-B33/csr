package net.bumblebee.claysoldiers.entity.client.accesories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessorySlot;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AccessoryRenderLayer extends RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel> implements IAccessoryRenderLayer {
    public final ClaySoldierCapeModel capeModel;
    public final ClaySoldierShieldModel shieldModel;
    public final ItemInHandRenderer itemInHandRenderer;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemRenderer itemRenderer;


    public AccessoryRenderLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> pRendererParent, EntityModelSet entityModelSet, ItemInHandRenderer renderer, ItemRenderer itemRenderer) {
        super(pRendererParent);
        this.capeModel = new ClaySoldierCapeModel(entityModelSet.bakeLayer(ClaySoldierCapeModel.LAYER_LOCATION));
        this.itemInHandRenderer = renderer;
        this.skullModels = SkullBlockRenderer.createSkullRenderers(entityModelSet);
        this.shieldModel = new ClaySoldierShieldModel(entityModelSet.bakeLayer(ClaySoldierShieldModel.LAYER_LOCATION));

        this.itemRenderer = itemRenderer;
    }

    @Override
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        Map<SoldierAccessorySlot<?>, RenderableAccessory> map = new HashMap<>();
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var multi = getMulti(claySoldier, slot);
            if (multi != null) {
                map.putAll(multi.getAccessories());
            }
        }
        boolean falling = claySoldier.isFalling();
        for (var acc : map.values()) {
            acc.render(this, pPoseStack, pBuffer, pPackedLight, claySoldier, pPartialTick, falling);
        }
    }

    @Nullable
    public SkullModelBase getSkullBase(SkullBlock.Type type) {
        return skullModels.get(type);
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

    @Override
    public ItemInHandRenderer getItemInHandRenderer() {
        return itemInHandRenderer;
    }

    @Nullable
    private SoldierMultiWearable getMulti(AbstractClaySoldierEntity claySoldier, SoldierEquipmentSlot slot) {
        ItemStackWithEffect stackWithEffect = claySoldier.getItemBySlot(slot);

        if (stackWithEffect == null || stackWithEffect.isEmpty()) {
            return null;
        }
        return stackWithEffect.wearableEffectMap();
    }
}
