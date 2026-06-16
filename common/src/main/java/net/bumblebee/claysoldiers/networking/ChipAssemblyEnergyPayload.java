package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChipAssemblyEnergyPayload(BlockPos pos, long energy) implements IClientPayload {
    public static final Type<ChipAssemblyEnergyPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "chip_assembly"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChipAssemblyEnergyPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ChipAssemblyEnergyPayload::pos,
            ByteBufCodecs.VAR_LONG, ChipAssemblyEnergyPayload::energy,
            ChipAssemblyEnergyPayload::new
    );

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        if (context.player().level().getBlockEntity(pos) instanceof ChipAssemblerBlockEntity chipAssembler) {
            chipAssembler.setEnergy(energy);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
