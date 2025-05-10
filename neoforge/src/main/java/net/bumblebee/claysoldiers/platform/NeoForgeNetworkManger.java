package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NeoForgeNetworkManger implements INetworkManger {
    private static final List<PayloadData<? , ?>> PAYLOAD_DATA = new ArrayList<>();
    public static <T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf> void forEach(Consumer<PayloadData<T, C>> consumer) {
        PAYLOAD_DATA.forEach(d -> consumer.accept((PayloadData<T, C>) d));
    }

    @Override
    public <T extends CustomPacketPayload, C extends RegistryFriendlyByteBuf> void registerS2CPayload(CustomPacketPayload.Type<T> id, StreamCodec<C, T> codec, BiConsumer<T, PayloadContext> clientHandler) {
        PAYLOAD_DATA.add(new PayloadData<>(id, codec, clientHandler));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToAllPlayers(ServerLevel ignored, CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendToPlayersTrackingBlockEntity(BlockEntity target, CustomPacketPayload payload) {
        if (target.getLevel() instanceof ServerLevel level) {
            PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(target.getBlockPos()), payload);
        } else {
            ClaySoldiersCommon.LOGGER.error("Cannot send a Packet to the Server from a Client Side BlockEntity");
        }
    }

    @Override
    public boolean hasChannel(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return player.connection.hasChannel(type);
    }

    @Override
    public boolean isMemoryConnection(ServerPlayer player) {
        return player.connection.getConnection().isMemoryConnection();
    }
}
