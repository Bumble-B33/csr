package net.bumblebee.claysoldiers.networking;

import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.FabricConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(int hamsterWheelSpeed, boolean shearBladeRecipeEnabled, int soldierTransferRate, int chargingPadPlayerRate) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "fabric_config"));
    public static final StreamCodec<ByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConfigSyncPayload::hamsterWheelSpeed,
            ByteBufCodecs.BOOL, ConfigSyncPayload::shearBladeRecipeEnabled,
            ByteBufCodecs.VAR_INT, ConfigSyncPayload::soldierTransferRate,
            ByteBufCodecs.VAR_INT, ConfigSyncPayload::chargingPadPlayerRate,
            ConfigSyncPayload::new
    );

    public void handleClient(ClientPlayNetworking.Context context) {
        FabricConfig.initSynced(hamsterWheelSpeed, shearBladeRecipeEnabled, soldierTransferRate, chargingPadPlayerRate);
        FabricConfig.logConfig("On Client Receive", true);
    }

    @Override
    public Type<? extends ConfigSyncPayload> type() {
        return ID;
    }


}
