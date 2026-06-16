package net.bumblebee.claysoldiers.entity.client.undead;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.ZombieClaySoldierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZombieClaySoldierRenderer extends ClaySoldierRenderer {
    public ZombieClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.addLayer(SoldierSuitLayer.zombie(this));
    }

    @Override
    protected int getColor(AbstractClaySoldierRenderState soldier) {
        return shiftColor(super.getColor(soldier));
    }

    @Override
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        if (claySoldierEntity instanceof ZombieClaySoldierEntity zombie) {
            claySoldierRenderState.hasPreviousTeamColor = !zombie.getPreviousTeam().getColor().isEmpty();
            claySoldierRenderState.previousTeamColor = zombie.getPreviousTeam().getColor(claySoldierEntity, partialTick);
        }
    }

    public static int shiftColor(int color) {
        int red = Math.clamp((color >> 16 & 255) - 25, 0, 255);
        int green = Math.clamp((color >> 8 & 255) + 25, 0, 255);
        int blue = Math.clamp((color & 255) - 25, 0, 255);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    protected int getVariantForColor(AbstractClaySoldierRenderState claySoldier) {
        return claySoldier.hasPreviousTeamColor ? claySoldier.previousTeamColor : super.getVariantForColor(claySoldier);
    }
}
