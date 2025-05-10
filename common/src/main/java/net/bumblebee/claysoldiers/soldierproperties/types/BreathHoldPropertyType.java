package net.bumblebee.claysoldiers.soldierproperties.types;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BreathHoldPropertyType extends SoldierPropertyType<Integer> {
    private static final Codec<Integer> CODEC = CodecUtils.withReplacementValues(Codec.INT, BreathHoldProperty.CODEC_SPECIAL, BreathHoldProperty.values());

    public static final int MAX_BREATH_HOLD = 1_000;
    public static final int NO_BREATH_HOLD = -200;

    public BreathHoldPropertyType() {
        super(CODEC, ByteBufCodecs.VAR_INT.cast(), BreathHoldProperty.NORMAL.get(), SoldierPropertyTypes.INT_AS_INT, SoldierPropertyTypes.INT_COMBINER, null);
    }

    @Override
    public List<Component> getDisplayNameWithValue(Integer value, @Nullable ClaySoldierInventoryQuery soldier) {
        var name = Component.translatable(this.getDescriptionId()).append(": ");

        if (value >= MAX_BREATH_HOLD) {
            name.append(Component.translatable(BreathHoldProperty.INFINITE.translatableKey()));
        } else if (value <= NO_BREATH_HOLD) {
            name.append(Component.translatable(BreathHoldProperty.NONE.translatableKey()));
        } else {
            name.append(value.toString());
        }

        return List.of(name);
    }

    public enum BreathHoldProperty implements StringRepresentable, Supplier<Integer>, KeyableTranslatableProperty {
        NONE("none", NO_BREATH_HOLD),
        NORMAL("normal", 0),
        INFINITE("infinite", MAX_BREATH_HOLD);

        public static final Codec<BreathHoldProperty> CODEC_SPECIAL = StringRepresentable.fromEnum(BreathHoldProperty::values);

        private final String serializedName;
        private final int amount;

        BreathHoldProperty(String name, int amount) {
            this.serializedName = name;
            this.amount = amount;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public Integer get() {
            return amount;
        }

        @Override
        public String toString() {
            return serializedName;
        }

        @Override
        public String translatableKey() {
            return SoldierPropertyTypes.BREATH_HOLD.get().getDescriptionId() + "." + serializedName;
        }
    }
}
