package net.bumblebee.claysoldiers.entity.client.horse;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ClayPegasusRenderer extends ClayHorseRenderer {

    public ClayPegasusRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.addLayer(new ClayHorseWingsRenderLayer(this, pContext.getModelSet()));
    }
}
