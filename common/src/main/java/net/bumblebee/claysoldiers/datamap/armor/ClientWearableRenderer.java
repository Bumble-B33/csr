package net.bumblebee.claysoldiers.datamap.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public interface ClientWearableRenderer {
    default void renderTrims(LivingEntity livingEntity, TextureAtlas atlas, Holder<ArmorMaterial> material, PoseStack stack, MultiBufferSource bufferSource, int packedLight, Model model, boolean sprite, float partialTicks) {
        for (var pair : getArmorTrims(livingEntity.registryAccess())) {
            renderTrim(atlas, material, stack, bufferSource, pair.trim(), packedLight, model, sprite, FastColor.ARGB32.color(0xFF, pair.color().getColor(livingEntity, partialTicks)));
        }
    }
    static void renderTrim(TextureAtlas atlas, Holder<ArmorMaterial> material, PoseStack stack, MultiBufferSource bufferSource, ArmorTrim trim, int packedLight, Model model, boolean sprite, int color) {
        TextureAtlasSprite textureatlassprite = atlas
                .getSprite(sprite ? trim.innerTexture(material) : trim.outerTexture(material));
        VertexConsumer vertexconsumer = textureatlassprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
        model.renderToBuffer(stack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    static void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model buffer) {
        buffer.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY);
    }
    static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model, int color, ResourceLocation armorResource) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(armorResource));
        if (color > 0 && color <= 0xFFFFFF) {
            color += 0xFF000000;
        }
        model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    default int getColor(ClaySoldierInventoryQuery soldier, float partialTicks) {
        ColorHelper offsetColor = ColorHelper.EMPTY;
        if (isAffectedByOffsetColor()) {
            offsetColor = soldier.getOffsetColor();
        }
        if (offsetColor.isEmpty()) {
            offsetColor = getColorHelper();
        }
        return soldier.unpackDynamicColor(offsetColor, partialTicks);
    }
    ColorHelper getColorHelper();

    boolean isAffectedByOffsetColor();

    ItemStack getArmorCopyStack();
    Iterable<SoldierWearableEffect.TrimHolder> getArmorTrims(RegistryAccess access);

}
