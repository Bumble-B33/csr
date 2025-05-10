package net.bumblebee.claysoldiers.clayremovalcondition;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RemovalConditionContext {
    private final Type type;
    private final ItemStack stack;
    @Nullable
    private final DamageSource sources;
    @Nullable
    private final MovementType movementType;

    public Type getType() {
        return type;
    }

    public ItemStack stack() {
        return stack;
    }

    public @Nullable DamageSource getDamageSource() {
        return sources;
    }

    public @Nullable MovementType getMovementType() {
        return movementType;
    }

    public static RemovalConditionContext useMelee(ItemStack stack) {
        return new RemovalConditionContext(Type.MELEE_ATTACK, stack, null, null);
    }
    public static RemovalConditionContext useRanged(ItemStack stack) {
        return new RemovalConditionContext(Type.RANGED_ATTACK, stack, null, null);
    }
    public static RemovalConditionContext hurt(DamageSource sources, ItemStack stack) {
        return new RemovalConditionContext(Type.HURT, stack, sources, null);
    }
    public static RemovalConditionContext teleportation(ItemStack stack, MovementType movementType) {
        return new RemovalConditionContext(Type.TELEPORT, stack, null, movementType);
    }
    public static RemovalConditionContext fireworkRocket(ItemStack stack) {
        return new RemovalConditionContext(Type.FLIGHT, stack, null, MovementType.TO_SAFETY);
    }
    public static RemovalConditionContext bounce(ItemStack stack) {
        return new RemovalConditionContext(Type.BOUNCE, stack, null, null);
    }

    public RemovalConditionContext(Type type, ItemStack stack, @Nullable DamageSource sources, @Nullable MovementType movementType) {
        this.type = type;
        this.stack = stack;
        this.sources = sources;
        this.movementType = movementType;
    }

    public enum Type implements StringRepresentable {
        RANGED_ATTACK("ranged"),
        MELEE_ATTACK("melee"),
        TELEPORT("teleport"),
        FLIGHT("flight"),
        HURT("hurt"),
        BOUNCE("bounce");

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    public enum MovementType implements StringRepresentable, KeyableTranslatableProperty {
        TO_OWNER("to_owner"),
        TO_TARGET("to_target"),
        TO_SAFETY("to_safety");

        public static final Codec<MovementType> CODEC = StringRepresentable.fromEnum(MovementType::values);
        public static final StreamCodec<FriendlyByteBuf, MovementType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(MovementType.class);
        private static final String LANG_PREFIX = RemovalCondition.COMPONENT_PREFIX + "context.";
        private final String serializedName;

        MovementType(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public String translatableKey() {
            return LANG_PREFIX + serializedName;
        }
    }
}
