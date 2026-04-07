package net.bumblebee.claysoldiers.entity.client.programmable;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ProgrammableClaySoldierRenderer extends ClaySoldierRenderer {
    public ProgrammableClaySoldierRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.layers.add(new ClaySoldierChipRenderLayer(this, context.getModelSet()));
    }

    @Override
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        if (claySoldierEntity instanceof ProgrammableClaySoldierEntity soldier) {
            var module = soldier.getInstalledModule();
            claySoldierRenderState.moduleTexture = module != null ? module.assetId() : null;
        }
    }
}
