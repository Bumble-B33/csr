package net.bumblebee.claysoldiers.util.color;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;
import java.util.Objects;

public class ColorHelper {
    public static final int DEFAULT_CLAY_COLOR = 0xFFa1a7b1;
    public static final Codec<ColorHelper> CODEC = Codec.either(Codec.INT, Codec.string(4, 7)).comapFlatMap(ColorHelper::getFromEither, ColorHelper::convertToEither);
    public static final StreamCodec<ByteBuf, ColorHelper> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.STRING_UTF8).map(ColorHelper::new, ColorHelper::convertToEither);
    public static final ColorHelper EMPTY = color(-1);
    public static final String JEB_NAME = "jeb_";
    private static final int MAGIC_NUMBER = 25;
    private static final String INT_COLOR_TAG = "color";
    private final int color;
    private final boolean jeb;

    public ColorHelper(int color, boolean jeb) {
        this.color = color;
        this.jeb = jeb;
    }
    private ColorHelper(Either<Integer, String> either) {
        this(either.left().orElse(-1), either.right().isPresent());
    }

    private static DataResult<ColorHelper> getFromEither(Either<Integer, String> either) {
        if (either.left().isPresent()) {
            return DataResult.success(ColorHelper.color(either.left().get()));
        }
        String name = either.right().orElseThrow();
        if (name.equals(JEB_NAME)) {
            return DataResult.success(ColorHelper.jeb());
        }
        if (name.matches("^#[0-9A-Fa-f]{6}$")) {
            return DataResult.success(ColorHelper.color(Integer.parseInt(name.substring(1), 16)));
        }

        return DataResult.error(() -> "Invalid hex format. Expected format: #RRGGBB");
    }
    private Either<Integer, String> convertToEither() {
        return jeb ? Either.right(JEB_NAME) : Either.left(color);
    }

    public void writeToTag(String key, CompoundTag tag) {
        var tagColor = new CompoundTag();
        if (color >= 0) {
            tagColor.putInt(INT_COLOR_TAG, color);
        }
        if (jeb) {
            tagColor.putBoolean(JEB_NAME, true);
        }
        tag.put(key, tagColor);
    }
    public static ColorHelper getFromTag(String key, CompoundTag tag) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return EMPTY;
        }

        var colorTag = tag.getCompound(key);
        return new ColorHelper(
                colorTag.contains(INT_COLOR_TAG, Tag.TAG_ANY_NUMERIC) ? colorTag.getInt(INT_COLOR_TAG) : -1,
                colorTag.contains(JEB_NAME)
        );
    }

    /**
     * Creates a new ColorHelper with a rainbow chaining color.
     */
    public static ColorHelper jeb() {
        return new ColorHelper(Either.right(JEB_NAME));
    }

    /**
     * Creates a new static color.
     */
    public static ColorHelper color(int color) {
        return new ColorHelper(Either.left(color));
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
    public int getColor(int offset, int tickCount, float pPartialTicks) {
        if (jeb) {
            int k = tickCount / MAGIC_NUMBER + offset;
            int colorValues = DyeColor.values().length;
            int colorIdMin = k % colorValues;
            int colorIdMax = (k + 1) % colorValues;
            float lerp = ((float) (tickCount % MAGIC_NUMBER) + pPartialTicks) / MAGIC_NUMBER;
            int min = Sheep.getColor(DyeColor.byId(colorIdMin));
            int max = Sheep.getColor(DyeColor.byId(colorIdMax));
            return FastColor.ARGB32.lerp(lerp, min, max);
        }
        return color;
    }
    public boolean isEmpty() {
        return !jeb && color <= -1;
    }
    public boolean isJeb() {
        return jeb;
    }
    public boolean hasStaticColor() {
        return color >= 0;
    }

    public int[] covertToRgb() {
        if (color <= -1) {
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
        if (color <= -1 ) {
            return new ColorHelper(colorHelper.color, jeb || colorHelper.jeb);
        }
        if (colorHelper.color <= -1) {
            return new ColorHelper(color, jeb || colorHelper.jeb);
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

        return new ColorHelper(getColorInt(newColorArray, (float) heightsValueOfEveryColorArray), jeb);
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
