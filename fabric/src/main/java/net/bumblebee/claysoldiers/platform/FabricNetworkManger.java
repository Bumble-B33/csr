package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricNetworkManger implements INetworkManger {
    private static final List<PayloadData<? , ?>> PAYLOAD_DATA = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf> void forEachClient(Consumer<PayloadData<T, C>> consumer) {
        PAYLOAD_DATA.forEach(d -> consumer.accept((PayloadData<T, C>) d));
    }

    @Override
    public <T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf> void registerS2CPayload(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec, BiConsumer<T, PayloadContext> clientHandler) {
        PAYLOAD_DATA.add(new PayloadData<>(id, codec, clientHandler));
    }

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
