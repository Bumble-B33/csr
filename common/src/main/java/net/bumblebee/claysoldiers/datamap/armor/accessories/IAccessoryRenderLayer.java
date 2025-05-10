package net.bumblebee.claysoldiers.datamap.armor.accessories;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierCapeModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierShieldModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.SkullBlock;

public interface IAccessoryRenderLayer {
    ClaySoldierModel getSoldierModel();
    ClaySoldierCapeModel getCapeModel();
    ClaySoldierShieldModel getShieldModel();
    SkullModelBase getSkullBase(SkullBlock.Type type);
    ItemInHandRenderer getItemInHandRenderer();
    ItemRenderer getItemRenderer();
}
