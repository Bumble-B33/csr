package net.bumblebee.claysoldiers.entity.client.boss;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;

public class VampireBatRenderer extends BatRenderer {
    public VampireBatRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new BatEyesLayer(this));
    }

    private static class BatEyesLayer extends EyesLayer<Bat, BatModel> {
        private static final RenderType PHANTOM_EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/bat_eyes.png"));

        public BatEyesLayer(RenderLayerParent<Bat, BatModel> renderer) {
            super(renderer);
        }

        @Override
        public RenderType renderType() {
            return PHANTOM_EYES;
        }
    }
}
