package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.accessories.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class StringRenderLayer implements RenderableAccessory {
    public static final Codec<StringRenderLayer> CODEC = ResourceLocation.CODEC.xmap(StringRenderLayer::new, s -> s.textureLocation);
    public static final StreamCodec<ByteBuf, StringRenderLayer> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(StringRenderLayer::new, s -> s.textureLocation);

    private final ResourceLocation textureLocation;
    private RenderType renderType = null;

    public StringRenderLayer() {
        this(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/wrapped.png"));
    }
    public StringRenderLayer(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier) {
        if (renderType == null) {
            renderType = RenderType.entityTranslucent(textureLocation);
        }
        nodeCollector.submitModel(
                renderedFrom.getSoldierModel(),
                (AbstractClaySoldierRenderState) claySoldier.renderStateFrom,
                pPoseStack,
                renderType,
                pPackedLight,
                claySoldier.overlayCords0,
                claySoldier.outlineColor,
                null
        );
    }
}
