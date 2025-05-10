package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record BlueprintPlacePayload(BlockPos pos, Item item) implements IClientPayload {
    public static final Type<BlueprintPlacePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_place"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPlacePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BlueprintPlacePayload::pos,
            ByteBufCodecs.registry(Registries.ITEM),
            BlueprintPlacePayload::item,
            BlueprintPlacePayload::new
    );
    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        if (context.client().level.getBlockEntity(pos) instanceof EaselBlockEntity easelBlockEntity) {
            easelBlockEntity.tryPlacingSoldier(item.getDefaultInstance());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
