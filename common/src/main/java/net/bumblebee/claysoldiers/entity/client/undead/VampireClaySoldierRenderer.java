package net.bumblebee.claysoldiers.entity.client.undead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.entity.VampiricClayMob;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

    @Override
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        if (claySoldierEntity instanceof VampiricClayMob vampire) {
            claySoldierRenderState.isNightForVampire = vampire.isNightForVampire();
        }
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
