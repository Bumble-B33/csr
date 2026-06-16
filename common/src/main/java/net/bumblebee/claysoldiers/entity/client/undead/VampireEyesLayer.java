package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.function.Predicate;

public class VampireEyesLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final Identifier EYES_LOCATION = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/vampire_eyes.png");
    private static final RenderType VAMPIRE_EYES = RenderTypes.entityTranslucentEmissive(EYES_LOCATION);
    private static final RenderType VAMPIRE_EYES_ACTIVE = RenderTypes.eyes(EYES_LOCATION);

    private final Predicate<AbstractClaySoldierRenderState> shouldEyesGlow;

    public VampireEyesLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer, Predicate<AbstractClaySoldierRenderState> shouldEyesGlow) {
        super(renderer);
        this.shouldEyesGlow = shouldEyesGlow;
    }

    public static VampireEyesLayer forVampiricClayMob(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer) {
        return new VampireEyesLayer(renderer, soldier -> soldier.isNightForVampire);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState claySoldier, float v, float v1) {
        if (shouldEyesGlow.test(claySoldier)) {
            nodeCollector.order(1).submitModel(this.getParentModel(), claySoldier, poseStack, VAMPIRE_EYES_ACTIVE, 0xF00000, OverlayTexture.NO_OVERLAY, -1, null, claySoldier.outlineColor, null);
        } else {
            nodeCollector.order(1).submitModel(this.getParentModel(), claySoldier, poseStack, VAMPIRE_EYES, 0xF00000, OverlayTexture.NO_OVERLAY, ARGB.color(0x7F, 0xFFFFFF), null, claySoldier.outlineColor, null);
        }

    }
}
