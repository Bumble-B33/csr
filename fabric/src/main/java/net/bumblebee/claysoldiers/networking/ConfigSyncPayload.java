package net.bumblebee.claysoldiers.networking;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.FabricCommonHooks;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigSyncPayload(boolean blueprintEnabled, long hamsterWheelSpeed) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_config"));
    public static final StreamCodec<ByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ConfigSyncPayload::blueprintEnabled,
            ByteBufCodecs.VAR_LONG, ConfigSyncPayload::hamsterWheelSpeed,
            ConfigSyncPayload::new
    );

    public void handleClient(ClientPlayNetworking.Context context) {
        FabricCommonHooks.setBlueprintEnabled(blueprintEnabled);
        ClaySoldierFabric.hamsterWheelSpeed = hamsterWheelSpeed;
        ClaySoldiersCommon.LOGGER.info("Config: Blueprint Feature is {} on client", (blueprintEnabled ? "enabled" : "disabled"));
    }

    @Override
    public Type<? extends ConfigSyncPayload> type() {
        return ID;
    }


}
