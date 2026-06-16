package net.bumblebee.claysoldiers.entity.client;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class ClaySoldierRenderer extends AbstractClaySoldierRenderer {
    public static final Identifier VARIANT_1 = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID,
            "textures/entity/clay_soldier/clay_soldier.png");
    public static final Identifier VARIANT_2 = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID,
            "textures/entity/clay_soldier/light_clay_soldier.png");

    public ClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ClaySoldierModel(pContext.bakeLayer(ClaySoldierModel.LAYER_LOCATION)));
    }

    @Override
    public Identifier getTextureLocation(AbstractClaySoldierRenderState pEntity) {
        return pEntity.skinVariantId == 0 ? VARIANT_1 : VARIANT_2;
    }
}
