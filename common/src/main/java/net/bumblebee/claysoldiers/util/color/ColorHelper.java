package net.bumblebee.claysoldiers.util.color;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;
import java.util.Objects;

public class ColorHelper {
    public static final int DEFAULT_CLAY_COLOR = 0xFFa1a7b1;
    public static final int NO_COLOR = -1;
    public static final Codec<ColorHelper> CODEC = Codec.either(Codec.INT, Codec.string(4, 7)).comapFlatMap(ColorHelper::getFromEither, ColorHelper::convertToEither);
    public static final StreamCodec<ByteBuf, ColorHelper> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ColorHelper decode(ByteBuf byteBuf) {
            if (!byteBuf.readBoolean()) {
                return EMPTY;
            }

            return new ColorHelper(byteBuf.readInt(), byteBuf.readBoolean(), false);
        }

        @Override
        public void encode(ByteBuf buf, ColorHelper colorHelper) {
            if (colorHelper.empty) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeInt(colorHelper.color);
                buf.writeBoolean(colorHelper.jeb);
            }
        }
    };

    public static final ColorHelper CLAY_COLOR = new ColorHelper(DEFAULT_CLAY_COLOR, false, false);
    public static final ColorHelper JEB =  new ColorHelper(NO_COLOR, true, false);

    public static final ColorHelper EMPTY = new ColorHelper(NO_COLOR, false, true);

    public static final String JEB_NAME = "jeb_";
    private final int color;
    private final boolean jeb;
    private final boolean empty;

    private ColorHelper(int color, boolean jeb, boolean empty) {
        this.color = empty ? NO_COLOR : color | 0xFF000000;
        this.jeb = jeb;
        this.empty = empty || (color == NO_COLOR && !jeb);
    }

    private static DataResult<ColorHelper> getFromEither(Either<Integer, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(ColorHelper.color(either.left().get()));
        }
        String name = either.right().orElseThrow();
        if (name.equals(JEB_NAME)) {
            return DataResult.success(JEB);
        }
        if (name.matches("^#[0-9A-Fa-f]{6}$")) {
            return DataResult.success(ColorHelper.color(Integer.parseInt(name.substring(1), 16)));
        }

        return DataResult.error(() -> "Invalid hex format. Expected format: #RRGGBB");
    }
    private Either<Integer, String> convertToEither() {
        return jeb ? Either.right(JEB_NAME) : Either.left(color);
    }

    /**
     * Creates a new ColorHelper with a rainbow chaining color.
     */
    public static ColorHelper jeb() {
        return JEB;
    }

    /**
     * Creates a new static color.
     */
    public static ColorHelper color(int color) {
        return new ColorHelper(color, false, false);
    }

    @Override
    public String toString() {
        return "Color(" + color + (jeb ? ",jeb)" : ")");
    }

    private String format() {
        if (jeb) {
            return "Jeb_";
        }
        return String.format("#%06x", (color & 0x00ffffff)).toUpperCase(Locale.ROOT);
    }
    /**
     * Formats this ColorHelper to hex code and applies its color.
     */
    public Component formatDynamic(LivingEntity entity) {
        return Component.literal(format()).withColor(getColor(entity, 0));
    }

    /**
     * Returns the dynamic color. This includes all color changing effects.
     */
    public int getColor(LivingEntity entity, float pPartialTicks) {
        return getColor(entity.getId(), entity.tickCount, pPartialTicks);
    }

    /**
     * Returns the dynamic color. This includes all color changing effects.
     */
    public int getColor(int offset, float ageInTicks) {
        if (jeb) {
            return ColorLerper.getLerpedColor(ColorLerper.Type.SHEEP, offset + ageInTicks);
        }
        return color;
    }

    /**
     * Returns the dynamic color. This includes all color changing effects.
     */
    public int getColor(int offset, int tickCount, float pPartialTicks) {
        return getColor(offset, tickCount + pPartialTicks);

    }
    public boolean isEmpty() {
        return empty;
    }
    public boolean isJeb() {
        return jeb;
    }
    public boolean hasStaticColor() {
        return color >= 0;
    }

    public int[] covertToRgb() {
        if (isEmpty()) {
            return new int[0];
        }

        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);
        return new int[]{red, green, blue};
    }

    /**
     * Returns the color without any color changing effects applied.
     */
    public int getColorStatic() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorHelper that = (ColorHelper) o;
        return color == that.color && jeb == that.jeb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, jeb);
    }

    /**
     * Adds the other Color onto this one
     * @param colorHelper the other Color to add.
     * @return the combined Color.
     */
    public ColorHelper addColor(ColorHelper colorHelper) {
        if (isEmpty()) {
            return colorHelper;
        }
        if (colorHelper.isEmpty()) {
            return this;
        }

        return dyeColorHelper(colorHelper.covertToRgb(), jeb || colorHelper.jeb);
    }

    private ColorHelper dyeColorHelper(int[] colorAdded, boolean jeb) {
        int[] newColorArray = new int[3];
        int heightsValueOfEveryColorArray = 0;

        final int[] currentColor = covertToRgb();
        final int soldierRed = currentColor[0];
        final int soldierGreen = currentColor[1];
        final int soldierBlue = currentColor[2];

        heightsValueOfEveryColorArray += Math.max(soldierRed, Math.max(soldierGreen, soldierBlue));

        newColorArray[0] += soldierRed;
        newColorArray[1] += soldierGreen;
        newColorArray[2] += soldierBlue;

        final int dyeRed = colorAdded[0];
        final int dyeGreen = colorAdded[1];
        final int dyeBlue = colorAdded[2];

        heightsValueOfEveryColorArray += Math.max(dyeRed, Math.max(dyeGreen, dyeBlue));
        newColorArray[0] += dyeRed;
        newColorArray[1] += dyeGreen;
        newColorArray[2] += dyeBlue;

        return new ColorHelper(getColorInt(newColorArray, (float) heightsValueOfEveryColorArray), jeb, false);
    }

    private static int getColorInt(int[] newColorArray, float heightsValueOfEveryColorArray) {
        int newRed = newColorArray[0] / 2;
        int newGreen = newColorArray[1] / 2;
        int newBlue = newColorArray[2] / 2;

        final float averageMaxValueOfEveryColor = heightsValueOfEveryColorArray / 2;
        final float maxAverageValue = (float) Math.max(newRed, Math.max(newGreen, newBlue));

        newRed = (int) ((float) newRed * averageMaxValueOfEveryColor / maxAverageValue);
        newGreen = (int) ((float) newGreen * averageMaxValueOfEveryColor / maxAverageValue);
        newBlue = (int) ((float) newBlue * averageMaxValueOfEveryColor / maxAverageValue);
        int colorInt = (newRed << 8) + newGreen;
        colorInt = (colorInt << 8) + newBlue;
        return colorInt;
    }
}
