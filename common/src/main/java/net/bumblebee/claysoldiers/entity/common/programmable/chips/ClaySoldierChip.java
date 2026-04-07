package net.bumblebee.claysoldiers.entity.common.programmable.chips;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class ClaySoldierChip<T> {
    private final static StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip.Type<?>> TYPE_STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_SOLDIER_MODULES);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaySoldierChip<?> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            var type = TYPE_STREAM_CODEC.decode(registryFriendlyByteBuf);
            return type.decode(registryFriendlyByteBuf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ClaySoldierChip<?> claySoldierChip) {
            claySoldierChip.encode(registryFriendlyByteBuf);
        }
    };
    private final static Codec<ClaySoldierChip.Type<?>> TYPE_CODEC = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.byNameCodec();
    @Nullable
    protected final ProgrammableClaySoldierEntity soldier;
    protected final T data;

    public ClaySoldierChip(@Nullable ProgrammableClaySoldierEntity soldier, T data) {
        this.soldier = soldier;
        this.data = data;
    }

    public ClaySoldierChip<T> withSoldier(ProgrammableClaySoldierEntity soldier) {
        return getType().build(soldier, data);
    }

    public abstract void addGoals(ServerLevel level, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder);

    public @Nullable ResourceLocation assetId() {
        return null;
    }

    public Component info() {
        return getType().getDisplayName();
    }

    public abstract Type<T> getType();

    public String asString() {
        return this.getClass().getSimpleName();
    }

    public void save(ValueOutput output) {
        output.store("chip_type", TYPE_CODEC, getType());
        output.store("chip_data", getType().dataCodec, data);
    }

    public void encode(RegistryFriendlyByteBuf byteBuf) {
        TYPE_STREAM_CODEC.encode(byteBuf, getType());
        getType().streamCodec.encode(byteBuf, data);
    }

    public static @Nullable ClaySoldierChip<?> load(ProgrammableClaySoldierEntity soldier, ValueInput input) {
        Type<?> type = input.read("chip_type", TYPE_CODEC).orElse(null);
        if (type == null) {
            return null;
        }

        return type.load(soldier, input);
    }

    public static class Type<T> {
        private final BiFunction<@Nullable ProgrammableClaySoldierEntity, T, ClaySoldierChip<T>> factory;
        private final Codec<T> dataCodec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
        @Nullable
        private String descriptionId;
        private Holder.Reference<Type<?>> holder;

        public Type(BiFunction<@Nullable ProgrammableClaySoldierEntity, T, ClaySoldierChip<T>> factory, Codec<T> dataCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
            this.factory = factory;
            this.dataCodec = dataCodec;
            this.streamCodec = streamCodec;

        }

        public ClaySoldierChip<T> build(@Nullable ProgrammableClaySoldierEntity soldier, T data) {
            return factory.apply(soldier, data);
        }

        public @Nullable ClaySoldierChip<T> load(ProgrammableClaySoldierEntity soldier, ValueInput input) {
            T data = input.read("chip_data", dataCodec).orElse(null);
            if (data == null) {
                return null;
            }
            return build(soldier, data);
        }

        public String getDescriptionId() {
            if (descriptionId == null) {
                descriptionId = Util.makeDescriptionId("clay_soldier_module", getOrCreateReference().key().location());
            }
            return descriptionId;
        }

        public Component getDisplayName() {
            return Component.translatable(getDescriptionId());
        }

        private Holder.Reference<Type<?>> getOrCreateReference() {
            if (holder == null) {
                holder = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.get(
                        ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.getKey(this)
                        ).orElseThrow();
            }
            return holder;
        }

        public ClaySoldierChip<T> decode(RegistryFriendlyByteBuf buf) {
            return build(null, streamCodec.decode(buf));
        }
    }
}
