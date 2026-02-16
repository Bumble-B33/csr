package net.bumblebee.claysoldiers.entity.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.bumblebee.claysoldiers.entity.client.undead.SoldierSuitLayer;
import net.bumblebee.claysoldiers.entity.client.undead.VampireClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.undead.VampireEyesLayer;
import net.bumblebee.claysoldiers.entity.client.undead.ZombieClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
    public void extractRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState claySoldierRenderState, float partialTick) {
        super.extractRenderState(claySoldierEntity, claySoldierRenderState, partialTick);
        if (claySoldierEntity instanceof BossClaySoldierEntity boss) {
            claySoldierRenderState.bossType = boss.getBossType();
        }
    }

    @Override
    protected int getColor(AbstractClaySoldierRenderState soldier) {
        int color = super.getColor(soldier);

        return switch (soldier.bossType) {
            case ZOMBIE -> ZombieClaySoldierRenderer.shiftColor(color);
            case VAMPIRE -> VampireClaySoldierRenderer.shiftColor(color);
            case null, default -> color;
        };
    }

    private static class TypeBasedRenderLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
        private final EnumMap<BossClaySoldierEntity.BossTypes, List<RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel>>> typeRenderLayer = new EnumMap<>(BossClaySoldierEntity.BossTypes.class);

        public TypeBasedRenderLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer) {
            super(renderer);
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.NORMAL, List.of());
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.ZOMBIE, List.of(SoldierSuitLayer.zombie(renderer)));
            typeRenderLayer.put(BossClaySoldierEntity.BossTypes.VAMPIRE, List.of(
                    SoldierSuitLayer.vampire(renderer),
                    new VampireEyesLayer(renderer, s -> true)
            ));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AbstractClaySoldierRenderState renderState, float v, float v1) {
            typeRenderLayer.get(renderState.bossType).forEach(layer -> layer.submit(poseStack, submitNodeCollector, packedLight, renderState, v, v1));
        }
    }
}
