package net.bumblebee.claysoldiers.networking;


import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IClientPayload extends CustomPacketPayload {
    void handleClient(INetworkManger.PayloadContext context);
}
