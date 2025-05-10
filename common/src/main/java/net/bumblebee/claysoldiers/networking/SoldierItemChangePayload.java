package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoldierItemChangePayload implements CustomPacketPayload, IClientPayload {
    public static final Type<SoldierItemChangePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_equipment_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierItemChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SoldierItemChangePayload::getEntity,
            SoldierEquipmentSlot.STREAM_CODEC,
            p -> p.slot,
            ItemStack.OPTIONAL_STREAM_CODEC,
            p -> p.stack,
            SoldierItemChangePayload::new
    );

    private final int entity;
    private final SoldierEquipmentSlot slot;
    private final ItemStack stack;

    public SoldierItemChangePayload(int pEntity, SoldierEquipmentSlot slot, ItemStack stack) {
        this.entity = pEntity;
        this.slot = slot;
        this.stack = stack;
    }

    public int getEntity() {
        return entity;
    }


    @Override
    @NotNull
    public Type<SoldierItemChangePayload> type() {
        return ID;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        Entity entity = context.player().level().getEntity(getEntity());
        if (entity instanceof AbstractClaySoldierEntity claySoldier) {
            claySoldier.setItemSlot(slot, stack);
        }
    }
}
