package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.datamap.armor.accessories.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SkullRenderable implements RenderableAccessory {
    public static final Codec<SkullRenderable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(s -> s.headItem),
            ResolvableProfile.CODEC.optionalFieldOf("profile").forGetter(s -> Optional.ofNullable(s.profile))
    ).apply(in, (head, profile) -> new SkullRenderable(head, profile.orElse(null))));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkullRenderable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), s -> s.headItem,
            ByteBufCodecs.optional(ResolvableProfile.STREAM_CODEC), s -> Optional.ofNullable(s.profile),
            (head, profile) -> new SkullRenderable(head, profile.orElse(null))
    );


    private static final float SKULL_SCALE = 1.1875F;
    private final Item headItem;
    private final ItemStack headItemStack;
    @Nullable
    private final SkullBlock.Type type;
    @Nullable
    private final ResolvableProfile profile;


    public SkullRenderable(Item headStack) {
        this(headStack, (ResolvableProfile) null);
    }

    public SkullRenderable(Item headStack, String playerName) {
        this(headStack, new ResolvableProfile(Optional.of(playerName), Optional.empty(), new PropertyMap()));
    }
    public SkullRenderable(Item headStack, ResolvableProfile profile) {
        this.headItem = headStack;
        this.profile = profile;
        this.headItemStack = headItem.getDefaultInstance();
        if (headItemStack.getItem() instanceof BlockItem blockitem && blockitem.getBlock() instanceof AbstractSkullBlock abstractskullblock) {
            this.type = abstractskullblock.getType();
        } else {
            this.type = null;
        }

    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AccessoryRenderState claySoldier) {
        if (!claySoldier.skullAccessory.isEmpty()) {
            pPoseStack.pushPose();
            ClaySoldierModel model = renderedFrom.getSoldierModel();
            model.root().translateAndRotate(pPoseStack);
            model.getHead().translateAndRotate(pPoseStack);
            if (type != null) {
                pPoseStack.scale(SKULL_SCALE, -SKULL_SCALE, -SKULL_SCALE);
                pPoseStack.translate(-0.5, 0.0, -0.5);
                SkullModelBase skullmodelbase = renderedFrom.getSkullBase(type);
                if (skullmodelbase == null) {
                    return;
                }

                RenderType rendertype = SkullBlockRenderer.getRenderType(type, claySoldier.wornHeadProfile);
                SkullBlockRenderer.renderSkull(null, 180.0F, claySoldier.wornHeadAnimationPos, pPoseStack, pBuffer, pPackedLight, skullmodelbase, rendertype);
            } else {
                translateToHead(pPoseStack);
                claySoldier.skullAccessory.render(pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
            }

            pPoseStack.popPose();
        }
    }

    public ItemStack getHeadStack() {
        return headItemStack;
    }

    public static void translateToHead(PoseStack poseStack) {
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
    }
}
