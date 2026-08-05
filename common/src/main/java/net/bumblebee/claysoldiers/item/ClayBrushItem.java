package net.bumblebee.claysoldiers.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, @NonNull BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide()) {
            ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
            if (getMode(itemInHand) == Mode.POI && entity instanceof ServerPlayer serverPlayer) {
                setPoiPosAfterClick(serverPlayer, itemInHand, pos);
            }
        }
        return false;
    }

    public static void setPoiPosAfterClick(ServerPlayer serverPlayer, ItemStack stack, @Nullable BlockPos pos) {
        if (serverPlayer.isCrouching()) {
            message(serverPlayer, Component.translatable(ClayBrushItem.POI_CLEAR_LANG).withColor(ClayBrushItem.Mode.POI.color));
            setPoiPos(stack, PoiPosInfo.EMPTY);
        } else {
            PoiPosInfo poiInfo = PoiPosInfo.create(pos, null);
            message(serverPlayer, Component.translatable(ClayBrushItem.POI_SET_LANG, poiInfo.shortDisplayName()).withColor(ClayBrushItem.Mode.POI.color));
            setPoiPos(stack, poiInfo);
        }
    }

    public static void withSide(ItemStack stack, @Nullable BlockPos pos, Direction side, @Nullable Player player) {
        var poiInfo = PoiPosInfo.create(pos, side);
        setPoiPos(stack, poiInfo);
        if (player instanceof ServerPlayer serverPlayer) {
            message(serverPlayer, Component.translatable(POI_SET_LANG, poiInfo.shortDisplayName()).withColor(ClayBrushItem.Mode.POI.color));
        }
    }

    public static void setPoiPos(ItemStack stack, @NonNull PoiPosInfo pos) {
        if (pos.isEmpty()) {
            stack.remove(ModDataComponents.POI_POS.get());
        } else {
            stack.set(ModDataComponents.POI_POS.get(), pos);
        }
    }

    public static @NotNull PoiPosInfo getPoiPos(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.POI_POS.get(), PoiPosInfo.EMPTY);
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
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            withSide(itemInHand, context.getClickedPos(), context.getClickedFace(), context.getPlayer());
            return InteractionResult.SUCCESS;
        }

        cycleMode(itemInHand, context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level pLevel, Player player, InteractionHand pUsedHand) {
        ItemStack itemInHand = player.getItemInHand(pUsedHand);
        if (getMode(itemInHand) == Mode.POI && player.isShiftKeyDown()) {
            withSide(itemInHand, null, null, player);
            return InteractionResult.SUCCESS;
        }
        cycleMode(itemInHand, player);
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
            PoiPosInfo poiPos = getPoiPos(stack);
            if (mode == Mode.POI && !poiPos.isEmpty()) {
                name.append(Component.literal(" (").append(poiPos.shortDisplayName()).append(")"));
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
}
