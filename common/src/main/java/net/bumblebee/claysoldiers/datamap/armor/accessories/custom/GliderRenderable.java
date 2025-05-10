package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

public class GliderRenderable implements RenderableAccessory {
    public static final Codec<GliderRenderable> CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(GliderRenderable::new, g -> g.glider);
public static final StreamCodec<RegistryFriendlyByteBuf, GliderRenderable> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).map(GliderRenderable::new, g -> g.glider);

    private final Item glider;

    public GliderRenderable(Item glider) {
        this.glider = glider;
    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling) {
        if (isFalling) {
            pPoseStack.pushPose();
            pPoseStack.translate(-1.2, 0.12, 0);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(90F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

            pPoseStack.scale(1.5f, 1.5f, 1.5f);
            renderedFrom.getItemInHandRenderer().renderItem(claySoldier, glider.getDefaultInstance(), ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();
        }
    }
}
