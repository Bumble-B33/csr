package net.bumblebee.claysoldiers.entity.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.undead.SoldierSuitLayer;
import net.bumblebee.claysoldiers.entity.client.undead.VampireClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.undead.VampireEyesLayer;
import net.bumblebee.claysoldiers.entity.client.undead.ZombieClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import java.util.EnumMap;
import java.util.List;

public class BossClaySoldierRenderer extends ClaySoldierRenderer {
    public BossClaySoldierRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        addLayer(new TypeBasedRenderLayer(this));
    }

    @Override
    protected void renderModel(AbstractClaySoldierEntity soldier, PoseStack pPoseStack, VertexConsumer vertexConsumer, int pPackedLight, int overlayCords, int color, int alpha) {
        int newColor = color;
        if (soldier instanceof BossClaySoldierEntity bossSoldier) {
            switch (bossSoldier.getBossType()) {
                case ZOMBIE -> newColor = ZombieClaySoldierRenderer.shiftColor(newColor);
                case VAMPIRE -> newColor = VampireClaySoldierRenderer.shiftColor(newColor);
            }
        }

        super.renderModel(soldier, pPoseStack, vertexConsumer, pPackedLight, overlayCords, newColor, alpha);
    }

    private static class TypeBasedRenderLayer extends RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel> {
        private final EnumMap<BossClaySoldierEntity.BossTypes, List<RenderLayer<AbstractClaySoldierEntity, ClaySoldierModel>>> typeRenderLayer = new EnumMap<>(BossClaySoldierEntity.BossTypes.class);

        public TypeBasedRenderLayer(RenderLayerParent<AbstractClaySoldierEntity, ClaySoldierModel> renderer) {
            super(renderer);
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.NORMAL, List.of());
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.ZOMBIE, List.of(SoldierSuitLayer.zombie(renderer)));
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.VAMPIRE, List.of(
                    SoldierSuitLayer.vampire(renderer),
                    new VampireEyesLayer(renderer, s -> true)
            ));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClaySoldierEntity soldier, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            typeRenderLayer.get(((BossClaySoldierEntity) soldier).getBossType()).forEach(render -> render.render(poseStack, multiBufferSource, packedLight, soldier, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch));
        }
    }
}
