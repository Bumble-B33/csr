package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record HamsterWheelEnergyPayload(long amount, BlockPos pos) implements IClientPayload {
    public static final Type<HamsterWheelEnergyPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel_energy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HamsterWheelEnergyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, HamsterWheelEnergyPayload::amount,
            BlockPos.STREAM_CODEC, HamsterWheelEnergyPayload::pos,
            HamsterWheelEnergyPayload::new
    );

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        if (context.client().level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity hamsterWheelBlock) {
            var en = hamsterWheelBlock.getEnergyStorage(null);
            if (en != null) {
                en.setEnergy(amount);
            }
        }
    }

    @Override
    public Type<? extends HamsterWheelEnergyPayload> type() {
        return ID;
    }
}
