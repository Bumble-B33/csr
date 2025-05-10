package net.bumblebee.claysoldiers.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

public class ClayBrushItem extends Item {
    public static final ResourceLocation MODE_PROPERTY = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "mode");
    public static final String POI_SET_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.poi.set";
    public static final String POI_CLEAR_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.poi.clear";
    public static final String NO_MODE_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.mode.no_mode";

    public ClayBrushItem(Properties pProperties) {
        super(pProperties.component(ModDataComponents.CLAY_BRUSH_MODE.get(), Mode.COMMAND).component(ModDataComponents.CLAY_BRUSH_POI.get(), PoiPos.EMPTY));
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!pLevel.isClientSide) {
            ItemStack itemInHand = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            if (getMode(itemInHand) == Mode.POI && pPlayer instanceof ServerPlayer serverPlayer) {
                if (pPlayer.isCrouching()) {
                    message(serverPlayer, Component.translatable(POI_CLEAR_LANG).withColor(Mode.POI.color));
                    setPoiPos(itemInHand, null);
                } else {
                    message(serverPlayer, Component.translatable(POI_SET_LANG, pPos.toShortString()).withColor(Mode.POI.color));
                    setPoiPos(itemInHand, pPos);
                }
            }
        }

        return false;
    }

    private static void setPoiPos(ItemStack stack, @Nullable BlockPos pos) {
        stack.set(ModDataComponents.CLAY_BRUSH_POI.get(), pos == null ? PoiPos.EMPTY : new PoiPos(pos, false));
    }

    private void cycleMode(ItemStack itemInHand, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Mode newMode = switch (itemInHand.get(ModDataComponents.CLAY_BRUSH_MODE.get())) {
                case COMMAND -> Mode.WORK;
                case WORK -> Mode.POI;
                case POI -> Mode.COMMAND;
                case null -> Mode.COMMAND;
            };
            itemInHand.set(ModDataComponents.CLAY_BRUSH_MODE.get(), newMode);
            message(serverPlayer, newMode.getDisplayName());
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack itemInHand = pContext.getItemInHand();
        cycleMode(itemInHand, pContext.getPlayer());
        return InteractionResult.sidedSuccess(pContext.getLevel().isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        cycleMode(pPlayer.getItemInHand(pUsedHand), pPlayer);
        return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), pLevel.isClientSide());
    }

    private static void message(ServerPlayer pPlayer, Component pMessageComponent) {
        pPlayer.sendSystemMessage(pMessageComponent, true);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        var mode = getMode(pStack);
        if (mode != null) {
            MutableComponent name = mode.getMutableDisplayName();
            PoiPos poiPos = pStack.get(ModDataComponents.CLAY_BRUSH_POI.get());
            if (mode == Mode.POI && poiPos != null && !poiPos.isEmpty()) {
                name.append(Component.literal(" (" + poiPos.pos.toShortString() + ")"));
            }
            pTooltipComponents.add(name);
        } else {
            pTooltipComponents.add(Component.translatable(NO_MODE_LANG).withStyle(ChatFormatting.RED));
        }
    }

    @Nullable
    public static Mode getMode(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_BRUSH_MODE.get());
    }

    public static BlockPos getPoiPos(ItemStack stack) {
        var poiPos = stack.get(ModDataComponents.CLAY_BRUSH_POI.get());
        return poiPos == null || poiPos.isEmpty() ? null : poiPos.pos;
    }

    public enum Mode implements StringRepresentable, KeyableTranslatableProperty {
        COMMAND(0, "command", 0, 0xc15a36),
        WORK(1, "work", 0.5f, 0x9a2323),
        POI(2, "poi", 1, 0x1a3bb3);

        public static final IntFunction<Mode> BY_ID = ByIdMap.continuous(mode -> mode.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, mode -> mode.id);
        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        private static final String LANG_KEY = "item.%s.clay_rush.mode.%s";

        private final int id;
        private final String serializedName;
        private final float overrideProperty;
        private final int color;

        Mode(int id, String serializedName, float overrideProperty, int color) {
            this.id = id;
            this.serializedName = serializedName;
            this.overrideProperty = overrideProperty;
            this.color = color;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public float getOverrideProperty() {
            return overrideProperty;
        }

        @Override
        public String translatableKey() {
            return LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, serializedName);
        }

        @Override
        public Style getStyle() {
            return Style.EMPTY.withColor(color);
        }
        private MutableComponent getMutableDisplayName() {
            return Component.translatable(translatableKey()).withStyle(getStyle());
        }
    }

    public record PoiPos(BlockPos pos, boolean isEmpty) {
        public static final PoiPos EMPTY = new PoiPos(BlockPos.ZERO, true);
        public static final Codec<PoiPos> CODEC = RecordCodecBuilder.create(in -> in.group(
                BlockPos.CODEC.optionalFieldOf("pos", BlockPos.ZERO).forGetter(PoiPos::pos),
                Codec.BOOL.optionalFieldOf("empty", true).forGetter(PoiPos::isEmpty)
        ).apply(in, PoiPos::new));
        public static final StreamCodec<ByteBuf, PoiPos> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, PoiPos::pos, ByteBufCodecs.BOOL, PoiPos::isEmpty, PoiPos::new);
    }

    public static void registerProperties(ItemPropertiesFactory factory) {
        factory.register(ModItems.CLAY_BRUSH.get(), MODE_PROPERTY,
                (stack, level, entity, seed) -> Objects.requireNonNullElse(stack.get(ModDataComponents.CLAY_BRUSH_MODE.get()), Mode.COMMAND).overrideProperty);
    }

    public interface ItemPropertiesFactory {
        void register(Item item, ResourceLocation name, ClampedItemPropertyFunction property);
    }
}
