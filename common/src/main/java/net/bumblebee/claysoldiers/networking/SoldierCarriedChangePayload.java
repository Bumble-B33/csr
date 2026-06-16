package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoldierCarriedChangePayload implements IClientPayload {
    public static final Type<SoldierCarriedChangePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_carried_change"));
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
    public void handleClient(NetworkManger.PayloadContext context) {
        Entity entity = context.player().level().getEntity(getEntity());
        if (entity instanceof ProgrammableClaySoldierEntity claySoldier) {
            claySoldier.setCarriedStack(item);
        }
    }
}
