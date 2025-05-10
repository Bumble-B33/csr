package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CapeRenderable implements RenderableAccessory {
    public static final Codec<CapeRenderable> CODEC = RecordCodecBuilder.create(in -> in.group(
            ResourceLocation.CODEC.fieldOf("texture_location").forGetter(c -> c.textureLocation),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(c -> c.color),
            Codec.BOOL.optionalFieldOf("affectedByOffsetColor", true).forGetter(c -> c.affectedByOffsetColor)
    ).apply(in, CapeRenderable::new));
    public static final StreamCodec<ByteBuf, CapeRenderable> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, c -> c.textureLocation,
            ColorHelper.STREAM_CODEC, c -> c.color,
            ByteBufCodecs.BOOL, c -> c.affectedByOffsetColor,
            CapeRenderable::new
    );

    private final ResourceLocation textureLocation;
    private final ColorHelper color;
    private final boolean affectedByOffsetColor;

    public CapeRenderable(ResourceLocation textureLocation) {
        this(textureLocation, ColorHelper.EMPTY, true);
    }
    public CapeRenderable(ResourceLocation textureLocation, ColorHelper color, boolean affectedByOffsetColor) {
        this.textureLocation = textureLocation;
        this.color = color;
        this.affectedByOffsetColor = affectedByOffsetColor;
    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling) {
        if (textureLocation != null) {
            ItemStack itemstack = claySoldier.getItemBySlot(SoldierEquipmentSlot.CHEST).stack();
            if (!itemstack.is(Items.ELYTRA)) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.0F, 0.0F, 0.125F);
                double d0 = Mth.lerp(pPartialTick, claySoldier.xCloakO, claySoldier.xCloak) - Mth.lerp(pPartialTick, claySoldier.xo, claySoldier.getX());
                double d1 = Mth.lerp(pPartialTick, claySoldier.yCloakO, claySoldier.yCloak) - Mth.lerp(pPartialTick, claySoldier.yo, claySoldier.getY());
                double d2 = Mth.lerp(pPartialTick, claySoldier.zCloakO, claySoldier.zCloak) - Mth.lerp(pPartialTick, claySoldier.zo, claySoldier.getZ());
                float f = Mth.rotLerp(pPartialTick, claySoldier.yBodyRotO, claySoldier.yBodyRot);
                double d3 = Mth.sin(f * (float) (Math.PI / 180.0));
                double d4 = -Mth.cos(f * (float) (Math.PI / 180.0));
                float f1 = (float) d1 * 10.0F;
                f1 = Mth.clamp(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                f2 = Mth.clamp(f2, 0.0F, 150.0F);
                float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                f3 = Mth.clamp(f3, -20.0F, 20.0F);
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                float f4 = Mth.lerp(pPartialTick, claySoldier.oBob, claySoldier.bob);
                f1 += Mth.sin(Mth.lerp(pPartialTick, claySoldier.walkDistO, claySoldier.walkDist) * 6.0F) * 32.0F * f4;
                if (claySoldier.isCrouching()) {
                    f1 += 25.0F;
                }

                pPoseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
                VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(textureLocation));
                final int offsetColor = getColor(claySoldier, pPartialTick);

                if (claySoldier.isInSittingPose()) {
                    pPoseStack.translate(0, 0.5, 0);
                    if (claySoldier.getId() % 2 == 1) {
                        pPoseStack.translate(0, 0.25, -0.2);

                    }
                }
                renderedFrom.getCapeModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, offsetColor);
                pPoseStack.popPose();
            }
        }
    }

    private boolean affectByOffsetColor() {
        return affectedByOffsetColor;
    }

    private int getColor(AbstractClaySoldierEntity soldier, float partialTick) {
        ColorHelper offsetColor = ColorHelper.EMPTY;
        if (affectByOffsetColor()) {
            offsetColor = soldier.getOffsetColor();
        }
        if (offsetColor.isEmpty()) {
            offsetColor = color;
        }
        return soldier.unpackDynamicColor(offsetColor, partialTick);
    }
}
