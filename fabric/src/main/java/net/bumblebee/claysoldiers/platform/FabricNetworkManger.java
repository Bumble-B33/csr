package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FabricNetworkManger extends NetworkManger {

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        var list = PlayerLookup.tracking(entity);
        list.forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    @Override
    public void sendToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
        PlayerLookup.all(level.getServer()).forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, payload));
    }

    @Override
    public void sendToPlayersTrackingBlockEntity(BlockEntity target, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(target)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public boolean hasChannel(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    @Override
    public boolean isMemoryConnection(ServerPlayer player) {
        return player.connection.connection.isMemoryConnection();
    }
}
