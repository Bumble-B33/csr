package net.bumblebee.claysoldiers.networking;

import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.FabricConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(long hamsterWheelSpeed, boolean shearBladeRecipeEnabled) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_config"));
    public static final StreamCodec<ByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ConfigSyncPayload::hamsterWheelSpeed,
            ByteBufCodecs.BOOL, ConfigSyncPayload::shearBladeRecipeEnabled,
            ConfigSyncPayload::new
    );

    public void handleClient(ClientPlayNetworking.Context context) {
        FabricConfig.hamsterWheelSpeed = hamsterWheelSpeed;
        FabricConfig.setShearBladeRecipeEnabled(shearBladeRecipeEnabled);;
        FabricConfig.logConfig("On Client Receive", true);
    }

    @Override
    public Type<? extends ConfigSyncPayload> type() {
        return ID;
    }


}
