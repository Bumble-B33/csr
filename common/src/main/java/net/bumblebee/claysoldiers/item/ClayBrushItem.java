package net.bumblebee.claysoldiers.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntFunction;

public class ClayBrushItem extends Item {
    public static final String POI_SET_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.poi.set";
    public static final String POI_CLEAR_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.poi.clear";
    public static final String NO_MODE_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_brush.mode.no_mode";

    public ClayBrushItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide()) {
            ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
            if (getMode(itemInHand) == Mode.POI && entity instanceof ServerPlayer serverPlayer) {
                setPoiPosAfterClick(serverPlayer, itemInHand, pos);
            }
        }
        return false;
    }

    public static void setPoiPosAfterClick(ServerPlayer serverPlayer, ItemStack stack, BlockPos pos) {
        if (serverPlayer.isCrouching()) {
            message(serverPlayer, Component.translatable(ClayBrushItem.POI_CLEAR_LANG).withColor(ClayBrushItem.Mode.POI.color));
            setPoiPos(stack, null);
        } else {
            message(serverPlayer, Component.translatable(ClayBrushItem.POI_SET_LANG, pos.toShortString()).withColor(ClayBrushItem.Mode.POI.color));
            setPoiPos(stack, pos);
        }

    }

    private static void setPoiPos(ItemStack stack, @Nullable BlockPos pos) {
        stack.set(ModDataComponents.POI_POS.get(), pos == null ? PoiPos.EMPTY : new PoiPos(pos, false));
    }

    private void cycleMode(ItemStack itemInHand, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Mode newMode = switch (itemInHand.get(ModDataComponents.CLAY_BRUSH_MODE.get())) {
                case COMMAND -> Mode.POI;
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
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        cycleMode(pPlayer.getItemInHand(pUsedHand), pPlayer);
        return InteractionResult.SUCCESS;
    }

    private static void message(ServerPlayer player, Component msg) {
        player.sendSystemMessage(msg, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var mode = getMode(stack);
        if (mode != null) {
            MutableComponent name = mode.getMutableDisplayName();
            PoiPos poiPos = stack.get(ModDataComponents.POI_POS.get());
            if (mode == Mode.POI && poiPos != null && !poiPos.isEmpty()) {
                name.append(Component.literal(" (" + poiPos.pos.toShortString() + ")"));
            }
            tooltipAdder.accept(name);
        } else {
            tooltipAdder.accept(Component.translatable(NO_MODE_LANG).withStyle(ChatFormatting.RED));
        }
    }

    @Nullable
    public static Mode getMode(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_BRUSH_MODE.get());
    }

    @Nullable
    public static BlockPos getPoiPos(ItemStack stack) {
        var poiPos = stack.get(ModDataComponents.POI_POS.get());
        return poiPos == null || poiPos.isEmpty() ? null : poiPos.pos;
    }

    public enum Mode implements StringRepresentable, KeyableTranslatableProperty {
        COMMAND(0, "command", 0, 0xc15a36),
        POI(1, "poi", 1, 0x1a3bb3);

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
}
