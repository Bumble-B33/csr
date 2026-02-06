package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.datamap.armor.accessories.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GliderRenderable implements RenderableAccessory {
    public static final Codec<GliderRenderable> CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(GliderRenderable::new, g -> g.glider);
public static final StreamCodec<RegistryFriendlyByteBuf, GliderRenderable> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).map(GliderRenderable::new, g -> g.glider);

    private final Item glider;
    private final ItemStack gliderStack;

    public GliderRenderable(Item glider) {
        this.glider = glider;
        this.gliderStack = glider.getDefaultInstance();
    }

    @Override
    public void submit(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pPackedLight, AccessoryRenderState claySoldier) {
        if (claySoldier.isFalling && !claySoldier.isInWater) {
            pPoseStack.pushPose();
            pPoseStack.translate(-1.2, 0.12, 0);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(90F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90F));

            pPoseStack.scale(1.5f, 1.5f, 1.5f);
            claySoldier.gliderAccessory.submit(pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, claySoldier.outlineColor);
            pPoseStack.popPose();
        }
    }

    public ItemStack getGliderStack() {
        return gliderStack;
    }
}
