package net.bumblebee.claysoldiers.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class ThrowableTransform {
    public static final Identifier SWEET_BERRY_MODEL = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "thrown_sweet_berry");
    public static final Identifier GLOW_BERRY_MODEL = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "thrown_glow_berry");

    public static final ThrowableTransform DEFAULT = new ThrowableTransform(null);
    public static final ThrowableTransform SWEET_BERRY = ThrowableTransform.of(SWEET_BERRY_MODEL);
    public static final ThrowableTransform GLOW_BERRY = ThrowableTransform.of(GLOW_BERRY_MODEL);

    public static final Codec<ThrowableTransform> CODEC = RecordCodecBuilder.create(in -> in.group(
            Identifier.CODEC.optionalFieldOf("newModel").forGetter(ThrowableTransform::newModel)
    ).apply(in, m -> new ThrowableTransform(m.orElse(null))));
    public static final StreamCodec<ByteBuf, ThrowableTransform> STREAM_CODEC = ByteBufCodecs.optional(Identifier.STREAM_CODEC).map(m -> ThrowableTransform.create(m.orElse(null)), ThrowableTransform::newModel);
    private final Identifier newModel;

    private ThrowableTransform(@Nullable Identifier newModel) {
        this.newModel = newModel;
    }

    private static ThrowableTransform create(@Nullable Identifier newModel) {
        if (newModel == null) {
            return DEFAULT;
        }
        return new ThrowableTransform(newModel);
    }

    public static ThrowableTransform of(@NonNull Identifier model) {
        Objects.requireNonNull(model);
        return new ThrowableTransform(model);
    }

    private Optional<Identifier> newModel() {
        return Optional.ofNullable(newModel);
    }

    public ItemStackWithEffect transform(@NonNull ItemStackWithEffect throwable) {
        if (newModel == null) {
            return throwable;
        }
        var newStack = throwable.copy();

        newStack.stack().set(DataComponents.ITEM_MODEL, newModel);
        return newStack;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ThrowableTransform that = (ThrowableTransform) o;
        return Objects.equals(newModel, that.newModel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(newModel);
    }

    public boolean isEmpty() {
        return this == DEFAULT;
    }
}


