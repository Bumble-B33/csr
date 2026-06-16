package net.bumblebee.claysoldiers.entity.client.boss;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class VampireBatRenderer extends BatRenderer {
    public VampireBatRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new BatEyesLayer(this));
    }

    private static class BatEyesLayer extends EyesLayer<BatRenderState, BatModel> {
        private static final RenderType PHANTOM_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/bat_eyes.png"));

        public BatEyesLayer(RenderLayerParent<BatRenderState, BatModel> renderer) {
            super(renderer);
        }

        @Override
        public RenderType renderType() {
            return PHANTOM_EYES;
        }
    }
}
