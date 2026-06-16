package net.bumblebee.claysoldiers.platform.services;

import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class NetworkManger {
    private final List<PayloadData<? extends IClientPayload,? extends RegistryFriendlyByteBuf>> payloads = new ArrayList<>();

    public <T extends IClientPayload, C extends RegistryFriendlyByteBuf> void registerS2CPayload(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec) {
        payloads.add(new PayloadData<>(id, codec));
    }

    @SuppressWarnings("unchecked")
    public void forEach(Consumer<PayloadData<IClientPayload, RegistryFriendlyByteBuf>> consumer) {
        payloads.forEach(d -> consumer.accept((PayloadData<IClientPayload, RegistryFriendlyByteBuf>) d));
    }

    public abstract void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    public abstract void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);
    public abstract void sendToAllPlayers(ServerLevel level, CustomPacketPayload payload);
    public abstract void sendToPlayersTrackingBlockEntity(BlockEntity target, CustomPacketPayload payload);

    public abstract boolean hasChannel(ServerPlayer player, CustomPacketPayload.Type<?> type);
    public abstract boolean isMemoryConnection(ServerPlayer player);

    public record PayloadContext(Minecraft client, Player player) {}
    public record PayloadData<T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf>(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec) { }
}
