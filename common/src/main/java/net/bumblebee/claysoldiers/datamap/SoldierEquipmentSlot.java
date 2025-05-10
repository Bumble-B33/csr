package net.bumblebee.claysoldiers.datamap;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum SoldierEquipmentSlot implements StringRepresentable, KeyableTranslatableProperty {
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.ARMOR, 0, 1, "feet"),
    LEGS(Type.ARMOR, 1, 2, "legs"),
    CHEST(Type.ARMOR, 2, 3, "chest"),
    HEAD(Type.ARMOR, 3, 4, "head"),
    CAPE(Type.ARMOR, 4, 6, "cape"),
    BACKPACK(Type.BACKPACK, 0, 7, "backpack"),
    BACKPACK_PASSIVE(Type.BACKPACK, 1, 8, "backpack_passive");

    public static final String SOLDIER_SLOT_PREFIX = "clay_soldier.slot." + ClaySoldiersCommon.MOD_ID + ".";

    public static final List<SoldierEquipmentSlot> HANDS = List.of(MAINHAND, OFFHAND);
    public static final List<SoldierEquipmentSlot> BACKPACK_SLOTS = List.of(BACKPACK, BACKPACK_PASSIVE);
    // No Slots redirect to Capability equip
    public static final List<SoldierEquipmentSlot> NO_SLOT = List.of();
    public static final List<SoldierEquipmentSlot> CUSTOM_EQUIP = NO_SLOT;

    public static final Codec<SoldierEquipmentSlot> CODEC = StringRepresentable.fromEnum(SoldierEquipmentSlot::values);
    public static final StreamCodec<FriendlyByteBuf, SoldierEquipmentSlot> STREAM_CODEC = CodecUtils.createEnumStreamCodec(SoldierEquipmentSlot.class);
    public static final StreamCodec<FriendlyByteBuf, List<SoldierEquipmentSlot>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, Optional<SoldierEquipmentSlot>> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Optional<SoldierEquipmentSlot> decode(ByteBuf byteBuf) {
            int i = byteBuf.readByte();
            return i == -1 ? Optional.empty() : Optional.of(SoldierEquipmentSlot.values()[i]);
        }

        @Override
        public void encode(ByteBuf o, Optional<SoldierEquipmentSlot> soldierEquipmentSlot) {
            o.writeByte(soldierEquipmentSlot.map(SoldierEquipmentSlot::ordinal).orElse(-1));
        }
    };
    private final Type type;
    private final int index;
    private final int filterFlag;
    private final String name;

    SoldierEquipmentSlot(Type pType, int pIndex, int pFilterFlag, String pName) {
        this.type = pType;
        this.index = pIndex;
        this.filterFlag = pFilterFlag;
        this.name = pName;
    }

    @Override
    public String translatableKey() {
        return SOLDIER_SLOT_PREFIX + getSerializedName();
    }

    public Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * Gets the actual slot index.
     */
    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == Type.ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static Optional<SoldierEquipmentSlot> getFromSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Optional.of(HEAD);
            case CHEST -> Optional.of(CHEST);
            case LEGS -> Optional.of(LEGS);
            case FEET -> Optional.of(FEET);
            case MAINHAND -> Optional.of(MAINHAND);
            case OFFHAND -> Optional.of(OFFHAND);
            case BODY -> Optional.empty();
        };
    }
    @Nullable
    public static EquipmentSlot convertToSlot(SoldierEquipmentSlot soldierEquipmentSlot) {
        return switch (soldierEquipmentSlot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
            case MAINHAND -> EquipmentSlot.MAINHAND;
            case OFFHAND -> EquipmentSlot.OFFHAND;
            default -> null;
        };
    }

    public enum Type {
        HAND,
        ARMOR,
        BACKPACK
    }

    public static final MapCodec<List<SoldierEquipmentSlot>> CODEC_MAPPED = Codec.mapEither(
            CustomSlotType.CODEC.fieldOf("slot"),
            CodecUtils.singularOrPluralCodecOptional(SoldierEquipmentSlot.CODEC, "slot")
    ).xmap(CustomSlotType::getFromEither, CustomSlotType::createEither);


    private enum CustomSlotType implements StringRepresentable {
        CUSTOM_PICK_UP("capability_pick_up", List.of()),
        HANDS("hands", SoldierEquipmentSlot.HANDS),
        BACK_PACKS("backpacks", SoldierEquipmentSlot.BACKPACK_SLOTS);

        public static final Codec<CustomSlotType> CODEC = StringRepresentable.fromEnum(CustomSlotType::values);

        private final String serializedName;
        private final List<SoldierEquipmentSlot> slots;

        CustomSlotType(String serializedName, List<SoldierEquipmentSlot> slots) {
            this.serializedName = serializedName;
            this.slots = slots;
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return serializedName;
        }
        private static List<SoldierEquipmentSlot> getFromEither(Either<CustomSlotType, Set<SoldierEquipmentSlot>> either) {
            if (either.left().isPresent()) {
                return either.left().get().slots;
            }
            return either.right().orElseThrow().stream().toList();
        }
        private static Either<CustomSlotType, Set<SoldierEquipmentSlot>> createEither(List<SoldierEquipmentSlot> slots) {
            if (slots.isEmpty()) {
                return Either.left(CustomSlotType.CUSTOM_PICK_UP);
            }
            if (slots.size() != 2) {
                return Either.right(Set.copyOf(slots));
            }
            if (slots.get(0) == SoldierEquipmentSlot.MAINHAND && slots.get(1) == SoldierEquipmentSlot.OFFHAND
                    || slots.get(0) == SoldierEquipmentSlot.OFFHAND && slots.get(1) == SoldierEquipmentSlot.MAINHAND
            ) {
                return Either.left(CustomSlotType.HANDS);
            }
            return Either.right(Set.copyOf(slots));
        }
    }
}
