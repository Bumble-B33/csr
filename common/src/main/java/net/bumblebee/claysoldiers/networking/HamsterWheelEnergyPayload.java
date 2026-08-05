package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record HamsterWheelEnergyPayload(int amount, BlockPos pos) implements IClientPayload {
    public static final Type<HamsterWheelEnergyPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel_energy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HamsterWheelEnergyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HamsterWheelEnergyPayload::amount,
            BlockPos.STREAM_CODEC, HamsterWheelEnergyPayload::pos,
            HamsterWheelEnergyPayload::new
    );

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
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
