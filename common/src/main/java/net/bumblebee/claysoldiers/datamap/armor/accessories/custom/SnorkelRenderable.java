package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class SnorkelRenderable implements RenderableAccessory {
    public static final ResourceLocation BAMBOO_STICK_TEXTURE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_soldier/bamboo_stick.png");
    public static final Codec<SnorkelRenderable> CODEC = ResourceLocation.CODEC.xmap(SnorkelRenderable::new, s -> s.textureLocation);
    public static final StreamCodec<ByteBuf, SnorkelRenderable> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(SnorkelRenderable::new, s -> s.textureLocation);


    private final ResourceLocation textureLocation;

    public SnorkelRenderable(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling) {
        VertexConsumer bambooStickBuffer = pBuffer.getBuffer(RenderType.entitySolid(textureLocation));
        renderedFrom.getSoldierModel().renderBambooStick(pPoseStack, bambooStickBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
    }
}
