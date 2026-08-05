package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.BlockEntityWithEnergy;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BlockEntityEnergyPayload(BlockPos pos, int energy) implements IClientPayload {
    public static final Type<BlockEntityEnergyPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "chip_assembly"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityEnergyPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockEntityEnergyPayload::pos,
            ByteBufCodecs.VAR_INT, BlockEntityEnergyPayload::energy,
            BlockEntityEnergyPayload::new
    );

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        if (context.player().level().getBlockEntity(pos) instanceof BlockEntityWithEnergy blockEntity) {
            blockEntity.setEnergy(energy);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
