package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.armor.accessories.IAccessoryRenderLayer;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SkullRenderable implements RenderableAccessory {
    public static final Codec<SkullRenderable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(s -> s.headStack),
            ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(s -> s.colorHelper),
            ResolvableProfile.CODEC.optionalFieldOf("profile").forGetter(s -> Optional.ofNullable(s.profile))
    ).apply(in, (head, color, profile) -> new SkullRenderable(head, color, profile.orElse(null))));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkullRenderable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), s -> s.headStack,
            ColorHelper.STREAM_CODEC, s -> s.colorHelper,
            ByteBufCodecs.optional(ResolvableProfile.STREAM_CODEC), s -> Optional.ofNullable(s.profile),
            (head, color, profile) -> new SkullRenderable(head, color, profile.orElse(null))
    );

    private static final float RANDOM_SCALE_FACTOR = 1.1875F;
    private final Item headStack;
    private final ColorHelper colorHelper;
    @Nullable
    private final ResolvableProfile profile;

    public SkullRenderable(Item headStack) {
        this(headStack, ColorHelper.EMPTY, null);
    }


    public SkullRenderable(Item headStack, String playerName) {
        this(headStack, ColorHelper.EMPTY, new ResolvableProfile(Optional.of(playerName), Optional.empty(), new PropertyMap()));
    }
    public SkullRenderable(Item headStack, ColorHelper colorHelper, ResolvableProfile profile) {
        this.headStack = headStack;
        this.colorHelper = colorHelper;
        this.profile = profile;
        if (!colorHelper.isEmpty() && headStack instanceof BlockItem blockItem && !((blockItem.getBlock() instanceof AbstractSkullBlock))) {
            ClaySoldiersCommon.LOGGER.error("SkullRenderer Color only supported for Skulls not blocks");
        }
    }

    @Override
    public void render(IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClaySoldierEntity claySoldier, float pPartialTick, boolean isFalling) {
        Item item = headStack;
        pPoseStack.pushPose();
        if (claySoldier.isBaby()) {
            pPoseStack.translate(0.0F, 0.03125F, 0.0F);
            pPoseStack.scale(0.7F, 0.7F, 0.7F);
            pPoseStack.translate(0.0F, 1.0F, 0.0F);
        }

        renderedFrom.getSoldierModel().getHead().translateAndRotate(pPoseStack);
        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock abstractSkullBlock) {
            pPoseStack.scale(RANDOM_SCALE_FACTOR, -RANDOM_SCALE_FACTOR, -RANDOM_SCALE_FACTOR);

            pPoseStack.translate(-0.5, 0.0, -0.5);
            SkullBlock.Type skyllType = abstractSkullBlock.getType();
            SkullModelBase skullmodelbase = renderedFrom.getSkullBase(skyllType);
            if (skullmodelbase == null) {
                return;
            }

            RenderType rendertype = SkullBlockRenderer.getRenderType(skyllType, profile);

            Entity entity = claySoldier.getVehicle();
            WalkAnimationState walkanimationstate;
            if (entity instanceof LivingEntity livingentity) {
                walkanimationstate = livingentity.walkAnimation;
            } else {
                walkanimationstate = claySoldier.walkAnimation;
            }

            float f3 = walkanimationstate.position(pPartialTick);

            renderSkull(f3, pPoseStack, pBuffer, pPackedLight, skullmodelbase, rendertype, colorHelper, claySoldier, pPartialTick);
        } else {
            CustomHeadLayer.translateToHead(pPoseStack, false);
            renderedFrom.getItemInHandRenderer().renderItem(claySoldier, headStack.getDefaultInstance(), ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pPackedLight);
        }

        pPoseStack.popPose();
    }

    private static void renderSkull(float pMouthAnimation, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight,
                                    SkullModelBase pModel, RenderType pRenderType, ColorHelper colorHelper, LivingEntity soldier, float partialTick) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.0F, 0.5F);

        pPoseStack.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(pRenderType);
        pModel.setupAnim(pMouthAnimation, 180, 0.0F);
        pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, colorHelper.getColor(soldier, partialTick));
        pPoseStack.popPose();
    }
}
