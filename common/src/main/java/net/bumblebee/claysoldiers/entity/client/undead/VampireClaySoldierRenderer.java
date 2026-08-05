package net.bumblebee.claysoldiers.entity.client.undead;

import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VampireClaySoldierRenderer extends ClaySoldierRenderer {
    public VampireClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.addLayer(SoldierSuitLayer.vampire(this));
        this.addLayer(VampireEyesLayer.forVampiricClayMob(this));
    }

    @Override
    protected int getColor(AbstractClaySoldierRenderState soldier) {
        return shiftColor(super.getColor(soldier));
    }

    public static int shiftColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = (int) (red + (255 - red) * 0.3F);
        green = (int) (green + (255 - green) * 0.3F);
        blue = (int) (blue + (255 - blue) * 0.3F);

        return (red << 16) | (green << 8) | blue;
    }
}
