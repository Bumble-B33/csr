package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoldierCarriedChangePayload implements IClientPayload {
    public static final Type<SoldierCarriedChangePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_carried_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierCarriedChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SoldierCarriedChangePayload::getEntity,
            ItemStack.OPTIONAL_STREAM_CODEC,
            SoldierCarriedChangePayload::getItem,
            SoldierCarriedChangePayload::new
    );

    private final int entity;
    private final ItemStack item;

    public SoldierCarriedChangePayload(int pEntity, ItemStack stack) {
        this.entity = pEntity;
        this.item = stack;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getEntity() {
        return entity;
    }

    @Override
    @NotNull
    public Type<SoldierCarriedChangePayload> type() {
        return ID;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        Entity entity = context.player().level().getEntity(getEntity());
        if (entity instanceof AbstractClaySoldierEntity claySoldier) {
            claySoldier.setCarriedStack(item);
        }
    }
}
