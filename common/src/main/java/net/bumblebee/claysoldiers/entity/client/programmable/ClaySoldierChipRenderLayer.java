package net.bumblebee.claysoldiers.entity.client.programmable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class ClaySoldierChipRenderLayer extends RenderLayer<AbstractClaySoldierRenderState, ClaySoldierModel> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/module/empty.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entitySolid(TEXTURE_LOCATION);
    private final ClaySoldierChipModel chip;

    public ClaySoldierChipRenderLayer(RenderLayerParent<AbstractClaySoldierRenderState, ClaySoldierModel> renderer, EntityModelSet entityModelSet) {
        super(renderer);
        this.chip = new ClaySoldierChipModel(entityModelSet.bakeLayer(ClaySoldierChipModel.LAYER_LOCATION));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AbstractClaySoldierRenderState renderState, float v, float v1) {
        nodeCollector.submitModel(
                chip,
                renderState,
                poseStack,
                getRenderType(renderState.moduleTexture),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0,
                null
        );
    }

    private static RenderType getRenderType(@Nullable Identifier textureLocation) {
        if (textureLocation == null) {
            return RENDER_TYPE;
        }
        var te = textureLocation.withPrefix("textures/entity/clay_soldier/module/").withSuffix(".png");
        return RenderTypes.entitySolid(te);
    }
}
