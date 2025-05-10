package net.bumblebee.claysoldiers.platform.services;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BiConsumer;

public interface INetworkManger {
    <T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf> void registerS2CPayload(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec, BiConsumer<T, PayloadContext> clientHandler);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);
    void sendToAllPlayers(ServerLevel level, CustomPacketPayload payload);
    void sendToPlayersTrackingBlockEntity(BlockEntity target, CustomPacketPayload payload);

    boolean hasChannel(ServerPlayer player, CustomPacketPayload.Type<?> type);
    boolean isMemoryConnection(ServerPlayer player);

    record PayloadContext(Minecraft client, Player player) {}
    record PayloadData<T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf>(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec, BiConsumer<T, PayloadContext> clientHandler) { }
}
