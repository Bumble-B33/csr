package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.blueprint.templates.BaseImmutableTemplate;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record BlueprintClientPayload(Map<Identifier, BaseImmutableTemplate> blueprintShapeMap) implements IClientPayload {
    public static final Type<BlueprintClientPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_items_paylaod"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintClientPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, BaseImmutableTemplate.STREAM_CODEC),
            BlueprintClientPayload::blueprintShapeMap,
            BlueprintClientPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        BlueprintManager.setupClient(blueprintShapeMap, context.player().registryAccess());
    }
}
