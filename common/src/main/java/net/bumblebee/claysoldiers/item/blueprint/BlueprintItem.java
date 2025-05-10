package net.bumblebee.claysoldiers.item.blueprint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.tooltip.BlueprintTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class BlueprintItem extends BlueprintPageItem {
    public static final ResourceLocation MARKING_PROPERTY = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "markings");
    public static final String DESCRIPTION_ID = "item." + ClaySoldiersCommon.MOD_ID + ".blueprint";
    public static final String DESCRIPTION_ID_WITH_STRUCTURE = DESCRIPTION_ID + "_with_structure";
    public static final String STRUCTURE_NAME_LANG = DESCRIPTION_ID + ".structure_name";
    public static final String BLUEPRINT_INVALID_LANG = DESCRIPTION_ID + ".invalid";


    public BlueprintItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        if (!pTooltipFlag.isAdvanced()) {
            return;
        }
        var dataLoc = pStack.get(ModDataComponents.BLUEPRINT_DATA.get());
        if (dataLoc != null) {
            pTooltipComponents.add(Component.translatable(STRUCTURE_NAME_LANG, dataLoc.toString()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            pTooltipComponents.add(Component.translatable(BLUEPRINT_INVALID_LANG).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return !pStack.has(DataComponents.HIDE_TOOLTIP) && !pStack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
                ? Optional.ofNullable(pStack.get(ModDataComponents.BLUEPRINT_DATA.get())).map(BlueprintTooltip::new)
                : Optional.empty();
    }

    /**
     * Creates a Blueprint ItemStack for the given data.
     * @param holder the {@code Holder<Blueprint>} of the data, requires the holder to have a key
     * @return a new Blueprint ItemStack for the given data.
     * @throws IllegalStateException if the Holder does not contain a Key
     */
    public static ItemStack createStackFromData(Holder<BlueprintData> holder) {
        ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
        stack.set(ModDataComponents.BLUEPRINT_DATA.get(), holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Cannot create ItemStack for non-register Date: " + holder)).location());

        stack.set(DataComponents.ITEM_NAME, Component.translatable(DESCRIPTION_ID_WITH_STRUCTURE, holder.value().getDisplayName()));
        stack.set(ModDataComponents.BLUEPRINT_ITEM_DATA.get(), new BlueprintItemData(holder.value().marking()));
        return stack;
    }

    public static void registerProperties(ClayBrushItem.ItemPropertiesFactory factory) {
        factory.register(ModItems.BLUEPRINT.get(), MARKING_PROPERTY,
                (stack, level, entity, seed) -> {
                    var data = stack.get(ModDataComponents.BLUEPRINT_ITEM_DATA.get());
                    return data != null ? data.marking() : 0f;
                });
    }

    public record BlueprintItemData(float marking) {
        public static final Codec<BlueprintItemData> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.FLOAT.fieldOf("marking").forGetter(BlueprintItemData::marking)
        ).apply(in, BlueprintItemData::new));
        public static final StreamCodec<ByteBuf, BlueprintItemData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, BlueprintItemData::marking,
                BlueprintItemData::new
        );
    }
}
