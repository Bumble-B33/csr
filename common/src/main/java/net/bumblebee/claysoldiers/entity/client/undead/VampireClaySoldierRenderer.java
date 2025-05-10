package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VampireClaySoldierRenderer extends ClaySoldierRenderer {
    public VampireClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.addLayer(SoldierSuitLayer.vampire(this));
        this.addLayer(VampireEyesLayer.forVampiricClayMob(this));
    }

    @Override
    protected void renderModel(AbstractClaySoldierEntity soldier, PoseStack pPoseStack, VertexConsumer vertexConsumer, int pPackedLight, int overlayCords, int color, int alpha) {
        super.renderModel(soldier, pPoseStack, vertexConsumer, pPackedLight, overlayCords, shiftColor(color), alpha);
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
