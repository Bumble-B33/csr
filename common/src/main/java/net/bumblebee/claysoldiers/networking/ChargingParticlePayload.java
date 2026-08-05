package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.particles.ChargingParticleOption;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ChargingParticlePayload(ChargingParticleOption particleOption, Vec3 pos) implements IClientPayload {
    public static final Type<ChargingParticlePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "particle_charge_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingParticlePayload> STREAM_CODEC = StreamCodec.composite(
            ChargingParticleOption.STREAM_CODEC, ChargingParticlePayload::particleOption,
            Vec3.STREAM_CODEC, ChargingParticlePayload::pos,
            ChargingParticlePayload::new
    );

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        context.client().level.addAlwaysVisibleParticle(particleOption, pos.x(), pos.y(), pos.z(), 0, 0, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
