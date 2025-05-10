package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.ZombieClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZombieClaySoldierRenderer extends ClaySoldierRenderer {
    public ZombieClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.addLayer(SoldierSuitLayer.zombie(this));
    }

    @Override
    protected void renderModel(AbstractClaySoldierEntity soldier, PoseStack pPoseStack, VertexConsumer vertexConsumer, int pPackedLight, int overlayCords, int color, int alpha) {
        super.renderModel(soldier, pPoseStack, vertexConsumer, pPackedLight, overlayCords, shiftColor(color), alpha);
    }

    public static int shiftColor(int color) {
        int red = Math.clamp((color >> 16 & 255) - 25, 0, 255);
        int green = Math.clamp((color >> 8 & 255) + 25, 0, 255);
        int blue = Math.clamp((color & 255) - 25, 0, 255);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    protected ClayMobTeam getVariantForColor(AbstractClaySoldierEntity claySoldier) {
        if (claySoldier instanceof ZombieClaySoldierEntity zombie) {
            return zombie.getPreviousTeam();
        }
        return super.getVariantForColor(claySoldier);
    }
}
