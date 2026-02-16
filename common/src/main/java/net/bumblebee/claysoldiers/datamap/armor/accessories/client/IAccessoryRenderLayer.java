package net.bumblebee.claysoldiers.datamap.armor.accessories.client;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierCapeModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierShieldModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.world.level.block.SkullBlock;

public interface IAccessoryRenderLayer {
    ClaySoldierModel getSoldierModel();
    ClaySoldierCapeModel getCapeModel();
    ClaySoldierShieldModel getShieldModel();
    SkullModelBase getSkullBase(SkullBlock.Type type);
    EquipmentAssetManager getEquipmentAssets();
    PlayerSkinRenderCache getPlayerSkinRenderCache();
    ClaySoldierModel getSnorkelModel();
}
