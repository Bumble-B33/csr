package net.bumblebee.claysoldiers.entity.client;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ClaySoldierRenderer extends AbstractClaySoldierRenderer {
    public static final ResourceLocation VARIANT_1 = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID,
            "textures/entity/clay_soldier/clay_soldier.png");
    public static final ResourceLocation VARIANT_2 = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID,
            "textures/entity/clay_soldier/light_clay_soldier.png");

    public ClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ClaySoldierModel(pContext.bakeLayer(ClaySoldierModel.LAYER_LOCATION)));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClaySoldierEntity pEntity) {
        return pEntity.getSkinVariant() == 0 ? VARIANT_1 : VARIANT_2;
    }
}
