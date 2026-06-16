package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class SoldierSuitLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final RenderType ZOMBIE_SUIT = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/zombie.png"));
    private static final RenderType VAMPIRE_SUIT = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_suit.png"));

    private final RenderType suit;

    public SoldierSuitLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer, RenderType suit) {
        super(pRenderer);
        this.suit = suit;
    }

    public static SoldierSuitLayer vampire(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, VAMPIRE_SUIT);
    }

    public static SoldierSuitLayer zombie(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> pRenderer) {
        return new SoldierSuitLayer(pRenderer, ZOMBIE_SUIT);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState claySoldier, float v, float v1) {
        nodeCollector.order(1).submitModel(this.getParentModel(), claySoldier, poseStack, suit, 0xF00000, LivingEntityRenderer.getOverlayCoords(claySoldier, 0.0F), ARGB.color(0x7F, 0xFFFFFF), null, claySoldier.outlineColor, null);

    }
}
