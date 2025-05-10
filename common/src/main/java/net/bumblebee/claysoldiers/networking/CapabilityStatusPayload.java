package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.platform.services.AbstractCapabilityManger;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CapabilityStatusPayload implements IClientPayload {
    public static final Type<CapabilityStatusPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "capability_status_sync"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<AbstractCapabilityManger.Types, Map<Item, Boolean>>> STREAM_CODEC_DATA = ByteBufCodecs.map(
            i -> new EnumMap<>(AbstractCapabilityManger.Types.class), CodecUtils.createEnumStreamCodec(AbstractCapabilityManger.Types.class),
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.ITEM), ByteBufCodecs.BOOL)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CapabilityStatusPayload> STREAM_CODEC = StreamCodec.composite(
            STREAM_CODEC_DATA, s -> s.capabilityStatuses,
            CapabilityStatusPayload::new
    );

    private final Map<AbstractCapabilityManger.Types, Map<Item, Boolean>> capabilityStatuses;

    public CapabilityStatusPayload(Map<AbstractCapabilityManger.Types, Map<Item, Boolean>> capabilityStatuses) {
        this.capabilityStatuses = capabilityStatuses;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        AbstractCapabilityManger.setForClient(capabilityStatuses);
    }

    @Override
    public Type<? extends CapabilityStatusPayload> type() {
        return ID;
    }
}
