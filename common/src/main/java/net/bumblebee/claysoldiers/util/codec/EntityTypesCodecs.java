package net.bumblebee.claysoldiers.util.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.ClaySoldierLike;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class EntityTypesCodecs {
    public static final Codec<EntityType<? extends AbstractClaySoldierEntity>> CLAY_SOLDIER_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().comapFlatMap(EntityTypesCodecs::castSoldier, Function.identity());
    private static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> ENTITY_TYPE_STREAM_CODEC = ByteBufCodecs.registry(Registries.ENTITY_TYPE);

    private static final Set<EntityType<? extends AbstractClaySoldierEntity>> claySoldierTypes = new HashSet<>();
    private static final Set<EntityType<? extends ClayMobEntity>> claySoldierLikes = new HashSet<>();

    public static <T extends ClayMobEntity & ClaySoldierLike> Codec<EntityType<T>> createClaySoldierLikeCodec() {
        return BuiltInRegistries.ENTITY_TYPE.byNameCodec().comapFlatMap(EntityTypesCodecs::castClaySoldierLike, Function.identity());
    }

    public static <T extends ClayMobEntity & ClaySoldierLike> StreamCodec<RegistryFriendlyByteBuf, EntityType<T>> createClaySoldierLikeStream() {
        return new StreamCodec<>() {
            @Override
            @SuppressWarnings("unchecked")
            public EntityType<T> decode(RegistryFriendlyByteBuf byteBuf) {
                var type = ENTITY_TYPE_STREAM_CODEC.decode(byteBuf);
                return (EntityType<T>) castClaySoldierLike(type).getOrThrow();
            }

            @Override
            public void encode(RegistryFriendlyByteBuf o, EntityType<T> type) {
                ENTITY_TYPE_STREAM_CODEC.encode(o, type);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static DataResult<EntityType<? extends AbstractClaySoldierEntity>> castSoldier(EntityType<?> type) {
        init();
        if (claySoldierTypes.contains(type)) {
            return DataResult.success((EntityType<? extends AbstractClaySoldierEntity>) type);
        }
        return DataResult.error(() -> type + " is not a ClaySoldier");
    }

    @SuppressWarnings("unchecked")
    private static <T extends ClayMobEntity & ClaySoldierLike> DataResult<EntityType<T>> castClaySoldierLike(EntityType<?> type) {
        init();
        if (claySoldierLikes.contains(type)) {
            return DataResult.success((EntityType<T>) type);
        }
        return DataResult.error(() -> type + " is not a ClaySoldierLike");
    }

    private static void init() {
        if (claySoldierTypes.isEmpty()) {
            addClaySoldier(ModEntityTypes.CLAY_SOLDIER_ENTITY.get());
            addClaySoldier(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get());
            addClaySoldier(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get());
            addClaySoldier(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get());
            addClaySoldier(ModEntityTypes.PROGRAMMABLE_CLAY_SOLDIER_ENTITY.get());

            addClaySoldierLike(ModEntityTypes.CLAY_WRAITH.get());
        }
    }

    public static void addClaySoldier(EntityType<? extends AbstractClaySoldierEntity> entityType) {
        claySoldierTypes.add(entityType);
        addClaySoldierLike(entityType);
    }

    public static <T extends ClayMobEntity & ClaySoldierLike> void addClaySoldierLike(EntityType<T> entityType) {
        claySoldierLikes.add(entityType);
    }

    private EntityTypesCodecs() {
    }
}
